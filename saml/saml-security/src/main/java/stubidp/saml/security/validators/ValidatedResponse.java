package stubidp.saml.security.validators;

import org.opensaml.core.xml.Namespace;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.xmlsec.signature.Signature;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public class ValidatedResponse implements ValidatedEncryptedAssertionContainer {
    private final Response response;

    public ValidatedResponse(Response response) {
        this.response = response;
    }

    public boolean isSuccess() {
        return response.getStatus().getStatusCode().getValue().equals(StatusCode.SUCCESS);
    }

    public String getID() {
        return response.getID();
    }

    public String getInResponseTo() {
        return response.getInResponseTo();
    }

    public Issuer getIssuer() {
        return response.getIssuer();
    }

    public Instant getIssueInstant() {
        return response.getIssueInstant();
    }

    public Status getStatus() {
        return response.getStatus();
    }

    public Signature getSignature() { return response.getSignature(); }

    public String getDestination() {
        return response.getDestination();
    }

    public Set<Namespace> getNamespaces() {
        return response.getNamespaces();
    }

    @Override
    public List<EncryptedAssertion> getEncryptedAssertions() {
        return response.getEncryptedAssertions();
    }
}
