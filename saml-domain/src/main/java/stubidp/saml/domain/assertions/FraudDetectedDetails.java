package stubidp.saml.domain.assertions;

public class FraudDetectedDetails {
    private final String idpFraudEventId;
    private final String fraudIndicator;

    public FraudDetectedDetails(String idpFraudEventId, String fraudIndicator) {
        this.idpFraudEventId = idpFraudEventId;
        this.fraudIndicator = fraudIndicator;
    }

    public String getIdpFraudEventId() {
        return idpFraudEventId;
    }

    public String getFraudIndicator() {
        return fraudIndicator;
    }
}
