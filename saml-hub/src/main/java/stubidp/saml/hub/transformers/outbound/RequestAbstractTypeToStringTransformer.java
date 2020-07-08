package stubidp.saml.hub.transformers.outbound;

import org.opensaml.saml.saml2.core.RequestAbstractType;
import stubidp.saml.hub.transformers.outbound.decorators.SigningRequestAbstractTypeSignatureCreator;
import stubidp.saml.serializers.serializers.XmlObjectToBase64EncodedStringTransformer;
import stubidp.saml.utils.core.transformers.outbound.decorators.SamlSignatureSigner;

import javax.inject.Inject;
import java.util.function.Function;

public class RequestAbstractTypeToStringTransformer<TInput extends RequestAbstractType> implements Function<TInput, String> {

    private final SigningRequestAbstractTypeSignatureCreator<TInput> signatureCreator;
    private final SamlSignatureSigner<TInput> samlSignatureSigner;
    private final XmlObjectToBase64EncodedStringTransformer<TInput> xmlObjectToBase64EncodedStringTransformer;

    @Inject
    public RequestAbstractTypeToStringTransformer(
            final SigningRequestAbstractTypeSignatureCreator<TInput> signatureCreator,
            final SamlSignatureSigner<TInput> samlSignatureSigner,
            final XmlObjectToBase64EncodedStringTransformer<TInput> xmlObjectToBase64EncodedStringTransformer) {

        this.signatureCreator = signatureCreator;
        this.samlSignatureSigner = samlSignatureSigner;
        this.xmlObjectToBase64EncodedStringTransformer = xmlObjectToBase64EncodedStringTransformer;
    }

    @Override
    public String apply(final TInput input) {
        final TInput requestWithSignature = signatureCreator.addUnsignedSignatureTo(input);

        final TInput signedRequest = samlSignatureSigner.sign(requestWithSignature);

        return xmlObjectToBase64EncodedStringTransformer.apply(signedRequest);
    }

}
