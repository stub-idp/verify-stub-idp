package stubidp.stubidp.views;

import stubidp.stubidp.Urls;
import stubidp.stubidp.domain.DatabaseIdpUser;
import stubidp.stubidp.views.helpers.IdpUserHelper;

import javax.ws.rs.core.UriBuilder;
import java.util.Arrays;
import java.util.Optional;

public class HomePageView extends IdpPageView {
    private final Optional<DatabaseIdpUser> loggedInUser;
    private final IdpUserHelper idpUserHelper;

    public HomePageView(String name, String idpId, String errorMessage, String assetId, Optional<DatabaseIdpUser> loggedInUser) {
        super("homePage.ftl", name, idpId, errorMessage, assetId, Optional.empty());
        this.loggedInUser = loggedInUser;
        idpUserHelper = new IdpUserHelper(loggedInUser.orElse(null));
    }

    public String getPageTitle() {
        return String.format("Welcome to %s", getName());
    }

    public boolean isUserLoggedIn() { return loggedInUser.isPresent(); }

    public String getUserFullName() {
        return String.format("%s %s", idpUserHelper.getFirstName(), idpUserHelper.getSurname());
    }

    public String getAnOrA() {
        String[] vowels = {"a","e","i","o","u"};
        return Arrays.stream(vowels).anyMatch(vowel -> super.getName().toLowerCase().startsWith(vowel)) ? "an" : "a";
    }

    public String getLogoutResource() {
        return UriBuilder.fromPath(Urls.SINGLE_IDP_LOGOUT_RESOURCE).build(idpId).toASCIIString();
    }

    public String getPreRegisterResource() {
        return UriBuilder.fromPath(Urls.SINGLE_IDP_PRE_REGISTER_RESOURCE).build(idpId).toASCIIString();
    }

    public String getStartPromptResource() {
        return UriBuilder.fromPath(Urls.SINGLE_IDP_START_PROMPT_RESOURCE).build(idpId).toASCIIString();
    }
}
