package stubidp.saml.utils.core.test.builders.metadata;

import org.opensaml.saml.saml2.metadata.SurName;

import java.util.Optional;

public class SurNameBuilder {
    private Optional<String> name = Optional.of("Flintstone");

    public static SurNameBuilder aSurName(){
        return new SurNameBuilder();
    }

    public SurName build() {
        SurName name = new org.opensaml.saml.saml2.metadata.impl.SurNameBuilder().buildObject();
        name.setValue(this.name.get());
        return name;
    }
}
