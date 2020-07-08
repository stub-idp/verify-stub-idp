package stubidp.saml.hub.hub.transformers.inbound;

import stubidp.saml.hub.hub.domain.CountryAuthenticationStatus;
import stubidp.saml.domain.DetailedStatusCode;

import java.util.Map;

public class SamlStatusToCountryAuthenticationStatusMappingsFactory {
    public enum SamlStatusDefinitions {
        Success(DetailedStatusCode.Success);

        private final DetailedStatusCode statusCode;

        SamlStatusDefinitions(DetailedStatusCode statusCode) {
            this.statusCode = statusCode;
        }

        public boolean matches(String samlStatusValue) {
            return statusCode.getStatus().equals(samlStatusValue);
        }
    }

    public static Map<SamlStatusDefinitions, CountryAuthenticationStatus.Status> getSamlToCountryAuthenticationStatusMappings() {
        // Matching SAML statuses to their CountryAuthenticationStatus counterparts is dependent on the ordering of these put()
        // statements. There must be a better way of doing this.
        return Map.<SamlStatusDefinitions, CountryAuthenticationStatus.Status>ofEntries(
                Map.entry(SamlStatusDefinitions.Success, CountryAuthenticationStatus.Status.Success)
        );
    }
}
