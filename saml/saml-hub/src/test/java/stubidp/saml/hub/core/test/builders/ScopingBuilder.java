package stubidp.saml.hub.core.test.builders;

import org.opensaml.saml.saml2.core.Scoping;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;

public class ScopingBuilder {

    private final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();

    public static ScopingBuilder aScoping() {
        return new ScopingBuilder();
    }

    public Scoping build() {
        return openSamlXmlObjectFactory.createScoping();
    }
}
