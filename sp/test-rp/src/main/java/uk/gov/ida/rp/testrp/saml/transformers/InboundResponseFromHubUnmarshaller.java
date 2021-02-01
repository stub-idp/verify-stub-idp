package uk.gov.ida.rp.testrp.saml.transformers;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Issuer;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.PassthroughAssertion;
import stubidp.saml.domain.assertions.PersistentId;
import stubidp.saml.domain.assertions.TransactionIdaStatus;
import stubidp.saml.hub.transformers.inbound.PassthroughAssertionUnmarshaller;
import stubidp.saml.hub.transformers.inbound.TransactionIdaStatusUnmarshaller;
import stubidp.saml.security.validators.ValidatedAssertions;
import stubidp.saml.security.validators.ValidatedResponse;
import uk.gov.ida.saml.idp.stub.domain.InboundResponseFromHub;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/*
* Used by the fake relying parties, so there is a bit of dodgyness here.
* */
public class InboundResponseFromHubUnmarshaller {
    private final PassthroughAssertionUnmarshaller passthroughAssertionUnmarshaller;
    private final TransactionIdaStatusUnmarshaller statusUnmarshaller;

    public InboundResponseFromHubUnmarshaller(
            TransactionIdaStatusUnmarshaller statusUnmarshaller,
            PassthroughAssertionUnmarshaller passthroughAssertionUnmarshaller) {
        this.statusUnmarshaller = statusUnmarshaller;
        this.passthroughAssertionUnmarshaller = passthroughAssertionUnmarshaller;
    }

    public InboundResponseFromHub fromSaml(ValidatedResponse validatedResponse, ValidatedAssertions validatedAssertions) {
        Optional<PersistentId> persistentId = Optional.empty();
        Optional<List<Attribute>> attributes = Optional.empty();
        Optional<AuthnContext> authnContext = Optional.empty();

        for (Assertion assertion : validatedAssertions.getAssertions()) {
            PassthroughAssertion outboundAssertion = passthroughAssertionUnmarshaller.fromAssertion(assertion);
            List<AttributeStatement> attributeStatements = assertion.getAttributeStatements();
            if (attributeStatements.size() == 1) {
                attributes = Optional.ofNullable(attributeStatements.get(0).getAttributes());
            }
            persistentId = Optional.ofNullable(outboundAssertion.getPersistentId());
            authnContext = outboundAssertion.getAuthnContext();
        }

        TransactionIdaStatus transformedStatus = statusUnmarshaller.fromSaml(validatedResponse.getStatus());

        Issuer issuer = validatedResponse.getIssuer();
        return new InboundResponseFromHub(
                validatedResponse.getID(),
                validatedResponse.getIssueInstant(),
                validatedResponse.getInResponseTo(),
                issuer == null ? null : issuer.getValue(),
                URI.create(validatedResponse.getDestination()),
                attributes,
                transformedStatus,
                persistentId,
                authnContext);
    }
}
