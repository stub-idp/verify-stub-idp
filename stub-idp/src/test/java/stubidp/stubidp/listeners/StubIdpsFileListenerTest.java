package stubidp.stubidp.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.jackson.Jackson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import stubidp.stubidp.configuration.IdpStubsConfiguration;
import stubidp.stubidp.configuration.SamlConfigurationImpl;
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
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// "work around" https://bugs.openjdk.java.net/browse/JDK-7133447
@DisabledOnOs(OS.MAC)
class StubIdpsFileListenerTest {

    private final File YML_FILE;
    private final Duration refreshDuration = Duration.ofMillis(5);
    private final ObjectMapper objectMapper = Jackson.newObjectMapper(new YAMLFactory());
    private StubIdpsFileListener stubIdpsFileListener;
    private IdpStubsRepository idpStubsRepository;

    StubIdpsFileListenerTest() {
        try {
            YML_FILE = File.createTempFile("test-stub-idps", "yml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setup() throws Exception {
        StubIdp testStubIdp = new TestStubIdp("a", "b", "c", List.of(), false);
        createYamlFile(testStubIdp);
        final StubIdpConfiguration stubIdpConfiguration = mock(StubIdpConfiguration.class);
        when(stubIdpConfiguration.getStubIdpsYmlFileLocation()).thenReturn(YML_FILE.getAbsolutePath());
        when(stubIdpConfiguration.getSamlConfiguration()).thenReturn(new SamlConfigurationImpl("foo", URI.create("foo")));
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        ConfigurationFactory<IdpStubsConfiguration> configurationFactory = new DefaultConfigurationFactoryFactory<IdpStubsConfiguration>().create(IdpStubsConfiguration.class, validator, Jackson.newObjectMapper(), "");
        AllIdpsUserRepository allIdpsUserRepository = new AllIdpsUserRepository(mock(JDBIUserRepository.class));
        idpStubsRepository = new IdpStubsRepository(allIdpsUserRepository, stubIdpConfiguration,  configurationFactory);
        stubIdpsFileListener = getStubIdpsFileListener(YML_FILE.getAbsolutePath(), idpStubsRepository);
    }

    @Test
    void verifyIdaStubsRepositoryIsUpdatedOnFileChange() throws Exception {
        StubIdp changedTestStubIdp = new TestStubIdp("e", "f", "g", List.of(), false);
        createYamlFile(changedTestStubIdp);
        stubIdpsFileListener.run();
        checkIdpDataLoaded(changedTestStubIdp);
    }

    private void checkIdpDataLoaded(StubIdp changedTestStubIdp) {
        final Idp idp = idpStubsRepository.getIdpWithFriendlyId(changedTestStubIdp.getFriendlyId());
        assertThat(idp.getAssetId()).isEqualTo(changedTestStubIdp.getAssetId());
        assertThat(idp.getDisplayName()).isEqualTo(changedTestStubIdp.getDisplayName());
        assertThat(idp.getFriendlyId()).isEqualTo(changedTestStubIdp.getFriendlyId());
    }

    @Test
    void verifyIdaStubsRepositoryIsUpdatedEvenIfPreviousFileChangeWasInvalid() throws Exception {
        ensureInvalidStubIdpsConfigWasWritten();
        stubIdpsFileListener.run();
        StubIdp changedTestStubIdp = new TestStubIdp("m", "n", "o", List.of(), false);
        createYamlFile(changedTestStubIdp);
        stubIdpsFileListener.run();
        checkIdpDataLoaded(changedTestStubIdp);
    }

    private void ensureInvalidStubIdpsConfigWasWritten() throws IOException, InterruptedException {
        Files.writeString(YML_FILE.toPath(), "bad string, this test will fail", UTF_8);
        Thread.sleep(1); // ensure a different modified time
        Files.setLastModifiedTime(YML_FILE.toPath(), FileTime.from(Instant.now()));
        Thread.sleep(refreshDuration.toMillis()*2); //wait for file to be processed by monitor
    }

    private void createYamlFile(StubIdp testStubIdp) throws IOException, InterruptedException {
        final TestIdpStubsConfiguration testIdpStubsConfiguration = new TestIdpStubsConfiguration(List.of(testStubIdp));
        final String yaml = getYamlAsString(testIdpStubsConfiguration);
        Files.writeString(YML_FILE.toPath(), yaml, UTF_8);
        Thread.sleep(1); // ensure a different modified time
        Files.setLastModifiedTime(YML_FILE.toPath(), FileTime.from(Instant.now()));
        Thread.sleep(refreshDuration.toMillis()*2); //wait for file to be processed by monitor
    }

    private String getYamlAsString(TestIdpStubsConfiguration testIdpStubsConfiguration) throws JsonProcessingException {
        return objectMapper.writeValueAsString(testIdpStubsConfiguration);
    }

    private StubIdpsFileListener getStubIdpsFileListener(String stubIdpsYmlFile, IdpStubsRepository idpStubsRepository) {
        return new StubIdpsFileListener(stubIdpsYmlFile, idpStubsRepository);
    }
}
