package stubidp.saml.utils.hub.domain;

import stubidp.saml.utils.core.domain.AuthnContext;
import stubidp.saml.utils.core.domain.IdaSamlMessage;

import java.net.URI;
import java.time.Instant;
import java.util.List;

public class EidasAuthnRequestFromHub extends IdaSamlMessage {
    private final List<AuthnContext> levelsOfAssurance;
    private final String providerName;

    public EidasAuthnRequestFromHub(
        String id,
        String issuer,
        Instant issueInstant,
        List<AuthnContext> levelsOfAssurance,
        URI countryPostEndpoint,
        String providerName) {
        super(id, issuer, issueInstant, countryPostEndpoint);
        this.levelsOfAssurance = levelsOfAssurance;
        this.providerName = providerName;
    }

    public static EidasAuthnRequestFromHub createRequestToSendFromHub(String id, List<AuthnContext> levelsOfAssurance, URI countryPostEndpoint, String providerName, String hubEntityId) {
        return new EidasAuthnRequestFromHub(id, hubEntityId, Instant.now(), levelsOfAssurance, countryPostEndpoint, providerName);
    }


    public String getProviderName() {
        return providerName;
    }

    public List<AuthnContext> getLevelsOfAssurance() {
        return levelsOfAssurance;
    }
}
