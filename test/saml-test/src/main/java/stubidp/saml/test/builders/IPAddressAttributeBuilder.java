package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.core.Attribute;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.IPAddress;
import stubidp.saml.test.OpenSamlXmlObjectFactory;

public class IPAddressAttributeBuilder {
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();

    private String value = "1.2.3.4";

    private IPAddressAttributeBuilder() {}

    public static IPAddressAttributeBuilder anIPAddress() {
        return new IPAddressAttributeBuilder();
    }

    public Attribute build() {

        Attribute ipAddressAttribute = openSamlXmlObjectFactory.createAttribute();
        ipAddressAttribute.setFriendlyName(IdaConstants.Attributes_1_1.IPAddress.FRIENDLY_NAME);
        ipAddressAttribute.setName(IdaConstants.Attributes_1_1.IPAddress.NAME);

        IPAddress ipAddressAttributeValue = openSamlXmlObjectFactory.createIPAddressAttributeValue(value);

        ipAddressAttribute.getAttributeValues().add(ipAddressAttributeValue);

        return ipAddressAttribute;
    }

    public IPAddressAttributeBuilder withValue(String value) {
        this.value = value;
        return this;
    }
}
