package stubidp.saml.test.builders;

import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import stubidp.saml.test.OpenSamlXmlObjectFactory;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

public class ResponseBuilder {
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();

    public static final String DEFAULT_REQUEST_ID = "default-request-id";
    public static final String DEFAULT_RESPONSE_ID = "default-response-id";

    private EncryptedAssertion defaultEncryptedAssertion;
    private boolean addDefaultEncryptedAssertionIfNoneIsAdded = true;
    private boolean shouldSign = true;
    private boolean shouldAddSignature = true;
    private SignatureAlgorithm signatureAlgorithm = new SignatureRSASHA256();
    private DigestAlgorithm digestAlgorithm = new DigestSHA256();
    private List<Assertion> assertions = new ArrayList<>();
    private List<EncryptedAssertion> encryptedAssertions = new ArrayList<>();

    private Optional<Issuer> issuer = ofNullable(IssuerBuilder.anIssuer().build());
    private Optional<String> id = Optional.of(DEFAULT_RESPONSE_ID);
    private Optional<Instant> issueInstant = ofNullable(Instant.now());
    private Optional<String> inResponseTo = Optional.of(DEFAULT_REQUEST_ID);
    private Optional<Status> status = ofNullable(StatusBuilder.aStatus().build());
    private Optional<Credential> signingCredential = empty();
    private Optional<String > destination = Optional.of("http://destination.local");

    private ResponseBuilder() {}

    public static ResponseBuilder aResponse() {
        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.defaultEncryptedAssertion = AssertionBuilder.anAssertion().build();
        return responseBuilder;
    }

    public static ResponseBuilder aValidIdpResponse() {
        return aResponse()
                .withStatus(StatusBuilder.aStatus().build())
                .addAssertion(AssertionBuilder.anAssertion().addAttributeStatement(AttributeStatementBuilder.anAttributeStatement().build()).buildUnencrypted())
                .addAssertion(AssertionBuilder.anAssertion().addAuthnStatement(AuthnStatementBuilder.anAuthnStatement().build()).buildUnencrypted());
    }

    public Response build() throws MarshallingException, SignatureException {
        Response response = openSamlXmlObjectFactory.createResponse();
        id.ifPresent(response::setID);
        inResponseTo.ifPresent(response::setInResponseTo);
        issueInstant.ifPresent(response::setIssueInstant);
        status.ifPresent(response::setStatus);
        destination.ifPresent(response::setDestination);
        response.getAssertions().addAll(assertions);
        if (encryptedAssertions.isEmpty() && addDefaultEncryptedAssertionIfNoneIsAdded) {
            response.getEncryptedAssertions().add(defaultEncryptedAssertion);
        } else {
            response.getEncryptedAssertions().addAll(encryptedAssertions);
        }
        if (issuer.isPresent()) {
            response.setIssuer(issuer.get());

            if (shouldAddSignature && Objects.nonNull(issuer.get().getValue()) && !issuer.get().getValue().isBlank()) {
                SignatureBuilder signatureBuilder = SignatureBuilder.aSignature().withSignatureAlgorithm(signatureAlgorithm);
                id.ifPresent(s -> signatureBuilder.withDigestAlgorithm(s, digestAlgorithm));
                signingCredential.ifPresent(signatureBuilder::withSigningCredential);
                response.setSignature(signatureBuilder.build());
                XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(response).marshall(response);
                if (shouldSign) {
                    Signer.signObject(response.getSignature());
                }
            }
        }
        return response;
    }

    public ResponseBuilder withIssuer(Issuer issuer) {
        this.issuer = ofNullable(issuer);
        return this;
    }

    public ResponseBuilder withId(String id) {
        this.id = ofNullable(id);
        return this;
    }

    public ResponseBuilder withInResponseTo(String requestId) {
        this.inResponseTo = ofNullable(requestId);
        return this;
    }

    public ResponseBuilder withIssueInstant(Instant issueInstant) {
        this.issueInstant = ofNullable(issueInstant);
        return this;
    }

    public ResponseBuilder addAssertion(Assertion assertion) {
        addDefaultEncryptedAssertionIfNoneIsAdded = false;
        this.assertions.add(assertion);
        return this;
    }

    public ResponseBuilder withNoDefaultAssertion() {
        this.addDefaultEncryptedAssertionIfNoneIsAdded = false;
        return this;
    }

    public ResponseBuilder addEncryptedAssertion(EncryptedAssertion encryptedAssertion) {
        addDefaultEncryptedAssertionIfNoneIsAdded = false;
        this.encryptedAssertions.add(encryptedAssertion);
        return this;
    }

    public ResponseBuilder withoutSignatureElement() {
        shouldAddSignature = false;
        return this;
    }

    public ResponseBuilder withoutSigning() {
        shouldSign = false;
        return this;
    }

    public ResponseBuilder withStatus(Status status) {
        this.status = ofNullable(status);
        return this;
    }

    public ResponseBuilder withSigningCredential(Credential signingCredential) {
        this.signingCredential = ofNullable(signingCredential);
        return this;
    }

    public ResponseBuilder withDestination(String destination) {
        this.destination = ofNullable(destination);
        return this;
    }

    public ResponseBuilder withSignatureAlgorithm(@NotNull SignatureAlgorithm signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
        return this;
    }

    public ResponseBuilder withDigestAlgorithm(@NotNull DigestAlgorithm digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
        return this;
    }
}
