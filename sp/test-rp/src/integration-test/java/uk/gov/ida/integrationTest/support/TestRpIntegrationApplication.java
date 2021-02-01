package uk.gov.ida.integrationTest.support;

import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;
import io.dropwizard.setup.Bootstrap;
import uk.gov.ida.rp.testrp.EntityId;
import uk.gov.ida.rp.testrp.TestRpApplication;
import uk.gov.ida.rp.testrp.TestRpModule;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.saml.core.test.HardCodedKeyStore;
import uk.gov.ida.saml.security.SigningKeyStore;

public class TestRpIntegrationApplication extends TestRpApplication {

    @Override
    public void initialize(Bootstrap<TestRpConfiguration> bootstrap) {
        super.initialize(bootstrap);
        Modules.override(new TestRpModule()).with(new AbstractModule() {
            @Override
            protected void configure() {
                bind(SigningKeyStore.class).toInstance(new HardCodedKeyStore(EntityId.TEST_RP));
            }
        });
    }
}
