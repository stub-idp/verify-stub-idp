package stubidp.saml.hub.core.test.builders;

import java.util.Optional;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;

public class NameIdPolicyBuilder {

    private final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    private Optional<String> format = Optional.ofNullable(NameIDType.PERSISTENT);

    public static NameIdPolicyBuilder aNameIdPolicy() {
        return new NameIdPolicyBuilder();
    }

    public NameIDPolicy build() {

        NameIDPolicy nameIdPolicy = openSamlXmlObjectFactory.createNameIdPolicy();

        format.ifPresent(nameIdPolicy::setFormat);

        return nameIdPolicy;
    }

    public NameIdPolicyBuilder withFormat(String format) {
        this.format = Optional.ofNullable(format);
        return this;
    }
}
