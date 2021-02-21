package acceptance.uk.gov.ida.verifyserviceprovider.builders;

import uk.gov.ida.verifyserviceprovider.compliance.dto.MatchingDataset;
import uk.gov.ida.verifyserviceprovider.compliance.dto.MatchingDatasetBuilder;

import javax.ws.rs.client.Entity;
import java.util.HashMap;
import java.util.Map;

import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;

public class ComplianceToolV2InitialisationRequestBuilder {

    private String serviceEntityId = "http://verify-service-provider";
    private String assertionConsumerServiceUrl = "http://verify-service-provider/response";
    private String signingCertificate = TEST_RP_PUBLIC_SIGNING_CERT;
    private String encryptionCertificate = TEST_RP_PUBLIC_ENCRYPTION_CERT;
    private MatchingDataset matchingDataset = new MatchingDatasetBuilder().build();

    public static ComplianceToolV2InitialisationRequestBuilder aComplianceToolV2InitialisationRequest() {
        return new ComplianceToolV2InitialisationRequestBuilder();
    }

    public Entity<Map<String, Object>> build() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("serviceEntityId", serviceEntityId);
        map.put("assertionConsumerServiceUrl", assertionConsumerServiceUrl);
        map.put("signingCertificate", signingCertificate);
        map.put("encryptionCertificate", encryptionCertificate);
        map.put("matchingDatasetJson", matchingDataset);

        return Entity.json(map);
    }

    public ComplianceToolV2InitialisationRequestBuilder withMatchingDataSet(MatchingDataset matchingDataset) {
        this.matchingDataset = matchingDataset;
        return this;
    }
}
