package uk.gov.ida.integrationTest.support;

import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import uk.gov.ida.rp.testrp.MsaStubRule;

public class IntegrationTestHelper {
    static {
        //doALittleHackToMakeGuicierHappy
        // magically, this has to be the first test to run otherwise things will fail.
        // see:
        // - https://github.com/HubSpot/dropwizard-guice/issues/95
        // - https://github.com/Squarespace/jersey2-guice/pull/39
        JerseyGuiceUtils.reset();
    }

    private static MsaStubRule msaStubRule = null;

    @BeforeClass
    public static void beforeClass() {
        getMsaStubRule().start();
    }

    @AfterClass
    public static void tearDown() {
        getMsaStubRule().stop();
    }

    public static synchronized MsaStubRule getMsaStubRule() {
        if(null==msaStubRule) {
            msaStubRule = new MsaStubRule("metadata.xml");
        }
        return msaStubRule;
    }

}
