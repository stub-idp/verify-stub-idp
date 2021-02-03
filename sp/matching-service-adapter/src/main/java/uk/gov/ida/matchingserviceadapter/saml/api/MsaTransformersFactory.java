package uk.gov.ida.matchingserviceadapter.saml.api;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA1;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import org.w3c.dom.Element;
import stubidp.saml.hub.core.validators.assertion.AssertionAttributeStatementValidator;
import stubidp.saml.hub.core.validators.assertion.AssertionValidator;
import stubidp.saml.hub.core.validators.assertion.IdentityProviderAssertionValidator;
import stubidp.saml.hub.core.validators.subject.AssertionSubjectValidator;
import stubidp.saml.hub.core.validators.subjectconfirmation.AssertionSubjectConfirmationValidator;
import stubidp.saml.hub.core.validators.subjectconfirmation.BasicAssertionSubjectConfirmationValidator;
import stubidp.saml.hub.test.transformers.ResponseToElementTransformer;
import stubidp.saml.hub.transformers.outbound.MatchingServiceIdaStatusMarshaller;
import stubidp.saml.security.EncrypterFactory;
import stubidp.saml.security.EncryptionCredentialResolver;
import stubidp.saml.security.EntityToEncryptForLocator;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.security.IdaKeyStoreCredentialRetriever;
import stubidp.saml.security.SignatureFactory;
import stubidp.saml.security.validators.issuer.IssuerValidator;
import stubidp.saml.serializers.serializers.XmlObjectToBase64EncodedStringTransformer;
import stubidp.saml.serializers.serializers.XmlObjectToElementTransformer;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.api.CoreTransformersFactory;
import stubidp.saml.utils.core.transformers.outbound.OutboundAssertionToSubjectTransformer;
import stubidp.saml.utils.core.transformers.outbound.decorators.ResponseAssertionSigner;
import stubidp.saml.utils.core.transformers.outbound.decorators.ResponseSignatureCreator;
import stubidp.saml.utils.core.transformers.outbound.decorators.SamlResponseAssertionEncrypter;
import stubidp.saml.utils.core.transformers.outbound.decorators.SamlSignatureSigner;
import stubidp.saml.utils.hub.transformers.outbound.UnknownUserCreationIdaStatusMarshaller;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.domain.HealthCheckResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertion;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromUnknownUserCreationService;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers.HealthCheckResponseFromMatchingServiceTransformer;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers.MatchingServiceAssertionToAssertionTransformer;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers.MatchingServiceAuthnStatementToAuthnStatementTransformer;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers.OutboundResponseFromMatchingServiceToSamlResponseTransformer;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers.OutboundResponseFromUnknownUserCreationServiceToSamlResponseTransformer;

import java.util.function.Function;

@SuppressWarnings("unused")
public class MsaTransformersFactory {
    private CoreTransformersFactory coreTransformersFactory;

    public MsaTransformersFactory() {
        coreTransformersFactory = new CoreTransformersFactory();
    }

    public ResponseToElementTransformer getResponseToElementTransformer(
            EncryptionCredentialResolver encryptionCredentialResolver,
            IdaKeyStore keyStore,
            EntityToEncryptForLocator entityToEnryptForLocator,
            MatchingServiceAdapterConfiguration configuration
    ) {
        SignatureAlgorithm signatureAlgorithm = configuration.shouldSignWithSHA1() ?
                new SignatureRSASHA1() :
                new SignatureRSASHA256();

        SignatureFactory signatureFactory = new SignatureFactory(
            new IdaKeyStoreCredentialRetriever(keyStore),
                signatureAlgorithm,
            new DigestSHA256()
        );
        SamlResponseAssertionEncrypter assertionEncrypter = new SamlResponseAssertionEncrypter(
                encryptionCredentialResolver,
                new EncrypterFactory(),
                entityToEnryptForLocator);
        return new ResponseToElementTransformer(
                new XmlObjectToElementTransformer<>(),
                new SamlSignatureSigner<>(),
                assertionEncrypter,
                new ResponseAssertionSigner(signatureFactory),
                new ResponseSignatureCreator(signatureFactory)
        );
    }

    public HealthCheckResponseFromMatchingServiceTransformer getHealthCheckResponseFromMatchingServiceToResponseTransformer() {
        return new HealthCheckResponseFromMatchingServiceTransformer(
                new OpenSamlXmlObjectFactory(),
                new MatchingServiceIdaStatusMarshaller(new OpenSamlXmlObjectFactory())
        );
    }

    public Function<OutboundResponseFromUnknownUserCreationService, Element> getOutboundResponseFromUnknownUserCreationServiceToElementTransformer(
            final EncryptionCredentialResolver encryptionCredentialResolver,
            final IdaKeyStore keyStore,
            final EntityToEncryptForLocator entityToEncryptForLocator,
            MatchingServiceAdapterConfiguration configuration) {
        Function<OutboundResponseFromUnknownUserCreationService, Response> t1 =
                new OutboundResponseFromUnknownUserCreationServiceToSamlResponseTransformer(
                        new OpenSamlXmlObjectFactory(),
                        new UnknownUserCreationIdaStatusMarshaller(new OpenSamlXmlObjectFactory()),
                        createMatchingServiceAssertionToAssertionTransformer()
                );
        Function<Response, Element> t2 = getResponseToElementTransformer(
                encryptionCredentialResolver,
                keyStore,
                entityToEncryptForLocator,
                configuration);
        return t2.compose(t1);
    }

    public Function<OutboundResponseFromMatchingService, Element> getOutboundResponseFromMatchingServiceToElementTransformer(
            final EncryptionCredentialResolver encryptionCredentialResolver,
            final IdaKeyStore keyStore,
            final EntityToEncryptForLocator entityToEncryptForLocator,
            MatchingServiceAdapterConfiguration configuration) {
        Function<OutboundResponseFromMatchingService, Response> t1 = new OutboundResponseFromMatchingServiceToSamlResponseTransformer(
                new MatchingServiceIdaStatusMarshaller(new OpenSamlXmlObjectFactory()),
                new OpenSamlXmlObjectFactory(),
                createMatchingServiceAssertionToAssertionTransformer()
        );
        Function<Response, Element> t2 = getResponseToElementTransformer(
                encryptionCredentialResolver,
                keyStore,
            entityToEncryptForLocator,
            configuration);
        return t2.compose(t1);
    }

    private MatchingServiceAssertionToAssertionTransformer createMatchingServiceAssertionToAssertionTransformer() {
        return new MatchingServiceAssertionToAssertionTransformer(
                new OpenSamlXmlObjectFactory(),
                new MatchingServiceAuthnStatementToAuthnStatementTransformer(new OpenSamlXmlObjectFactory()),
                new OutboundAssertionToSubjectTransformer(new OpenSamlXmlObjectFactory())
        );
    }

    public Function<HealthCheckResponseFromMatchingService, Element> getHealthcheckResponseFromMatchingServiceToElementTransformer(
            final EncryptionCredentialResolver encryptionCredentialResolver,
            final IdaKeyStore keyStore,
            final EntityToEncryptForLocator entityToEncryptForLocator,
            MatchingServiceAdapterConfiguration configuration
    ){
        Function<HealthCheckResponseFromMatchingService, Response> t1 = getHealthCheckResponseFromMatchingServiceToResponseTransformer();
        Function<Response, Element> t2 = getResponseToElementTransformer(
                encryptionCredentialResolver,
                keyStore,
                entityToEncryptForLocator,
                configuration);
        return t2.compose(t1);
    }

    public Function<MatchingServiceAssertion, String> getMatchingServiceAssertionToStringTransformer() {
        Function<MatchingServiceAssertion, Assertion> matchingServiceAssertionToAssertionTransformer = createMatchingServiceAssertionToAssertionTransformer();
        Function<Assertion, String> assertionToStringTransformer = new XmlObjectToBase64EncodedStringTransformer<>();
        return assertionToStringTransformer.compose(matchingServiceAssertionToAssertionTransformer);
    }

    private IdentityProviderAssertionValidator getIdentityProviderAssertionValidator() {
        return new IdentityProviderAssertionValidator(
                new IssuerValidator(),
                new AssertionSubjectValidator(),
                new AssertionAttributeStatementValidator(),
                new AssertionSubjectConfirmationValidator()
        );
    }

    private AssertionValidator getAssertionValidator() {
        return new AssertionValidator(
                    new IssuerValidator(),
                    new AssertionSubjectValidator(),
                    new AssertionAttributeStatementValidator(),
                    new BasicAssertionSubjectConfirmationValidator()
        );
    }
}
