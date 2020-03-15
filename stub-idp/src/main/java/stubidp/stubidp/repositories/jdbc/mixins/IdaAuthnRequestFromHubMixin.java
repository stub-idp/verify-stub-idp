package stubidp.stubidp.repositories.jdbc.mixins;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import stubidp.saml.utils.core.domain.AuthnContext;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class IdaAuthnRequestFromHubMixin {
	@JsonCreator
	IdaAuthnRequestFromHubMixin(
			@JsonProperty("id") String id,
			@JsonProperty("issuer") String issuer,
			@JsonProperty("issueInstant") Instant issueInstant,
			@JsonProperty("levelsOfAssurance") List<AuthnContext> levelsOfAssurance,
			@JsonProperty("forceAuthentication") Optional<Boolean> forceAuthentication,
			@JsonProperty("sessionExpiryTimestamp") Instant sessionExpiryTimestamp,
			@JsonProperty("idpPostEndpoint") URI idpPostEndpoint,
			@JsonProperty("comparisonType") AuthnContextComparisonTypeEnumeration comparisonType) { }
}