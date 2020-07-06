package stubidp.saml.hub.hub.transformers.outbound;

import org.opensaml.saml.saml2.core.StatusDetail;
import stubidp.saml.extensions.extensions.StatusValue;
import stubidp.saml.hub.hub.domain.IdpIdaStatus;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.domain.DetailedStatusCode;
import stubidp.saml.utils.core.transformers.outbound.IdaStatusMarshaller;

import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class IdpIdaStatusMarshaller extends IdaStatusMarshaller<IdpIdaStatus> {

    private static final Map<IdpIdaStatus, DetailedStatusCode> REST_TO_SAML_CODES =
            Map.<IdpIdaStatus, DetailedStatusCode>ofEntries(
                    Map.entry(IdpIdaStatus.success(), DetailedStatusCode.Success),
                    Map.entry(IdpIdaStatus.noAuthenticationContext(), DetailedStatusCode.NoAuthenticationContext),
                    Map.entry(IdpIdaStatus.authenticationFailed(), DetailedStatusCode.AuthenticationFailed),
                    Map.entry(IdpIdaStatus.requesterError(), DetailedStatusCode.RequesterErrorFromIdp),
                    Map.entry(IdpIdaStatus.authenticationCancelled(), DetailedStatusCode.NoAuthenticationContext),
                    Map.entry(IdpIdaStatus.authenticationPending(), DetailedStatusCode.NoAuthenticationContext),
                    Map.entry(IdpIdaStatus.upliftFailed(), DetailedStatusCode.NoAuthenticationContext)
            );

    private static final Map<IdpIdaStatus, String> REST_TO_STATUS_DETAIL =
            Map.<IdpIdaStatus, String>ofEntries(
                    Map.entry(IdpIdaStatus.authenticationCancelled(), StatusValue.CANCEL),
                    Map.entry(IdpIdaStatus.authenticationPending(), StatusValue.PENDING),
                    Map.entry(IdpIdaStatus.upliftFailed(), StatusValue.UPLIFT_FAILED)
            );

    public IdpIdaStatusMarshaller(OpenSamlXmlObjectFactory samlObjectFactory) {
        super(samlObjectFactory);
    }

    @Override
    protected Optional<String> getStatusMessage(IdpIdaStatus originalStatus) {
        return originalStatus.getMessage();
    }

    @Override
    protected Optional<StatusDetail> getStatusDetail(IdpIdaStatus originalStatus) {
        if (REST_TO_STATUS_DETAIL.containsKey(originalStatus)) {
            StatusDetail statusDetail = this.samlObjectFactory.createStatusDetail();
            StatusValue statusValue = this.samlObjectFactory.createStatusValue(REST_TO_STATUS_DETAIL.get(originalStatus));
            statusDetail.getUnknownXMLObjects().add(statusValue);
            return of(statusDetail);
        }
        return empty();
    }

    @Override
    protected DetailedStatusCode getDetailedStatusCode(IdpIdaStatus originalStatus) {
        return REST_TO_SAML_CODES.get(originalStatus);
    }
}
