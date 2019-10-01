package stubidp.saml.utils.core.transformers.outbound;

import org.opensaml.saml.saml2.core.Response;
import stubidp.saml.serializers.serializers.XmlObjectToBase64EncodedStringTransformer;
import stubidp.saml.utils.core.transformers.outbound.decorators.ResponseSignatureCreator;
import stubidp.saml.utils.core.transformers.outbound.decorators.SamlResponseAssertionEncrypter;
import stubidp.saml.utils.core.transformers.outbound.decorators.SamlSignatureSigner;

import javax.inject.Inject;
import java.util.function.Function;

public class UnsignedAssertionResponseToSignedStringTransformer implements Function<Response, String> {

    private final XmlObjectToBase64EncodedStringTransformer<?> xmlObjectToBase64EncodedStringTransformer;
    private final SamlSignatureSigner<Response> samlSignatureSigner;
    private final SamlResponseAssertionEncrypter samlResponseAssertionEncrypter;
    private final ResponseSignatureCreator responseSignatureCreator;

    @Inject
    public UnsignedAssertionResponseToSignedStringTransformer(
            XmlObjectToBase64EncodedStringTransformer<?> xmlObjectToBase64EncodedStringTransformer,
            SamlSignatureSigner<Response> samlSignatureSigner,
            SamlResponseAssertionEncrypter samlResponseAssertionEncrypter,
            ResponseSignatureCreator responseSignatureCreator) {
        this.xmlObjectToBase64EncodedStringTransformer = xmlObjectToBase64EncodedStringTransformer;
        this.samlSignatureSigner = samlSignatureSigner;
        this.samlResponseAssertionEncrypter = samlResponseAssertionEncrypter;
        this.responseSignatureCreator = responseSignatureCreator;
    }

    @Override
    public String apply(final Response response) {
        final Response responseWithSignature = responseSignatureCreator.addUnsignedSignatureTo(response);
        final Response encryptedAssertionResponse = samlResponseAssertionEncrypter.encryptAssertions(responseWithSignature);
        final Response signedResponse = samlSignatureSigner.sign(encryptedAssertionResponse);
        return xmlObjectToBase64EncodedStringTransformer.apply(signedResponse);
    }
}