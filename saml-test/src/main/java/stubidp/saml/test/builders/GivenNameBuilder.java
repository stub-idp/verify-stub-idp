package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.metadata.GivenName;

import java.util.Optional;

public class GivenNameBuilder {
    private Optional<String> value = Optional.ofNullable("Fred");

    private GivenNameBuilder() {}

    public static GivenNameBuilder aGivenName(){
        return new GivenNameBuilder();
    }

    public GivenName build() {
        GivenName givenName = new org.opensaml.saml.saml2.metadata.impl.GivenNameBuilder().buildObject();
        givenName.setValue(value.get());
        return givenName;
    }
}
