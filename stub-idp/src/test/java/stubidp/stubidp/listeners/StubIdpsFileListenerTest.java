package stubidp.stubidp.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.Duration;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import stubidp.saml.stubidp.configuration.SamlConfigurationImpl;
import stubidp.stubidp.configuration.IdpStubsConfiguration;
import stubidp.stubidp.configuration.StubIdp;
import stubidp.stubidp.configuration.StubIdpConfiguration;
import stubidp.stubidp.repositories.AllIdpsUserRepository;
import stubidp.stubidp.repositories.Idp;
import stubidp.stubidp.repositories.IdpStubsRepository;
import stubidp.stubidp.repositories.jdbc.JDBIUserRepository;
import stubidp.stubidp.utils.TestIdpStubsConfiguration;
import stubidp.stubidp.utils.TestStubIdp;

import javax.validation.Validation;
import javax.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StubIdpsFileListenerTest {

    private File YML_FILE;
    private StubIdpsFileListener stubIdpsFileListener;
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private IdpStubsRepository idpStubsRepository;

    public StubIdpsFileListenerTest() {
        try {
            YML_FILE = File.createTempFile("test-stub-idps", "yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    public void setup() throws Exception {
        StubIdp testStubIdp = new TestStubIdp("a", "b", "c", List.of(), false);
        createYamlFile(testStubIdp);
        final StubIdpConfiguration stubIdpConfiguration = mock(StubIdpConfiguration.class);
        when(stubIdpConfiguration.getStubIdpsYmlFileLocation()).thenReturn(YML_FILE.getAbsolutePath());
        when(stubIdpConfiguration.getStubIdpYmlFileRefresh()).thenReturn(Duration.milliseconds(20));
        when(stubIdpConfiguration.getSamlConfiguration()).thenReturn(new SamlConfigurationImpl("foo", URI.create("foo")));
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        ConfigurationFactory<IdpStubsConfiguration> configurationFactory = new DefaultConfigurationFactoryFactory<IdpStubsConfiguration>().create(IdpStubsConfiguration.class, validator, Jackson.newObjectMapper(), "");
        ConfigurationSourceProvider configurationSourceProvider = path -> new FileInputStream(stubIdpConfiguration.getStubIdpsYmlFileLocation());
        AllIdpsUserRepository allIdpsUserRepository = new AllIdpsUserRepository(mock(JDBIUserRepository.class));
        idpStubsRepository = new IdpStubsRepository(allIdpsUserRepository, stubIdpConfiguration,  configurationFactory) {
            @Override
            public void load(String yamlFile) {
                super.load(yamlFile);
                countDownLatch.countDown();
            }
        };
        stubIdpsFileListener = getStubIdpsFileListener(idpStubsRepository, stubIdpConfiguration);
        stubIdpsFileListener.start();
    }

    @Disabled("a race condition is preventing this test from running successfully, consistently")
    @Test
    public void verifyIdaStubsRepositoryIsUpdatedOnFileChange() throws Exception {
        initializeSynchronizationWithFileMonitor();
        StubIdp changedTestStubIdp = new TestStubIdp("e", "f", "g", List.of(), false);
        createYamlFile(changedTestStubIdp);

        waitForFileToBeReadByMonitor();

        checkIdpDataLoaded(changedTestStubIdp);
    }

    private void checkIdpDataLoaded(StubIdp changedTestStubIdp) {
        final Idp idp = idpStubsRepository.getIdpWithFriendlyId(changedTestStubIdp.getFriendlyId());
        assertThat(idp.getAssetId()).isEqualTo(changedTestStubIdp.getAssetId());
        assertThat(idp.getDisplayName()).isEqualTo(changedTestStubIdp.getDisplayName());
        assertThat(idp.getFriendlyId()).isEqualTo(changedTestStubIdp.getFriendlyId());
    }

    @Test
    public void verifyIdaStubsRepositoryIsUpdatedEvenIfPreviousFileChangeWasInvalid() throws Exception {
        ensureInvalidStubIdpsConfigWasProcessed();
        initializeSynchronizationWithFileMonitor();
        StubIdp changedTestStubIdp = new TestStubIdp("m", "n", "o", List.of(), false);
        createYamlFile(changedTestStubIdp);

        waitForFileToBeReadByMonitor();
        checkIdpDataLoaded(changedTestStubIdp);
    }

    @AfterEach
    public void teardown() throws Exception {
        stubIdpsFileListener.stop();
    }

    private void ensureInvalidStubIdpsConfigWasProcessed() throws IOException, InterruptedException {
        writeStringToFile(YML_FILE, "bad string, this test will fail", UTF_8);
        FileUtils.touch(YML_FILE);
        Thread.sleep(500); //wait for file to be processed by monitor
    }

    private void waitForFileToBeReadByMonitor() throws InterruptedException {
        countDownLatch.await(10, TimeUnit.SECONDS);
    }

    private void initializeSynchronizationWithFileMonitor() {
        countDownLatch = new CountDownLatch(1);
    }

    private void createYamlFile(StubIdp testStubIdp) throws IOException, InterruptedException {
        TestIdpStubsConfiguration testIdpStubsConfiguration = new TestIdpStubsConfiguration(List.of(testStubIdp));

        final String yaml = getYamlAsString(testIdpStubsConfiguration);
        writeStringToFile(YML_FILE, yaml, UTF_8);
        Thread.sleep(1); // ensure a different modified time
        FileUtils.touch(YML_FILE);
        Thread.sleep(500); //wait for file to be processed by monitor
    }

    private String getYamlAsString(TestIdpStubsConfiguration testIdpStubsConfiguration) throws JsonProcessingException {
        return new ObjectMapper(new YAMLFactory()).writeValueAsString(testIdpStubsConfiguration);
    }

    private StubIdpsFileListener getStubIdpsFileListener(IdpStubsRepository idpStubsRepository, final StubIdpConfiguration stubIdpConfiguration) {
        return new StubIdpsFileListener(stubIdpConfiguration, idpStubsRepository);
    }
}
