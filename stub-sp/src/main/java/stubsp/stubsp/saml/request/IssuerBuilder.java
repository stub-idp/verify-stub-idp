package stubsp.stubsp.saml.request;

import org.opensaml.saml.saml2.core.Issuer;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;

import java.util.Optional;

public class IssuerBuilder {
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    private Optional<String> issuerId = Optional.empty();
    private String format = null;

    static IssuerBuilder anIssuer() {
        return new IssuerBuilder();
    }

    public Issuer build() {

        Issuer issuer = openSamlXmlObjectFactory.createIssuer(issuerId.orElse(null));

        issuer.setFormat(format);

        return issuer;
    }

    IssuerBuilder withIssuerId(String issuerId) {
        this.issuerId = Optional.ofNullable(issuerId);
        return this;
    }

    public IssuerBuilder withFormat(String format) {
        this.format = format;
        return this;
    }
}
