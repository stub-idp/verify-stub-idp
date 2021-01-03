package stubidp.stubidp.views;

import io.dropwizard.views.View;
import stubidp.shared.csrf.CSRFView;
import stubidp.stubidp.Urls;

import javax.ws.rs.core.UriBuilder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class IdpPageView extends View implements CSRFView {
    private final String subPageTemplateName;
    private final String name;
    final String idpId;
    private final String errorMessage;
    private final String assetId;
    private final Optional<String> csrfToken;

    IdpPageView(String subPageTemplateName, String name, String idpId, String errorMessage, String assetId, Optional<String> csrfToken) {
        super("idpPage.ftl", StandardCharsets.UTF_8);

        this.subPageTemplateName = subPageTemplateName;
        this.name = name;
        this.idpId = idpId;
        this.errorMessage = errorMessage;
        this.assetId = assetId;
        this.csrfToken = csrfToken;
    }

    public String getPageTitle() {
        return "No page title set.";
    }

    public String getAssetId() {
        return assetId;
    }

    public String getIdpId() {
        return idpId;
    }

    public String getName() {
        return name;
    }

    public String getErrorMessage() {
        if (errorMessage == null) {
            return "";
        }
        return errorMessage;
    }

    public String getSubPageTemplateName() {
        return subPageTemplateName;
    }

    public Optional<String> getCsrfToken() {
        return csrfToken;
    }

    public String getRegistrationResource() {
        return UriBuilder.fromPath(Urls.IDP_REGISTER_RESOURCE).build(idpId).toASCIIString();
    }

    public String getLoginResource() {
        return UriBuilder.fromPath(Urls.IDP_LOGIN_RESOURCE).build(idpId).toASCIIString();
    }

    public String getDebugResource() {
        return UriBuilder.fromPath(Urls.IDP_DEBUG_RESOURCE).build(idpId).toASCIIString();
    }

    public String getEidasRegistrationResource() {
        return UriBuilder.fromPath(Urls.EIDAS_REGISTER_RESOURCE).build(idpId).toASCIIString();
    }

    public String getEidasLoginResource() {
        return UriBuilder.fromPath(Urls.EIDAS_LOGIN_RESOURCE).build(idpId).toASCIIString();
    }

    public String getEidasDebugResource() {
        return UriBuilder.fromPath(Urls.EIDAS_DEBUG_RESOURCE).build(idpId).toASCIIString();
    }

}
