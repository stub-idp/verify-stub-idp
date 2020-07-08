package stubidp.saml.hub.transformers.outbound;

import org.opensaml.saml.saml2.core.AttributeQuery;
import org.w3c.dom.Element;
import stubidp.saml.hub.transformers.outbound.decorators.SamlAttributeQueryAssertionSignatureSigner;
import stubidp.saml.hub.transformers.outbound.decorators.SigningRequestAbstractTypeSignatureCreator;
import stubidp.saml.serializers.serializers.XmlObjectToElementTransformer;
import stubidp.saml.hub.core.transformers.outbound.decorators.SamlAttributeQueryAssertionEncrypter;
import stubidp.saml.utils.core.transformers.outbound.decorators.SamlSignatureSigner;

import javax.inject.Inject;
import java.util.function.Function;

public class AttributeQueryToElementTransformer implements Function<AttributeQuery, Element> {

    private final SigningRequestAbstractTypeSignatureCreator<AttributeQuery> signatureCreator;
    private final SamlAttributeQueryAssertionSignatureSigner samlAttributeQueryAssertionSignatureSigner;
    private final SamlSignatureSigner<AttributeQuery> samlSignatureSigner;
    private final XmlObjectToElementTransformer<AttributeQuery> xmlObjectToElementTransformer;
    private final SamlAttributeQueryAssertionEncrypter encrypter;

    @Inject
    public AttributeQueryToElementTransformer(
            final SigningRequestAbstractTypeSignatureCreator<AttributeQuery> signatureCreator,
            final SamlAttributeQueryAssertionSignatureSigner samlAttributeQueryAssertionSignatureSigner,
            final SamlSignatureSigner<AttributeQuery> samlSignatureSigner,
            final XmlObjectToElementTransformer<AttributeQuery> xmlObjectToElementTransformer,
            final SamlAttributeQueryAssertionEncrypter encrypter) {

        this.signatureCreator = signatureCreator;
        this.samlSignatureSigner = samlSignatureSigner;
        this.samlAttributeQueryAssertionSignatureSigner = samlAttributeQueryAssertionSignatureSigner;
        this.xmlObjectToElementTransformer = xmlObjectToElementTransformer;
        this.encrypter = encrypter;
    }

    @Override
    public Element apply(final AttributeQuery attributeQuery) {
        final AttributeQuery queryWithSignature = signatureCreator.addUnsignedSignatureTo(attributeQuery);
        final AttributeQuery queryWithSignedAssertions =
                samlAttributeQueryAssertionSignatureSigner.signAssertions(queryWithSignature);
        final AttributeQuery queryWithEncryptedAssertions =
                encrypter.encryptAssertions(queryWithSignedAssertions);
        final AttributeQuery signedQuery =
                samlSignatureSigner.sign(queryWithEncryptedAssertions);
        return xmlObjectToElementTransformer.apply(signedQuery);
    }

}
