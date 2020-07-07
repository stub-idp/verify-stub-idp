package stubidp.saml.test.metadata;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.metadata.AttributeAuthorityDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.support.SignatureException;
import stubidp.saml.test.builders.AttributeAuthorityDescriptorBuilder;
import stubidp.saml.test.builders.EntityDescriptorBuilder;
import stubidp.saml.test.builders.IdpSsoDescriptorBuilder;
import stubidp.saml.test.builders.KeyDescriptorBuilder;
import stubidp.saml.test.builders.KeyInfoBuilder;
import stubidp.saml.test.builders.SPSSODescriptorBuilder;
import stubidp.saml.test.builders.SignatureBuilder;
import stubidp.saml.test.builders.X509CertificateBuilder;
import stubidp.saml.test.builders.X509DataBuilder;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.test.devpki.TestEntityIds;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static java.util.Arrays.asList;

public class EntityDescriptorFactory {
    public static final String SIGNING_ONE = "signing_one";
    public static final String SIGNING_TWO = "signing_two";
    public static final String ENCRYPTION = "encryption";
    public static final String SIGNING_BAD = "signing_bad";
    private static final String SIGNING_USAGE = "SIGNING";
    private static final String ENCRYPTION_USAGE = "ENCRYPTION";

    public EntityDescriptor hubEntityDescriptor() {
        KeyDescriptor siginingKeyDescriptorOne = createKeyDescriptor(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, SIGNING_ONE, SIGNING_USAGE);
        KeyDescriptor siginingKeyDescriptorTwo = createKeyDescriptor(TestCertificateStrings.HUB_TEST_SECONDARY_PUBLIC_SIGNING_CERT, SIGNING_TWO, SIGNING_USAGE);
        KeyDescriptor encryptionKeyDescriptor = createKeyDescriptor(TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT, ENCRYPTION, ENCRYPTION_USAGE);
        SPSSODescriptor spssoDescriptor = SPSSODescriptorBuilder.anSpServiceDescriptor()
                .addKeyDescriptor(siginingKeyDescriptorOne)
                .addKeyDescriptor(siginingKeyDescriptorTwo)
                .addKeyDescriptor(encryptionKeyDescriptor)
                .withoutDefaultSigningKey()
                .withoutDefaultEncryptionKey().build();
        try {
            return EntityDescriptorBuilder.anEntityDescriptor()
                    .withEntityId(TestEntityIds.HUB_ENTITY_ID)
                    .addSpServiceDescriptor(spssoDescriptor)
                    .withIdpSsoDescriptor(null)
                    .withValidUntil(Instant.now().atZone(ZoneId.of("UTC")).plusWeeks(2).toInstant())
                    .withSignature(null)
                    .withoutSigning()
                    .build();
        } catch (MarshallingException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    public EntityDescriptor hubEntityDescriptorWithWrongUsageCertificates() {
        KeyDescriptor wrongUsageKeyDescriptorOne = createKeyDescriptor(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, SIGNING_ONE, ENCRYPTION_USAGE);
        KeyDescriptor wrongUsageKeyDescriptorTwo = createKeyDescriptor(TestCertificateStrings.HUB_TEST_SECONDARY_PUBLIC_SIGNING_CERT, SIGNING_TWO, ENCRYPTION_USAGE);
        KeyDescriptor encryptionKeyDescriptorThree = createKeyDescriptor(TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT, ENCRYPTION, ENCRYPTION_USAGE);
        SPSSODescriptor spssoDescriptor = SPSSODescriptorBuilder.anSpServiceDescriptor()
                .addKeyDescriptor(wrongUsageKeyDescriptorOne)
                .addKeyDescriptor(wrongUsageKeyDescriptorTwo)
                .addKeyDescriptor(encryptionKeyDescriptorThree)
                .withoutDefaultSigningKey()
                .withoutDefaultEncryptionKey().build();
        try {
            return EntityDescriptorBuilder.anEntityDescriptor()
                    .withEntityId(TestEntityIds.HUB_ENTITY_ID)
                    .addSpServiceDescriptor(spssoDescriptor)
                    .withIdpSsoDescriptor(null)
                    .withValidUntil(Instant.now().atZone(ZoneId.of("UTC")).plusWeeks(2).toInstant())
                    .withSignature(null)
                    .withoutSigning()
                    .build();
        } catch (MarshallingException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    private KeyDescriptor createKeyDescriptor(final String testCertificateString, final String keyName, final String usage) {
        X509Certificate x509Certificate = X509CertificateBuilder.aX509Certificate().withCert(testCertificateString).build();
        X509Data x509Data = X509DataBuilder.aX509Data().withX509Certificate(x509Certificate).build();
        KeyInfo signing = KeyInfoBuilder.aKeyInfo().withKeyName(keyName).withX509Data(x509Data).build();
        return KeyDescriptorBuilder.aKeyDescriptor().withUse(usage).withKeyInfo(signing).build();
    }

    public EntityDescriptor badHubEntityDescriptor() {
        KeyDescriptor siginingKeyDescriptorOne = createKeyDescriptor(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, SIGNING_ONE, SIGNING_USAGE);
        KeyDescriptor siginingKeyDescriptorTwo = createKeyDescriptor(TestCertificateStrings.HUB_TEST_SECONDARY_PUBLIC_SIGNING_CERT, SIGNING_TWO, SIGNING_USAGE);
        KeyDescriptor encryptionKeyDescriptor = createKeyDescriptor(TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT, ENCRYPTION, ENCRYPTION_USAGE);
        KeyDescriptor siginingKeyDescriptorBad = createKeyDescriptor(TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT, SIGNING_BAD, SIGNING_USAGE);
        SPSSODescriptor spssoDescriptor = SPSSODescriptorBuilder.anSpServiceDescriptor()
                                                                .addKeyDescriptor(siginingKeyDescriptorOne)
                                                                .addKeyDescriptor(siginingKeyDescriptorTwo)
                                                                .addKeyDescriptor(encryptionKeyDescriptor)
                                                                .addKeyDescriptor(siginingKeyDescriptorBad)
                                                                .withoutDefaultSigningKey()
                                                                .withoutDefaultEncryptionKey().build();
        try {
            return EntityDescriptorBuilder.anEntityDescriptor()
                                          .withEntityId(TestEntityIds.HUB_ENTITY_ID)
                                          .addSpServiceDescriptor(spssoDescriptor)
                                          .withIdpSsoDescriptor(null)
                                          .withValidUntil(Instant.now().atZone(ZoneId.of("UTC")).plusWeeks(2).toInstant())
                                          .withSignature(null)
                                          .withoutSigning()
                                          .build();
        } catch (MarshallingException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    public EntityDescriptor idpEntityDescriptor(String idpEntityId) {
        try {
            return getEntityDescriptorBuilder(idpEntityId)
                    .withSignature(null)
                    .withoutSigning()
                    .withValidUntil(Instant.now().atZone(ZoneId.of("UTC")).plusWeeks(2).toInstant())
                    .build();
        } catch (MarshallingException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    public EntityDescriptor signedIdpEntityDescriptor(String idpEntityId, Credential signingCredential, Instant validUntil) {
        Signature signature = SignatureBuilder.aSignature().withX509Data(TestCertificateStrings.PUBLIC_SIGNING_CERTS.get(idpEntityId)).withSigningCredential(signingCredential).build();
        try {
            return getEntityDescriptorBuilder(idpEntityId)
                    .withSignature(signature)
                    .withValidUntil(validUntil)
                    .build();
        } catch (MarshallingException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    private EntityDescriptorBuilder getEntityDescriptorBuilder(String idpEntityId) {
        KeyDescriptor keyDescriptor = buildKeyDescriptor(idpEntityId);
        IDPSSODescriptor idpssoDescriptor = IdpSsoDescriptorBuilder.anIdpSsoDescriptor().addKeyDescriptor(keyDescriptor).withoutDefaultSigningKey().build();

        return EntityDescriptorBuilder.anEntityDescriptor()
                .withEntityId(idpEntityId)
                .withIdpSsoDescriptor(idpssoDescriptor)
                .setAddDefaultSpServiceDescriptor(false);
    }

    public EntityDescriptor attributeAuthorityEntityDescriptor(String attributeAuthorityEntityId) {
        KeyDescriptor keyDescriptor = buildKeyDescriptor(attributeAuthorityEntityId);
        AttributeAuthorityDescriptor attributeAuthorityDescriptor = AttributeAuthorityDescriptorBuilder.anAttributeAuthorityDescriptor().addKeyDescriptor(keyDescriptor).build();
        try {
            return EntityDescriptorBuilder.anEntityDescriptor()
                    .withEntityId(attributeAuthorityEntityId)
                    .withIdpSsoDescriptor(null)
                    .withValidUntil(Instant.now().atZone(ZoneId.of("UTC")).plusWeeks(2).toInstant())
                    .withSignature(null)
                    .withoutSigning()
                    .withAttributeAuthorityDescriptor(attributeAuthorityDescriptor)
                    .setAddDefaultSpServiceDescriptor(false)
                    .build();
        } catch (MarshallingException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    public List<EntityDescriptor> defaultEntityDescriptors() {
        return asList(
            hubEntityDescriptor(),
            idpEntityDescriptor(TestEntityIds.STUB_IDP_ONE),
            idpEntityDescriptor(TestEntityIds.STUB_IDP_TWO),
            idpEntityDescriptor(TestEntityIds.STUB_IDP_THREE),
            idpEntityDescriptor(TestEntityIds.STUB_IDP_FOUR)
        );
    }

    private KeyDescriptor buildKeyDescriptor(String entityId) {
        String certificate = TestCertificateStrings.PUBLIC_SIGNING_CERTS.get(entityId);
        X509Certificate x509Certificate = X509CertificateBuilder.aX509Certificate().withCert(certificate).build();
        X509Data build = X509DataBuilder.aX509Data().withX509Certificate(x509Certificate).build();
        KeyInfo signing_one = KeyInfoBuilder.aKeyInfo().withKeyName("signing_one").withX509Data(build).build();
        return KeyDescriptorBuilder.aKeyDescriptor().withKeyInfo(signing_one).build();
    }
}
