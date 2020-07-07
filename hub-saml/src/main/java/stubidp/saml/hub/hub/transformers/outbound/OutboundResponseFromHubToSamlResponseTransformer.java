package stubidp.saml.hub.hub.transformers.outbound;

import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.domain.response.OutboundResponseFromHub;
import stubidp.saml.domain.assertions.TransactionIdaStatus;
import stubidp.saml.utils.core.transformers.outbound.IdaResponseToSamlResponseTransformer;
import stubidp.saml.utils.core.transformers.outbound.IdaStatusMarshaller;

public class OutboundResponseFromHubToSamlResponseTransformer extends IdaResponseToSamlResponseTransformer<OutboundResponseFromHub> {

    private final IdaStatusMarshaller<TransactionIdaStatus> statusMarshaller;
    private final EncryptedAssertionUnmarshaller encryptedAssertionUnmarshaller;

    public OutboundResponseFromHubToSamlResponseTransformer(
            IdaStatusMarshaller<TransactionIdaStatus> statusMarshaller,
            OpenSamlXmlObjectFactory openSamlXmlObjectFactory,
            EncryptedAssertionUnmarshaller encryptedAssertionUnmarshaller) {

        super(openSamlXmlObjectFactory);

        this.statusMarshaller = statusMarshaller;
        this.encryptedAssertionUnmarshaller = encryptedAssertionUnmarshaller;
    }

    @Override
    protected void transformAssertions(OutboundResponseFromHub originalResponse, Response transformedResponse) {
        originalResponse
                .getEncryptedAssertions().stream()
                .map(encryptedAssertionUnmarshaller::transform)
                .forEach(transformedResponse.getEncryptedAssertions()::add);
    }

    @Override
    protected Status transformStatus(OutboundResponseFromHub originalResponse) {
        return statusMarshaller.toSamlStatus(originalResponse.getStatus());
    }

    @Override
    protected void transformDestination(OutboundResponseFromHub originalResponse, Response transformedResponse) {
        transformedResponse.setDestination(originalResponse.getDestination().toASCIIString());
    }
}
