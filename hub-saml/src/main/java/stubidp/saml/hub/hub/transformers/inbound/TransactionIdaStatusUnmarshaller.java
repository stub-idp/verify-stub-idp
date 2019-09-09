package stubidp.saml.hub.hub.transformers.inbound;

import stubidp.saml.utils.core.domain.TransactionIdaStatus;

import java.util.AbstractMap;
import java.util.Map;

public class TransactionIdaStatusUnmarshaller extends IdaStatusUnmarshaller<TransactionIdaStatus> {

    private static final Map<IdaStatusMapperStatus, TransactionIdaStatus> SAML_TO_REST_CODES =
            Map.<IdaStatusMapperStatus, TransactionIdaStatus>ofEntries(
                    Map.entry(IdaStatusMapperStatus.RequesterErrorFromIdpAsSentByHub, TransactionIdaStatus.RequesterError),
                    Map.entry(IdaStatusMapperStatus.AuthenticationFailed, TransactionIdaStatus.AuthenticationFailed),
                    Map.entry(IdaStatusMapperStatus.NoAuthenticationContext, TransactionIdaStatus.NoAuthenticationContext),
                    Map.entry(IdaStatusMapperStatus.NoMatchingServiceMatchFromHub, TransactionIdaStatus.NoMatchingServiceMatchFromHub), // This line represents Success:no-match (Legacy functionality which will be deleted in the distant future)
                    Map.entry(IdaStatusMapperStatus.NoMatchingServiceMatchFromMatchingService, TransactionIdaStatus.NoMatchingServiceMatchFromHub), // This line represents Responder:no-match
                    Map.entry(IdaStatusMapperStatus.Success, TransactionIdaStatus.Success)
            );

    public TransactionIdaStatusUnmarshaller() {
        super(SAML_TO_REST_CODES);
    }
}
