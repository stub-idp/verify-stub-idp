package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.core.Issuer;
import stubidp.saml.test.OpenSamlXmlObjectFactory;
import stubidp.test.devpki.TestCertificateStrings;

import java.util.Optional;

public class IssuerBuilder {
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    private Optional<String> issuerId = Optional.ofNullable(TestCertificateStrings.TEST_ENTITY_ID);
    private String format = null;

    private IssuerBuilder() {}

    public static IssuerBuilder anIssuer() {
        return new IssuerBuilder();
    }

    public Issuer build() {
        Issuer issuer = openSamlXmlObjectFactory.createIssuer(issuerId.orElse(null));
        issuer.setFormat(format);
        return issuer;
    }

    public IssuerBuilder withIssuerId(String issuerId) {
        this.issuerId = Optional.ofNullable(issuerId);
        return this;
    }

    public IssuerBuilder withFormat(String format) {
        this.format = format;
        return this;
    }
}
