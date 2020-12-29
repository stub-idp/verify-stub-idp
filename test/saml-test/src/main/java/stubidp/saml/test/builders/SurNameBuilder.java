package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.metadata.SurName;

import java.util.Optional;

public class SurNameBuilder {
    private final Optional<String> name = Optional.of("Flintstone");

    private SurNameBuilder() {}

    public static SurNameBuilder aSurName(){
        return new SurNameBuilder();
    }

    public SurName build() {
        SurName name = new org.opensaml.saml.saml2.metadata.impl.SurNameBuilder().buildObject();
        name.setValue(this.name.get());
        return name;
    }
}
