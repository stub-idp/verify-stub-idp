package stubidp.stubidp.saml;

import io.dropwizard.util.Duration;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.PredicateRoleDescriptorResolver;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.saml.hub.core.validators.DestinationValidator;
import stubidp.saml.hub.hub.validators.authnrequest.AuthnRequestFromTransactionValidator;
import stubidp.saml.hub.hub.validators.authnrequest.AuthnRequestIdKey;
import stubidp.saml.hub.hub.validators.authnrequest.AuthnRequestIssueInstantValidator;
import stubidp.saml.hub.hub.validators.authnrequest.ConcurrentMapIdExpirationCache;
import stubidp.saml.hub.hub.validators.authnrequest.DuplicateAuthnRequestValidator;
import stubidp.saml.security.CertificateChainEvaluableCriterion;
import stubidp.saml.security.MetadataBackedSignatureValidator;
import stubidp.saml.security.validators.issuer.IssuerValidator;
import stubidp.saml.serializers.deserializers.validators.NotNullSamlStringValidator;
import stubidp.saml.utils.hub.transformers.inbound.decorators.AuthnRequestSizeValidator;
import stubidp.saml.utils.hub.validators.StringSizeValidator;
import stubidp.stubidp.domain.factories.StubTransformersFactory;
import stubidp.stubidp.exceptions.InvalidEidasAuthnRequestException;
import stubidp.utils.security.security.X509CertificateFactory;
import stubidp.utils.security.security.verification.CertificateChainValidator;
import stubidp.utils.security.security.verification.PKIXParametersProvider;

import java.security.KeyStore;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public abstract class BaseAuthnRequestValidator {

    private static final Logger LOG = LoggerFactory.getLogger(BaseAuthnRequestValidator.class);

    private static final ConcurrentMapIdExpirationCache<AuthnRequestIdKey> concurrentMapIdExpirationCache = new ConcurrentMapIdExpirationCache<>(new ConcurrentHashMap<>());
    private static final Duration requestValidityDuration = Duration.minutes(5); // should be long enough...
    private static final AuthnRequestFromTransactionValidator authnRequestFromTransactionValidator = new AuthnRequestFromTransactionValidator(new IssuerValidator(), new DuplicateAuthnRequestValidator(concurrentMapIdExpirationCache, () -> requestValidityDuration), new AuthnRequestIssueInstantValidator(() -> requestValidityDuration));
    private static final AuthnRequestSizeValidator authnRequestSizeValidator = new AuthnRequestSizeValidator(new StringSizeValidator());
    private static final NotNullSamlStringValidator notNullSamlStringValidator = new NotNullSamlStringValidator();
    private static final Function<String, AuthnRequest> stringToAuthnRequestTransformer = new StubTransformersFactory().getStringToAuthnRequest();

    protected final MetadataBackedSignatureValidator metadataBackedSignatureValidator;

    public BaseAuthnRequestValidator(MetadataResolver metadataResolver, KeyStore trustStore) {
        this.metadataBackedSignatureValidator = getMetadataBackedSignatureValidator(metadataResolver, trustStore);
    }

    public final AuthnRequest transformAndValidate(String schemeId, String samlRequest) {
        notNullSamlStringValidator.validate(samlRequest);
        authnRequestSizeValidator.validate(samlRequest);
        AuthnRequest authnRequest = stringToAuthnRequestTransformer.apply(samlRequest);
        validateSpecificQualities(authnRequest);
        validateSignature(authnRequest);
        authnRequestFromTransactionValidator.validate(authnRequest);
        getDestinationValidator(schemeId).validate(authnRequest.getDestination());
        return authnRequest;
    }

    protected abstract DestinationValidator getDestinationValidator(String schemeId);

    protected abstract void validateSpecificQualities(AuthnRequest request);

    protected abstract void validateSignature(AuthnRequest request);

    private MetadataBackedSignatureValidator getMetadataBackedSignatureValidator(MetadataResolver metadataResolver, KeyStore trustStore) {
        CertificateChainValidator certificateChainValidator = new CertificateChainValidator(new PKIXParametersProvider(), new X509CertificateFactory());
        CertificateChainEvaluableCriterion c = new CertificateChainEvaluableCriterion(certificateChainValidator, trustStore);
        MetadataBackedSignatureValidator metadataBackedSignatureValidator = MetadataBackedSignatureValidator
                .withCertificateChainValidation(new ExplicitKeySignatureTrustEngine(getMetadataCredentialResolver(metadataResolver), DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver()), c);
        return metadataBackedSignatureValidator;
    }

    private MetadataCredentialResolver getMetadataCredentialResolver(MetadataResolver metadataResolver) {
        try {
            PredicateRoleDescriptorResolver predicateRoleDescriptorResolver = new PredicateRoleDescriptorResolver(metadataResolver);
            predicateRoleDescriptorResolver.initialize();
            MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolver();
            metadataCredentialResolver.setRoleDescriptorResolver(predicateRoleDescriptorResolver);
            metadataCredentialResolver.setKeyInfoCredentialResolver(DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver());
            metadataCredentialResolver.initialize();
            return metadataCredentialResolver;
        } catch (ComponentInitializationException e) {
            throw new InvalidEidasAuthnRequestException(e);
        }
    }
}
