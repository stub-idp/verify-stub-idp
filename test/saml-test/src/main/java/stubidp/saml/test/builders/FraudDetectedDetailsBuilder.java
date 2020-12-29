package stubidp.saml.test.builders;

import stubidp.saml.domain.assertions.FraudDetectedDetails;

public class FraudDetectedDetailsBuilder {
    private final String eventId = "default-event-id";
    private String fraudIndicator = "IT01";

    private FraudDetectedDetailsBuilder() {}

    public static FraudDetectedDetailsBuilder aFraudDetectedDetails() {
        return new FraudDetectedDetailsBuilder();
    }

    public FraudDetectedDetails build() {
        return new FraudDetectedDetails(eventId, fraudIndicator);
    }

    public FraudDetectedDetailsBuilder withFraudIndicator(String fraudIndicator) {
        this.fraudIndicator = fraudIndicator;
        return this;
    }

}
