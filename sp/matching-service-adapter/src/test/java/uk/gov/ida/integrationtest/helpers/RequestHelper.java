package uk.gov.ida.integrationtest.helpers;

import org.apache.commons.codec.binary.Base64;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import stubidp.saml.hub.core.transformers.outbound.decorators.SamlAttributeQueryAssertionEncrypter;
import stubidp.saml.hub.transformers.outbound.AttributeQueryToElementTransformer;
import stubidp.saml.hub.transformers.outbound.decorators.SamlAttributeQueryAssertionSignatureSigner;
import stubidp.saml.hub.transformers.outbound.decorators.SigningRequestAbstractTypeSignatureCreator;
import stubidp.saml.security.EncrypterFactory;
import stubidp.saml.security.EncryptionCredentialFactory;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.security.IdaKeyStoreCredentialRetriever;
import stubidp.saml.security.SignatureFactory;
import stubidp.saml.serializers.deserializers.ElementToOpenSamlXMLObjectTransformer;
import stubidp.saml.serializers.deserializers.parser.SamlObjectParser;
import stubidp.saml.serializers.serializers.XmlObjectToElementTransformer;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.transformers.outbound.decorators.SamlSignatureSigner;
import stubidp.test.security.HardCodedKeyStore;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.test.devpki.TestEntityIds;
import stubidp.utils.security.security.PrivateKeyFactory;
import stubidp.utils.security.security.PublicKeyFactory;
import stubidp.utils.security.security.X509CertificateFactory;
import uk.gov.ida.matchingserviceadapter.rest.soap.SamlElementType;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;

import javax.ws.rs.client.Entity;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collections;

import static javax.ws.rs.core.MediaType.TEXT_XML_TYPE;
import static stubidp.test.devpki.TestEntityIds.TEST_RP_MS;

public class RequestHelper {

    public static Response makeAttributeQueryRequest(String uri, AttributeQuery attributeQuery, SignatureAlgorithm signatureAlgorithm, DigestAlgorithm digestAlgorithm, String hubEntityId) {
        ElementToOpenSamlXMLObjectTransformer<Response> responseElementToOpenSamlXMLObjectTransformer = new ElementToOpenSamlXMLObjectTransformer<>(new SamlObjectParser());

        Document attributeQueryDocument = getAttributeQueryToElementTransformer(signatureAlgorithm, digestAlgorithm, hubEntityId).apply(attributeQuery).getOwnerDocument();
        Document soapResponse = JerseyClientBuilder.createClient().target(uri).request()
                .post(Entity.entity(attributeQueryDocument, TEXT_XML_TYPE))
                .readEntity(Document.class);

        Element soapMessage = new SoapMessageManager().unwrapSoapMessage(soapResponse, SamlElementType.Response);
        return responseElementToOpenSamlXMLObjectTransformer.apply(soapMessage);
    }

    public static AttributeQueryToElementTransformer getAttributeQueryToElementTransformer(SignatureAlgorithm signatureAlgorithm, DigestAlgorithm digestAlgorithm, String hubEntityId) {
        PublicKeyFactory publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());
        PrivateKey privateKey = new PrivateKeyFactory().createPrivateKey(Base64.decodeBase64(TestCertificateStrings.PRIVATE_SIGNING_KEYS.get(TestEntityIds.HUB_ENTITY_ID)));
        PublicKey publicKey = publicKeyFactory.createPublicKey(TestCertificateStrings.getPrimaryPublicEncryptionCert(TestEntityIds.HUB_ENTITY_ID));

        PrivateKey privateEncryptionKey = new PrivateKeyFactory().createPrivateKey(Base64.decodeBase64(TestCertificateStrings.HUB_TEST_PRIVATE_ENCRYPTION_KEY));
        PublicKey publicEncryptionKey = publicKeyFactory.createPublicKey(TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT);

        KeyPair encryptionKeyPair = new KeyPair(publicEncryptionKey, privateEncryptionKey);

        KeyPair signingKeyPair = new KeyPair(publicKey, privateKey);
        IdaKeyStore keyStore = new IdaKeyStore(signingKeyPair, Collections.singletonList(encryptionKeyPair));

        IdaKeyStoreCredentialRetriever privateCredentialFactory = new IdaKeyStoreCredentialRetriever(keyStore);
        return new AttributeQueryToElementTransformer(
                new SigningRequestAbstractTypeSignatureCreator<>(new SignatureFactory(privateCredentialFactory, signatureAlgorithm, digestAlgorithm)),
                new SamlAttributeQueryAssertionSignatureSigner(privateCredentialFactory, new OpenSamlXmlObjectFactory(), hubEntityId),
                new SamlSignatureSigner<>(),
                new XmlObjectToElementTransformer<>(),
                new SamlAttributeQueryAssertionEncrypter(new EncryptionCredentialFactory(new HardCodedKeyStore(hubEntityId)), new EncrypterFactory(), requestId -> TEST_RP_MS)
        );
    }
}
