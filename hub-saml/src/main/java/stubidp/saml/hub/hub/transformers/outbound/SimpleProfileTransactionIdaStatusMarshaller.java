package stubidp.saml.hub.hub.transformers.outbound;

import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.domain.DetailedStatusCode;
import stubidp.saml.utils.core.domain.TransactionIdaStatus;
import stubidp.saml.utils.core.transformers.outbound.IdaStatusMarshaller;

import javax.inject.Inject;
import java.util.AbstractMap;
import java.util.Map;

public class SimpleProfileTransactionIdaStatusMarshaller extends IdaStatusMarshaller<TransactionIdaStatus> {

    private static final Map<TransactionIdaStatus, DetailedStatusCode> REST_TO_SAML_CODES =
            Map.<TransactionIdaStatus, DetailedStatusCode>ofEntries(
                    Map.entry(TransactionIdaStatus.Success, DetailedStatusCode.Success),
                    Map.entry(TransactionIdaStatus.NoAuthenticationContext, DetailedStatusCode.NoAuthenticationContext),
                    // no-match is different in the simple saml profile: because there are no assertions included
                    // it should not be Success as it is in the standard signed responses from hub.  The Success
                    // response was changed to Responder for the simple saml profile (see the relatively opaque text
                    // in saml-profiles-2.0-os.pdf:544 that says a Success response MUST have an assertion
                    Map.entry(TransactionIdaStatus.NoMatchingServiceMatchFromHub, DetailedStatusCode.SimpleProfileNoMatchingServiceMatchFromHub),
                    Map.entry(TransactionIdaStatus.AuthenticationFailed, DetailedStatusCode.AuthenticationFailed),
                    Map.entry(TransactionIdaStatus.RequesterError, DetailedStatusCode.RequesterErrorFromIdpAsSentByHub)
            );

    @Inject
    public SimpleProfileTransactionIdaStatusMarshaller(OpenSamlXmlObjectFactory samlObjectFactory) {
        super(samlObjectFactory);
    }


    @Override
    protected DetailedStatusCode getDetailedStatusCode(TransactionIdaStatus originalStatus) {
        return REST_TO_SAML_CODES.get(originalStatus);
    }
}
