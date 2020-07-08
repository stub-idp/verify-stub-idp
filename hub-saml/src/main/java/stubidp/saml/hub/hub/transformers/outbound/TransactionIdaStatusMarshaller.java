package stubidp.saml.hub.hub.transformers.outbound;

import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.domain.DetailedStatusCode;
import stubidp.saml.domain.assertions.TransactionIdaStatus;
import stubidp.saml.utils.core.transformers.outbound.IdaStatusMarshaller;

import javax.inject.Inject;
import java.util.Map;

public class TransactionIdaStatusMarshaller extends IdaStatusMarshaller<TransactionIdaStatus> {

    private static final Map<TransactionIdaStatus, DetailedStatusCode> REST_TO_SAML_CODES =
            Map.<TransactionIdaStatus, DetailedStatusCode>ofEntries(
                    Map.entry(TransactionIdaStatus.Success, DetailedStatusCode.Success),
                    Map.entry(TransactionIdaStatus.NoAuthenticationContext, DetailedStatusCode.NoAuthenticationContext),
                    Map.entry(TransactionIdaStatus.NoMatchingServiceMatchFromHub, DetailedStatusCode.NoMatchingServiceMatchFromHub),
                    Map.entry(TransactionIdaStatus.AuthenticationFailed, DetailedStatusCode.AuthenticationFailed),
                    Map.entry(TransactionIdaStatus.RequesterError, DetailedStatusCode.RequesterErrorFromIdpAsSentByHub)
            );

    @Inject
    public TransactionIdaStatusMarshaller(OpenSamlXmlObjectFactory samlObjectFactory) {
        super(samlObjectFactory);
    }


    @Override
    protected DetailedStatusCode getDetailedStatusCode(TransactionIdaStatus originalStatus) {
        return REST_TO_SAML_CODES.get(originalStatus);
    }
}
