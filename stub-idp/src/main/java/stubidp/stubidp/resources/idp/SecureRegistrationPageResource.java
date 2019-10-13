package stubidp.stubidp.resources.idp;

import stubidp.shared.views.SamlMessageRedirectViewFactory;
import stubidp.stubidp.filters.SessionCookieValueMustExistAsASession;
import stubidp.stubidp.repositories.IdpSessionRepository;
import stubidp.stubidp.repositories.IdpStubsRepository;
import stubidp.stubidp.services.IdpUserService;
import stubidp.stubidp.services.NonSuccessAuthnResponseService;

import javax.inject.Inject;

@SessionCookieValueMustExistAsASession
public class SecureRegistrationPageResource extends RegistrationPageResource {

    @Inject
    public SecureRegistrationPageResource(IdpStubsRepository idpStubsRepository,
                                          IdpUserService idpUserService,
                                          SamlMessageRedirectViewFactory samlMessageRedirectViewFactory,
                                          NonSuccessAuthnResponseService nonSuccessAuthnResponseService,
                                          IdpSessionRepository idpSessionRepository) {
        super(idpStubsRepository, idpUserService, samlMessageRedirectViewFactory, nonSuccessAuthnResponseService, idpSessionRepository);
    }
}
