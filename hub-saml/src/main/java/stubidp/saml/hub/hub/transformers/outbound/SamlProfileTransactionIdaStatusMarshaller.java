package stubidp.saml.hub.hub.transformers.outbound;

import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.domain.DetailedStatusCode;
import stubidp.saml.domain.assertions.TransactionIdaStatus;
import stubidp.saml.utils.core.transformers.outbound.IdaStatusMarshaller;

import javax.inject.Inject;
import java.util.Map;

/**
 * This class is the same as TransactionIdaStatusMarshaller except that TransactionIdaStatus.NoMatchingServiceMatchFromHub
 * is mapped to DetailedStatusCode.SamlProfileNoMatchingServiceMatchFromHub. This is because the saml profile specifies that a
 * no-match (which has no Assertions) should have Status of Responder. TransactionIdaStatusMarshaller is kept in the package
 * for backwards compatibility (RPs receive Success:no-match at time of writing) but the goal is to deprecate it
 * (but this might take years).
 */
public class SamlProfileTransactionIdaStatusMarshaller extends IdaStatusMarshaller<TransactionIdaStatus> {

    private static final Map<TransactionIdaStatus, DetailedStatusCode> REST_TO_SAML_CODES =
            Map.<TransactionIdaStatus, DetailedStatusCode>ofEntries(
                    Map.entry(TransactionIdaStatus.Success, DetailedStatusCode.Success),
                    Map.entry(TransactionIdaStatus.NoAuthenticationContext, DetailedStatusCode.NoAuthenticationContext),
                    Map.entry(TransactionIdaStatus.NoMatchingServiceMatchFromHub, DetailedStatusCode.SamlProfileNoMatchingServiceMatchFromHub),
                    Map.entry(TransactionIdaStatus.AuthenticationFailed, DetailedStatusCode.AuthenticationFailed),
                    Map.entry(TransactionIdaStatus.RequesterError, DetailedStatusCode.RequesterErrorFromIdpAsSentByHub)
            );

    @Inject
    public SamlProfileTransactionIdaStatusMarshaller(OpenSamlXmlObjectFactory samlObjectFactory) {
        super(samlObjectFactory);
    }


    @Override
    protected DetailedStatusCode getDetailedStatusCode(TransactionIdaStatus originalStatus) {
        return REST_TO_SAML_CODES.get(originalStatus);
    }
}
