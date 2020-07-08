package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import stubidp.saml.test.OpenSamlXmlObjectFactory;

import java.util.Optional;

public class NameIdPolicyBuilder {
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    private Optional<String> format = Optional.ofNullable(NameIDType.PERSISTENT);

    private NameIdPolicyBuilder() {}

    public static NameIdPolicyBuilder aNameIdPolicy() {
        return new NameIdPolicyBuilder();
    }

    public NameIDPolicy build() {
        NameIDPolicy nameIdPolicy = openSamlXmlObjectFactory.createNameIdPolicy();

        if (format.isPresent()) {
            nameIdPolicy.setFormat(format.get());
        }

        return nameIdPolicy;
    }

    public NameIdPolicyBuilder withFormat(String format) {
        this.format = Optional.ofNullable(format);
        return this;
    }
}
