package uk.gov.ida.integrationTest.support;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.support.SignatureException;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.domain.SamlStatusCode;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.TestEntityIds;
import uk.gov.ida.saml.core.test.builders.AssertionBuilder;
import uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder;
import uk.gov.ida.saml.core.test.builders.IssuerBuilder;
import uk.gov.ida.saml.core.test.builders.MatchingDatasetAttributeStatementBuilder_1_1;
import uk.gov.ida.saml.core.test.builders.ResponseBuilder;
import uk.gov.ida.saml.core.test.builders.SignatureBuilder;
import uk.gov.ida.saml.core.test.builders.StatusBuilder;
import uk.gov.ida.saml.core.test.builders.StatusCodeBuilder;
import uk.gov.ida.saml.core.test.builders.SubjectBuilder;
import uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder;
import uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder;
import uk.gov.ida.saml.serializers.XmlObjectToBase64EncodedStringTransformer;

import java.util.UUID;

public class HubResponseFactory {

    private HubResponseFactory() {}

    public static String getSignedNoMatchResponse(String status) throws MarshallingException, SignatureException {
        ResponseBuilder response = ResponseBuilder.aResponse().withDestination("/SAML2/SSO/Response/POST").withNoDefaultAssertion();
        Issuer issuer = IssuerBuilder.anIssuer().withIssuerId(TestEntityIds.HUB_ENTITY_ID).build();
        Credential hubSigningCredential = new TestCredentialFactory(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY).getSigningCredential();
        response.withSigningCredential(hubSigningCredential).withIssuer(issuer);
        response.withStatus(
                StatusBuilder.aStatus().withStatusCode(
                        StatusCodeBuilder.aStatusCode().withValue(status).withSubStatusCode(
                                StatusCodeBuilder.aStatusCode().withValue(SamlStatusCode.NO_MATCH).build()).build()).build());

        XmlObjectToBase64EncodedStringTransformer<XMLObject> transformer = new XmlObjectToBase64EncodedStringTransformer<>();
        return transformer.apply(response.build());
    }

    public static String getUnsignedNoMatchResponse() throws MarshallingException, SignatureException {
        ResponseBuilder response = ResponseBuilder.aResponse().withDestination("/SAML2/SSO/Response/POST").withNoDefaultAssertion();
        response.withStatus(
                StatusBuilder.aStatus().withStatusCode(
                        StatusCodeBuilder.aStatusCode().withValue(StatusCode.RESPONDER).withSubStatusCode(
                                StatusCodeBuilder.aStatusCode().withValue(SamlStatusCode.NO_MATCH).build()).build()).build());

        XmlObjectToBase64EncodedStringTransformer<XMLObject> transformer = new XmlObjectToBase64EncodedStringTransformer<>();
        return transformer.apply(response.build());
    }

    public static String getSignedResponse(String hashedPid) throws MarshallingException, SignatureException {
        return getResponse(true, false, hashedPid);
    }

    public static String getUnsignedResponse(String hashedPid) throws MarshallingException, SignatureException {
        return getResponse(false, false, hashedPid);
    }

    public static String getUserAccountCreationResponse() throws MarshallingException, SignatureException {
        return getResponse(true, true, UUID.randomUUID().toString());
    }

    private static String getResponse(boolean signed, boolean addUserCreationAttributes, String hashedPid) throws MarshallingException, SignatureException {
        String requestId = "a-request";
        AuthnStatement authnStatement = AuthnStatementBuilder.anAuthnStatement().build();
        final Subject mdsAssertionSubject = SubjectBuilder.aSubject().withNameId(buildNameID(hashedPid)).withSubjectConfirmation(SubjectConfirmationBuilder.aSubjectConfirmation().withSubjectConfirmationData(SubjectConfirmationDataBuilder.aSubjectConfirmationData().withInResponseTo(requestId).build()).build()).build();
        final AttributeStatement matchingDatasetAttributeStatement = MatchingDatasetAttributeStatementBuilder_1_1.aMatchingDatasetAttributeStatement_1_1().build();
        TestCredentialFactory msaSigningCredentialFactory = new TestCredentialFactory(TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT, TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY);
        final Credential signingCredential = msaSigningCredentialFactory.getSigningCredential();
        final Credential encryptingCredential = new TestCredentialFactory(TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT, TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY).getEncryptingCredential();
        ResponseBuilder response = ResponseBuilder.aResponse()
                .withIssuer(null)
                .withDestination("/SAML2/SSO/Response/POST");

        if(addUserCreationAttributes) {
                response.addEncryptedAssertion(
                    AssertionBuilder.anAssertion()
                            .withId("assertion-1-id")
                            .addAuthnStatement(authnStatement)
                            .withIssuer(IssuerBuilder.anIssuer().withIssuerId(TestEntityIds.TEST_RP_MS).build())
                            .withSubject(mdsAssertionSubject)
                            .addAttributeStatement(matchingDatasetAttributeStatement)
                            .withSignature(SignatureBuilder.aSignature().withSigningCredential(signingCredential).build())
                            .buildWithEncrypterCredential(encryptingCredential));
        } else {
            response.addEncryptedAssertion(
                    AssertionBuilder.anAssertion()
                            .withId("assertion-1-id")
                            .addAuthnStatement(authnStatement)
                            .withIssuer(IssuerBuilder.anIssuer().withIssuerId(TestEntityIds.TEST_RP_MS).build())
                            .withSubject(mdsAssertionSubject)
                            .withSignature(SignatureBuilder.aSignature().withSigningCredential(signingCredential).build())
                            .buildWithEncrypterCredential(encryptingCredential));
        }

        if (signed) {
            Issuer issuer = IssuerBuilder.anIssuer().withIssuerId(TestEntityIds.HUB_ENTITY_ID).build();
            Credential hubSigningCredential = new TestCredentialFactory(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY).getSigningCredential();
            response.withSigningCredential(hubSigningCredential).withIssuer(issuer);
        } else {
            response.withIssuer(null).withoutSigning();
        }

        XmlObjectToBase64EncodedStringTransformer<XMLObject> transformer = new XmlObjectToBase64EncodedStringTransformer<>();
        return transformer.apply(response.build());
    }

    private static NameID buildNameID(String id) {
        NameID nameId = new OpenSamlXmlObjectFactory().createNameId(id);
        nameId.setFormat(NameIDType.PERSISTENT);

        return nameId;
    }
}
