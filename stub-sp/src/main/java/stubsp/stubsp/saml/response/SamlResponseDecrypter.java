package stubsp.stubsp.saml.response;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import org.slf4j.event.Level;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;
import stubidp.saml.hub.core.validators.DestinationValidator;
import stubidp.saml.hub.core.validators.assertion.AssertionAttributeStatementValidator;
import stubidp.saml.hub.core.validators.assertion.AuthnStatementAssertionValidator;
import stubidp.saml.hub.core.validators.assertion.DuplicateAssertionValidatorImpl;
import stubidp.saml.hub.core.validators.assertion.IPAddressValidator;
import stubidp.saml.hub.core.validators.assertion.IdentityProviderAssertionValidator;
import stubidp.saml.hub.core.validators.assertion.MatchingDatasetAssertionValidator;
import stubidp.saml.hub.core.validators.subject.AssertionSubjectValidator;
import stubidp.saml.hub.core.validators.subjectconfirmation.AssertionSubjectConfirmationValidator;
import stubidp.saml.hub.hub.domain.InboundResponseFromIdp;
import stubidp.saml.hub.hub.transformers.inbound.IdaResponseFromIdpUnmarshaller;
import stubidp.saml.hub.hub.transformers.inbound.IdpIdaStatusUnmarshaller;
import stubidp.saml.hub.hub.transformers.inbound.PassthroughAssertionUnmarshaller;
import stubidp.saml.hub.hub.transformers.inbound.SamlStatusToCountryAuthenticationStatusCodeMapper;
import stubidp.saml.hub.hub.transformers.inbound.providers.DecoratedSamlResponseToIdaResponseIssuedByIdpTransformer;
import stubidp.saml.hub.hub.validators.authnrequest.ConcurrentMapIdExpirationCache;
import stubidp.saml.hub.hub.validators.response.common.ResponseSizeValidator;
import stubidp.saml.hub.hub.validators.response.idp.components.EncryptedResponseFromIdpValidator;
import stubidp.saml.hub.hub.validators.response.idp.components.ResponseAssertionsFromIdpValidator;
import stubidp.saml.hub.metadata.IdpMetadataPublicKeyStore;
import stubidp.saml.metadata.JerseyClientMetadataResolver;
import stubidp.saml.metadata.factories.MetadataSignatureTrustEngineFactory;
import stubidp.saml.security.AssertionDecrypter;
import stubidp.saml.security.CredentialFactorySignatureValidator;
import stubidp.saml.security.DecrypterFactory;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.security.IdaKeyStoreCredentialRetriever;
import stubidp.saml.security.MetadataBackedSignatureValidator;
import stubidp.saml.security.SamlAssertionsSignatureValidator;
import stubidp.saml.security.SamlMessageSignatureValidator;
import stubidp.saml.security.SigningCredentialFactory;
import stubidp.saml.security.validators.ValidatedResponse;
import stubidp.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;
import stubidp.saml.security.validators.issuer.IssuerValidator;
import stubidp.saml.security.validators.signature.SamlResponseSignatureValidator;
import stubidp.saml.serializers.deserializers.OpenSamlXMLObjectUnmarshaller;
import stubidp.saml.serializers.deserializers.StringToOpenSamlObjectTransformer;
import stubidp.saml.serializers.deserializers.parser.SamlObjectParser;
import stubidp.saml.serializers.deserializers.validators.Base64StringDecoder;
import stubidp.saml.serializers.deserializers.validators.NotNullSamlStringValidator;
import stubidp.saml.serializers.serializers.XmlObjectToBase64EncodedStringTransformer;
import stubidp.saml.utils.core.transformers.AuthnContextFactory;
import stubidp.saml.utils.hub.validators.StringSizeValidator;
import stubsp.stubsp.saml.response.eidas.EidasAttributeStatementAssertionValidator;
import stubsp.stubsp.saml.response.eidas.EidasAuthnResponseIssuerValidator;
import stubsp.stubsp.saml.response.eidas.InboundResponseFromCountry;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.text.MessageFormat.format;

/**
 * Be warned that this class does little to no validation and is just for testing the contents of a response
 */
public class SamlResponseDecrypter {

    // Manual Guice injection
    private final StringToOpenSamlObjectTransformer<Response> stringToOpenSamlObjectTransformer = new StringToOpenSamlObjectTransformer<>(new NotNullSamlStringValidator(),
            new Base64StringDecoder(),
            new ResponseSizeValidator(new StringSizeValidator()),
            new OpenSamlXMLObjectUnmarshaller<>(new SamlObjectParser()));

    private final Client client;
    private final URI assertionConsumerServices;
    private final boolean checkKeyInfo;

    private final URI idpMetadataUri;
    private final String spEntityId;
    private final IdaKeyStore spKeyStore;

    private final IdaKeyStore eidasKeyStore;
    private final Optional<URI> eidasMetadataUri;

    public SamlResponseDecrypter(Client client, URI idpMetadataUri, String spEntityId, Optional<URI> eidasMetadataUri, URI assertionConsumerServices, IdaKeyStore spKeyStore, IdaKeyStore eidasKeyStore) {
        this(client, idpMetadataUri, spEntityId, eidasMetadataUri, assertionConsumerServices, spKeyStore, eidasKeyStore, false);
    }

    public SamlResponseDecrypter(Client client, URI idpMetadataUri, String spEntityId, Optional<URI> eidasMetadataUri, URI assertionConsumerServices, IdaKeyStore spKeyStore, IdaKeyStore eidasKeyStore, boolean checkKeyInfo) {
        this.client = client;
        this.idpMetadataUri = idpMetadataUri;
        this.spEntityId = spEntityId;
        this.eidasMetadataUri = eidasMetadataUri;
        this.assertionConsumerServices = assertionConsumerServices;
        this.spKeyStore = spKeyStore;
        this.eidasKeyStore = eidasKeyStore;
        this.checkKeyInfo = checkKeyInfo;
    }

    /**
     * Be warned that this method does little to no validation and is just for testing the contents of a response
     */
    public InboundResponseFromIdp decryptSaml(String samlResponse) {
        final JerseyClientMetadataResolver jerseyClientMetadataResolver = getMetadataResolver(idpMetadataUri);
        final SigningCredentialFactory credentialFactory = new SigningCredentialFactory(new AuthnResponseKeyStore(new IdpMetadataPublicKeyStore(jerseyClientMetadataResolver)));
        DecoratedSamlResponseToIdaResponseIssuedByIdpTransformer decoratedSamlResponseToIdaResponseIssuedByIdpTransformer
                = buildDecoratedSamlResponseToIdaResponseIssuedByIdpTransformer(credentialFactory, spKeyStore);

        final org.opensaml.saml.saml2.core.Response response = stringToOpenSamlObjectTransformer.apply(samlResponse);
        if(checkKeyInfo) {
            validateKeyInfoPresent(response);
        } else {
            validateKeyInfoNotPresent(response);
        }
        return decoratedSamlResponseToIdaResponseIssuedByIdpTransformer.apply(response);
    }

    private void validateKeyInfoNotPresent(SignableSAMLObject signableSAMLObject) {
        if (Objects.nonNull(signableSAMLObject.getSignature().getKeyInfo())) {
            throw new RuntimeException("KeyInfo was not null");
        }
    }

    private void validateKeyInfoPresent(SignableSAMLObject signableSAMLObject) {
        if (Objects.isNull(signableSAMLObject.getSignature().getKeyInfo())) {
            throw new RuntimeException("KeyInfo cannot be null");
        }
        if (signableSAMLObject.getSignature().getKeyInfo().getX509Datas().isEmpty()) {
            throw new RuntimeException("no x509 data found");
        }
        if (signableSAMLObject.getSignature().getKeyInfo().getX509Datas().get(0).getX509Certificates().isEmpty()) {
            throw new RuntimeException("no x509 certificates found in x509 data");
        }
        if (Objects.isNull(signableSAMLObject.getSignature().getKeyInfo().getX509Datas().get(0).getX509Certificates().get(0))) {
            throw new RuntimeException("x509 certificate was invalid");
        }
    }

    private JerseyClientMetadataResolver getMetadataResolver(URI metadataUri) {
        final JerseyClientMetadataResolver jerseyClientMetadataResolver = new JerseyClientMetadataResolver(null,  client, metadataUri);
        try {
            // a parser pool needs to be provided
            BasicParserPool pool = new BasicParserPool();
            pool.initialize();
            jerseyClientMetadataResolver.setParserPool(pool);
            jerseyClientMetadataResolver.setId("SamlDecrypter.MetadataResolver"+UUID.randomUUID());
            jerseyClientMetadataResolver.initialize();
            jerseyClientMetadataResolver.refresh();
        } catch (ComponentInitializationException | ResolverException e) {
            e.printStackTrace();
        }
        return jerseyClientMetadataResolver;
    }

    public InboundResponseFromCountry decryptEidasSaml(String samlResponse) {
        return decryptEidasSaml(samlResponse, true);
    }

    public InboundResponseFromCountry decryptEidasSamlUnsignedAssertions(String samlResponse) {
        return decryptEidasSaml(samlResponse, false);
    }

    /**
     * Be warned that this method does little to no validation and is just for testing the contents of a response
     */
    private InboundResponseFromCountry decryptEidasSaml(String samlResponse, boolean signedAssertions) {
        Response response = stringToOpenSamlObjectTransformer.apply(samlResponse);
        validateKeyInfoPresent(response);
        ValidatedResponse validatedResponse = validateResponse(response);
        AssertionDecrypter assertionDecrypter = getAES256WithGCMAssertionDecrypter(eidasKeyStore);
        List<Assertion> assertions = assertionDecrypter.decryptAssertions(validatedResponse);
        Optional<Assertion> validatedIdentityAssertion = validateAssertion(validatedResponse, assertions, signedAssertions);

        return new InboundResponseFromCountry(response.getIssuer().getValue(),
                validatedIdentityAssertion.get(),
                response.getStatus()
        );
    }

    private AssertionDecrypter getAES256WithGCMAssertionDecrypter(IdaKeyStore keyStore) {
        IdaKeyStoreCredentialRetriever idaKeyStoreCredentialRetriever = new IdaKeyStoreCredentialRetriever(keyStore);
        Decrypter decrypter = new DecrypterFactory().createDecrypter(idaKeyStoreCredentialRetriever.getDecryptingCredentials());
        return new AssertionDecrypter(
                new EncryptionAlgorithmValidator(
                    Set.of(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256, EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256_GCM),
                    Set.of(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP)),
                decrypter
        );
    }

    private ValidatedResponse validateResponse(Response response) {
        SamlResponseSignatureValidator samlResponseSignatureValidator = new SamlResponseSignatureValidator(getSamlMessageSignatureValidator(spEntityId));
        final ValidatedResponse validatedResponse = samlResponseSignatureValidator.validate(response, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        new DestinationValidator(UriBuilder.fromUri(assertionConsumerServices).replacePath(null).build(), assertionConsumerServices.getPath()).validate(response.getDestination());
        return validatedResponse;
    }

    private void getValidatedAssertion(List<Assertion> decryptedAssertions, boolean signedAssertions) {
        SamlAssertionsSignatureValidator samlAssertionsSignatureValidator = new SamlAssertionsSignatureValidator(getSamlMessageSignatureValidator(spEntityId));
        try {
            samlAssertionsSignatureValidator.validate(decryptedAssertions, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        } catch(SamlTransformationErrorException e) {
            if(signedAssertions) {
                throw e;
            }
            if(!e.getMessage().contains("Message has no signature")) {
                throw e;
            }
        }
    }

    private void responseAssertionFromCountryValidatorValidate(ValidatedResponse validatedResponse, Assertion validatedIdentityAssertion, boolean signedAssertions) {

        new IdentityProviderAssertionValidator(
                new IssuerValidator(),
                new AssertionSubjectValidator(),
                new AssertionAttributeStatementValidator(),
                new AssertionSubjectConfirmationValidator(),
                signedAssertions
        ).validate(validatedIdentityAssertion, validatedResponse.getInResponseTo(), assertionConsumerServices.toASCIIString());

        if (validatedResponse.isSuccess()) {

            if (validatedIdentityAssertion.getAuthnStatements().size() > 1) {
                SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.multipleAuthnStatements();
                throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
            }

            new EidasAttributeStatementAssertionValidator().validate(validatedIdentityAssertion);
            new AuthnStatementAssertionValidator(
                    new DuplicateAssertionValidatorImpl(new ConcurrentMapIdExpirationCache<>(new ConcurrentHashMap<>()))
            ).validate(validatedIdentityAssertion);
            new EidasAuthnResponseIssuerValidator().validate(validatedResponse, validatedIdentityAssertion);
        }
    }

    private SamlMessageSignatureValidator getSamlMessageSignatureValidator(String entityId) {
        return Optional.of(getMetadataResolver(eidasMetadataUri.get()))
                .map(m -> {
                    try {
                        return new MetadataSignatureTrustEngineFactory().createSignatureTrustEngine(m);
                    } catch (ComponentInitializationException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(MetadataBackedSignatureValidator::withoutCertificateChainValidation)
                .map(SamlMessageSignatureValidator::new)
                .orElseThrow(() -> new SamlTransformationErrorException(format("Unable to find metadata resolver for entity Id {0}", entityId), Level.ERROR));
    }

    private Optional<Assertion> validateAssertion(ValidatedResponse validatedResponse, List<Assertion> decryptedAssertions, boolean signedAssertions) {
        getValidatedAssertion(decryptedAssertions, signedAssertions);
        Optional<Assertion> identityAssertion = decryptedAssertions.stream().findFirst();
        identityAssertion.ifPresent(assertion -> responseAssertionFromCountryValidatorValidate(validatedResponse, assertion, signedAssertions));
        return identityAssertion;
    }

    private DecoratedSamlResponseToIdaResponseIssuedByIdpTransformer buildDecoratedSamlResponseToIdaResponseIssuedByIdpTransformer(SigningCredentialFactory credentialFactory, IdaKeyStore keyStore) {
        IdaKeyStoreCredentialRetriever storeCredentialRetriever = new IdaKeyStoreCredentialRetriever(keyStore);
        return new DecoratedSamlResponseToIdaResponseIssuedByIdpTransformer(
                new IdaResponseFromIdpUnmarshaller(
                        new IdpIdaStatusUnmarshaller(),
                        new PassthroughAssertionUnmarshaller(new XmlObjectToBase64EncodedStringTransformer<>(), new AuthnContextFactory())),
                new SamlResponseSignatureValidator(new SamlMessageSignatureValidator(new CredentialFactorySignatureValidator(credentialFactory))),
                new AssertionDecrypter(new EncryptionAlgorithmValidator(), new DecrypterFactory().createDecrypter(storeCredentialRetriever.getDecryptingCredentials())),
                new SamlAssertionsSignatureValidator(new SamlMessageSignatureValidator(new CredentialFactorySignatureValidator(credentialFactory))),
                new EncryptedResponseFromIdpValidator<>(new SamlStatusToCountryAuthenticationStatusCodeMapper()),
                new DestinationValidator(UriBuilder.fromUri(assertionConsumerServices).replacePath(null).build(), assertionConsumerServices.getPath()),
                new ResponseAssertionsFromIdpValidator(
                        new IdentityProviderAssertionValidator(
                                new IssuerValidator(),
                                new AssertionSubjectValidator(),
                                new AssertionAttributeStatementValidator(),
                                new AssertionSubjectConfirmationValidator()),
                        new MatchingDatasetAssertionValidator(new DuplicateAssertionValidatorImpl(new ConcurrentMapIdExpirationCache<>(new ConcurrentHashMap<>()))),
                        new AuthnStatementAssertionValidator(new DuplicateAssertionValidatorImpl(new ConcurrentMapIdExpirationCache<>(new ConcurrentHashMap<>()))),
                        new IPAddressValidator(),
                        spEntityId));
    }
}
