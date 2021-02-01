package uk.gov.ida.integrationTest.support;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import uk.gov.ida.rp.testrp.MsaStubRule;

public class IntegrationTestHelper {

    private static final MsaStubRule msaStubRule = new MsaStubRule("metadata.xml");

    @BeforeAll
    public static void beforeClass() {
        msaStubRule.start();
    }

    @AfterAll
    public static void tearDown() {
        msaStubRule.stop();
    }

    public static MsaStubRule getMsaStubRule() {
        return msaStubRule;
    }
}
