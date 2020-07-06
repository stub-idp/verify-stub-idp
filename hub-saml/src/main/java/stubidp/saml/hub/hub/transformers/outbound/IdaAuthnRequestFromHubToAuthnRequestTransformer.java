package stubidp.saml.hub.hub.transformers.outbound;

import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.Scoping;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.request.IdaAuthnRequestFromHub;
import stubidp.saml.hub.hub.HubConstants;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;

import javax.inject.Inject;

public class IdaAuthnRequestFromHubToAuthnRequestTransformer extends IdaAuthnRequestToAuthnRequestTransformer<IdaAuthnRequestFromHub> {

    @Inject
    public IdaAuthnRequestFromHubToAuthnRequestTransformer(OpenSamlXmlObjectFactory samlObjectFactory) {
        super(samlObjectFactory);
    }

    protected void supplementAuthnRequestWithDetails(IdaAuthnRequestFromHub originalRequestFromHub, AuthnRequest authnRequest) {

        Conditions conditions = getSamlObjectFactory().createConditions();
        conditions.setNotOnOrAfter(originalRequestFromHub.getSessionExpiryTimestamp());
        authnRequest.setConditions(conditions);

        Scoping scoping = getSamlObjectFactory().createScoping();
        scoping.setProxyCount(0);
        authnRequest.setScoping(scoping);

        AuthnContextComparisonTypeEnumeration comparisonType = originalRequestFromHub.getComparisonType();
        RequestedAuthnContext requestedAuthnContext = getSamlObjectFactory().createRequestedAuthnContext(comparisonType);

        originalRequestFromHub.getLevelsOfAssurance().stream()
                .map(AuthnContext::getUri)
                .map(uri -> getSamlObjectFactory().createAuthnContextClassReference(uri))
                .forEach(ref -> requestedAuthnContext.getAuthnContextClassRefs().add(ref));

        NameIDPolicy nameIdPolicy = getSamlObjectFactory().createNameIdPolicy();
        nameIdPolicy.setFormat(NameIDType.PERSISTENT);
        nameIdPolicy.setSPNameQualifier(HubConstants.SP_NAME_QUALIFIER);
        nameIdPolicy.setAllowCreate(true);
        authnRequest.setNameIDPolicy(nameIdPolicy);

        authnRequest.setRequestedAuthnContext(requestedAuthnContext);

        if (originalRequestFromHub.getForceAuthentication().isPresent()) {
            authnRequest.setForceAuthn(originalRequestFromHub.getForceAuthentication().get());
        }
    }

}
