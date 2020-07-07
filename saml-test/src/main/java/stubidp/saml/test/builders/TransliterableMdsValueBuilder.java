package stubidp.saml.test.builders;

import stubidp.saml.domain.assertions.TransliterableMdsValue;

import java.time.Instant;
import java.time.ZoneId;

public class TransliterableMdsValueBuilder {
    private String value = null;
    private Instant from = Instant.now().atZone(ZoneId.of("UTC")).minusDays(5).toInstant();
    private Instant to = Instant.now();
    private boolean verified = false;

    private TransliterableMdsValueBuilder() {}

    public static TransliterableMdsValueBuilder asTransliterableMdsValue() {
        return new TransliterableMdsValueBuilder();
    }

    public TransliterableMdsValue build() {
        return new TransliterableMdsValue(SimpleMdsValueBuilder.<String>aSimpleMdsValue()
                .withValue(value)
                .withFrom(from)
                .withTo(to)
                .withVerifiedStatus(verified)
                .build());
    }

    public TransliterableMdsValueBuilder withValue(String value) {
        this.value = value;
        return this;
    }

    public TransliterableMdsValueBuilder withFrom(Instant from) {
        this.from = from;
        return this;
    }

    public TransliterableMdsValueBuilder withTo(Instant to) {
        this.to = to;
        return this;
    }

    public TransliterableMdsValueBuilder withVerifiedStatus(boolean verified) {
        this.verified = verified;
        return this;
    }
}
