package stubidp.saml.hub.hub.transformers.inbound;

import com.google.common.collect.ImmutableMap;
import stubidp.saml.extensions.extensions.StatusValue;
import stubidp.saml.hub.hub.domain.IdpIdaStatus;
import stubidp.saml.utils.core.domain.DetailedStatusCode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SamlStatusToIdpIdaStatusMappingsFactory {
    enum SamlStatusDefinitions {
        Success(DetailedStatusCode.Success, Optional.empty()),
        AuthenticationCancelled(DetailedStatusCode.NoAuthenticationContext, Optional.of(StatusValue.CANCEL)),
        AuthenticationPending(DetailedStatusCode.NoAuthenticationContext, Optional.of(StatusValue.PENDING)),
        NoAuthenticationContext(DetailedStatusCode.NoAuthenticationContext, Optional.empty()),
        AuthenticationFailed(DetailedStatusCode.AuthenticationFailed, Optional.empty()),
        RequesterErrorFromIdp(DetailedStatusCode.RequesterErrorFromIdp, Optional.empty()),
        RequesterErrorRequestDeniedFromIdp(DetailedStatusCode.RequesterErrorRequestDeniedFromIdp, Optional.empty()),
        UpliftFailed(DetailedStatusCode.NoAuthenticationContext, Optional.of(StatusValue.UPLIFT_FAILED));

        private final DetailedStatusCode statusCode;
        private final Optional<String> statusDetailValue;

        SamlStatusDefinitions(DetailedStatusCode statusCode, Optional<String> statusDetailValue) {
            this.statusCode = statusCode;
            this.statusDetailValue = statusDetailValue;
        }

        public boolean matches(String samlStatusValue, Optional<String> samlSubStatusValue, List<String> statusDetailValues) {
            boolean statusCodesMatch = statusCode.getStatus().equals(samlStatusValue) && statusCode.getSubStatus().equals(samlSubStatusValue);
            boolean statusDetailsMatch = statusDetailValue.map(statusDetailValues::contains).orElse(true);
            return statusCodesMatch && statusDetailsMatch;
        }
    }

    public static Map<SamlStatusDefinitions, IdpIdaStatus.Status> getSamlToIdpIdaStatusMappings() {
        // Matching SAML statuses to their IdpIdaStatus counterparts is dependent on the ordering of these put()
        // statements. There must be a better way of doing this.
        return ImmutableMap.<SamlStatusDefinitions, IdpIdaStatus.Status>builder()
                .put(Map.entry(SamlStatusDefinitions.Success, IdpIdaStatus.Status.Success))
                .put(Map.entry(SamlStatusDefinitions.AuthenticationCancelled, IdpIdaStatus.Status.AuthenticationCancelled))
                .put(Map.entry(SamlStatusDefinitions.AuthenticationPending, IdpIdaStatus.Status.AuthenticationPending))
                .put(Map.entry(SamlStatusDefinitions.UpliftFailed, IdpIdaStatus.Status.UpliftFailed))
                .put(Map.entry(SamlStatusDefinitions.NoAuthenticationContext, IdpIdaStatus.Status.NoAuthenticationContext))
                .put(Map.entry(SamlStatusDefinitions.AuthenticationFailed, IdpIdaStatus.Status.AuthenticationFailed))
                .put(Map.entry(SamlStatusDefinitions.RequesterErrorFromIdp, IdpIdaStatus.Status.RequesterError))
                .put(Map.entry(SamlStatusDefinitions.RequesterErrorRequestDeniedFromIdp, IdpIdaStatus.Status.RequesterError))
                .build();
    }
}
