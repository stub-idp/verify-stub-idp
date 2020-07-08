package stubidp.saml.hub.transformers.inbound;

import stubidp.saml.domain.assertions.IdpIdaStatus;
import stubidp.saml.extensions.extensions.StatusValue;
import stubidp.saml.domain.DetailedStatusCode;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SamlStatusToIdpIdaStatusMappingsFactory {
    public enum SamlStatusDefinitions {
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
        Map<SamlStatusDefinitions, IdpIdaStatus.Status> statusMap = new LinkedHashMap<>();
        statusMap.put(SamlStatusDefinitions.Success, IdpIdaStatus.Status.Success);
        statusMap.put(SamlStatusDefinitions.AuthenticationCancelled, IdpIdaStatus.Status.AuthenticationCancelled);
        statusMap.put(SamlStatusDefinitions.AuthenticationPending, IdpIdaStatus.Status.AuthenticationPending);
        statusMap.put(SamlStatusDefinitions.UpliftFailed, IdpIdaStatus.Status.UpliftFailed);
        statusMap.put(SamlStatusDefinitions.NoAuthenticationContext, IdpIdaStatus.Status.NoAuthenticationContext);
        statusMap.put(SamlStatusDefinitions.AuthenticationFailed, IdpIdaStatus.Status.AuthenticationFailed);
        statusMap.put(SamlStatusDefinitions.RequesterErrorFromIdp, IdpIdaStatus.Status.RequesterError);
        statusMap.put(SamlStatusDefinitions.RequesterErrorRequestDeniedFromIdp, IdpIdaStatus.Status.RequesterError);
        return Collections.unmodifiableMap(statusMap);
    }
}
