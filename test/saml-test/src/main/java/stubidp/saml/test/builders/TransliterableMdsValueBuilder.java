package stubidp.saml.test.builders;

import stubidp.saml.domain.assertions.TransliterableMdsValue;

import java.time.LocalDate;

public class TransliterableMdsValueBuilder {
    private String value = null;
    private LocalDate from = LocalDate.now().minusDays(5);
    private LocalDate to = LocalDate.now();
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

    public TransliterableMdsValueBuilder withFrom(LocalDate from) {
        this.from = from;
        return this;
    }

    public TransliterableMdsValueBuilder withTo(LocalDate to) {
        this.to = to;
        return this;
    }

    public TransliterableMdsValueBuilder withVerifiedStatus(boolean verified) {
        this.verified = verified;
        return this;
    }
}
