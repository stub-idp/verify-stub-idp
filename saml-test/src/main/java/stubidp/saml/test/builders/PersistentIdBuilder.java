package stubidp.saml.test.builders;

import stubidp.saml.domain.assertions.PersistentId;

public class PersistentIdBuilder {
    private String nameId = "default-name-id";

    private PersistentIdBuilder() {}

    public static PersistentIdBuilder aPersistentId() {
        return new PersistentIdBuilder();
    }

    public PersistentId build() {
        return new PersistentId(nameId);
    }

    public PersistentIdBuilder withNameId(String persistentId) {
        this.nameId = persistentId;
        return this;
    }
}
