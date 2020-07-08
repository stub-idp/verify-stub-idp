package stubidp.saml.domain.assertions;

import java.io.Serializable;

public class PersistentId implements Serializable {
    private final String nameId;

    public PersistentId(String nameId) {
        this.nameId = nameId;
    }

    public String getNameId() {
        return nameId;
    }
}
