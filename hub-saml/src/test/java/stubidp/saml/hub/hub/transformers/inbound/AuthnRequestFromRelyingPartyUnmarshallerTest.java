package stubidp.saml.hub.hub.transformers.inbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Extensions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.impl.AttributeBuilder;
import org.opensaml.saml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.saml.saml2.core.impl.ExtensionsBuilder;
import org.opensaml.saml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml.saml2.encryption.Encrypter;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.xmlsec.signature.impl.SignatureBuilder;
import org.opensaml.xmlsec.signature.impl.SignatureImpl;
import stubidp.saml.extensions.IdaSamlBootstrap;
import stubidp.saml.extensions.extensions.versioning.Version;
import stubidp.saml.extensions.extensions.versioning.VersionImpl;
import stubidp.saml.extensions.extensions.versioning.application.ApplicationVersion;
import stubidp.saml.extensions.extensions.versioning.application.ApplicationVersionImpl;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.hub.hub.domain.AuthnRequestFromRelyingParty;
import stubidp.saml.security.DecrypterFactory;
import stubidp.saml.security.EncrypterFactory;
import stubidp.utils.security.security.PrivateKeyFactory;
import stubidp.utils.security.security.PublicKeyFactory;
import stubidp.utils.security.security.X509CertificateFactory;

import java.net.URI;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.test.devpki.TestCertificateStrings.HUB_TEST_PRIVATE_ENCRYPTION_KEY;
import static stubidp.test.devpki.TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT;

public class AuthnRequestFromRelyingPartyUnmarshallerTest extends OpenSAMLRunner {

    static {
        IdaSamlBootstrap.bootstrap();
    }

    private static Encrypter encrypter;

    private AuthnRequestFromRelyingPartyUnmarshaller unmarshaller;

    @BeforeEach
    public void setUp() {
        final BasicCredential basicCredential = createBasicCredential();
        encrypter = new EncrypterFactory().createEncrypter(basicCredential);

        unmarshaller = new AuthnRequestFromRelyingPartyUnmarshaller(new DecrypterFactory().createDecrypter(List.of(basicCredential)));
    }

    @Test
    public void fromSamlMessage_shouldMapAuthnRequestToAuthnRequestFromRelyingParty() throws Exception {
        Instant issueInstant = Instant.now();
        SignatureImpl signature = new SignatureBuilder().buildObject();

        AuthnRequest authnRequest = new AuthnRequestBuilder().buildObject();
        authnRequest.setID("some-id");
        Issuer issuer = new IssuerBuilder().buildObject();
        issuer.setValue("some-service-entity-id");
        authnRequest.setIssuer(issuer);
        authnRequest.setIssueInstant(issueInstant);
        authnRequest.setDestination("http://example.local");
        authnRequest.setForceAuthn(true);
        authnRequest.setAssertionConsumerServiceURL("some-url");
        authnRequest.setAssertionConsumerServiceIndex(5);
        authnRequest.setSignature(signature);
        authnRequest.setExtensions(createApplicationVersionExtensions("some-version"));

        AuthnRequestFromRelyingParty authnRequestFromRelyingParty = unmarshaller.fromSamlMessage(authnRequest);
        AuthnRequestFromRelyingParty expected = new AuthnRequestFromRelyingParty(
            "some-id",
            "some-service-entity-id",
            issueInstant,
            URI.create("http://example.local"),
            Optional.of(true),
            Optional.of(URI.create("some-url")),
            Optional.of(5),
            Optional.of(signature),
            Optional.of("some-version")
        );

        assertThat(authnRequestFromRelyingParty).isEqualTo(expected);
    }

    @Test
    public void fromSamlMessage_shouldNotComplainWhenThereIsNoExtensionsElement() {
        AuthnRequest authnRequest = new AuthnRequestBuilder().buildObject();
        authnRequest.setIssuer(new IssuerBuilder().buildObject());
        authnRequest.setDestination("http://example.local");

        AuthnRequestFromRelyingParty authnRequestFromRelyingParty = unmarshaller.fromSamlMessage(authnRequest);

        assertThat(authnRequestFromRelyingParty.getVerifyServiceProviderVersion()).isEqualTo(Optional.empty());
    }

    @Test
    public void fromSamlMessage_shouldNotComplainWhenExceptionDuringDecryption() throws Exception {
        AuthnRequest authnRequest = new AuthnRequestBuilder().buildObject();
        authnRequest.setIssuer(new IssuerBuilder().buildObject());
        authnRequest.setDestination("http://example.local");
        authnRequest.setExtensions(createApplicationVersionExtensions(null));

        AuthnRequestFromRelyingParty authnRequestFromRelyingParty = unmarshaller.fromSamlMessage(authnRequest);

        assertThat(authnRequestFromRelyingParty.getVerifyServiceProviderVersion()).isEqualTo(Optional.empty());
    }

    private Extensions createApplicationVersionExtensions(String version) throws Exception {
        Extensions extensions = new ExtensionsBuilder().buildObject();
        Attribute versionsAttribute = new AttributeBuilder().buildObject();
        versionsAttribute.setName("Versions");
        versionsAttribute.getAttributeValues().add(createApplicationVersion(version));
        extensions.getUnknownXMLObjects().add(encrypter.encrypt(versionsAttribute));
        return extensions;
    }

    private Version createApplicationVersion(String versionNumber) {
        ApplicationVersion applicationVersion = new ApplicationVersionImpl();
        applicationVersion.setValue(versionNumber);
        return new VersionImpl() {{
            setApplicationVersion(applicationVersion);
        }};
    }

    private BasicCredential createBasicCredential() {
        final PublicKey publicKey = new PublicKeyFactory(new X509CertificateFactory()).createPublicKey(HUB_TEST_PUBLIC_ENCRYPTION_CERT);
        PrivateKey privateKey = new PrivateKeyFactory().createPrivateKey(Base64.getMimeDecoder().decode(HUB_TEST_PRIVATE_ENCRYPTION_KEY));
        return new BasicCredential(publicKey, privateKey);
    }
}