package stubidp.stubidp.resources.idp;

import stubidp.stubidp.cookies.CookieFactory;
import stubidp.stubidp.filters.SessionCookieValueMustExistAsASession;
import stubidp.stubidp.repositories.IdpSessionRepository;
import stubidp.stubidp.repositories.IdpStubsRepository;
import stubidp.stubidp.services.IdpUserService;
import stubidp.stubidp.services.NonSuccessAuthnResponseService;
import stubidp.stubidp.views.SamlMessageRedirectViewFactory;

import javax.inject.Inject;

@SessionCookieValueMustExistAsASession
public class SecureLoginPageResource extends LoginPageResource {
    @Inject
    public SecureLoginPageResource(IdpStubsRepository idpStubsRepository,
                                   NonSuccessAuthnResponseService nonSuccessAuthnResponseService,
                                   SamlMessageRedirectViewFactory samlMessageRedirectViewFactory,
                                   IdpUserService idpUserService,
                                   IdpSessionRepository sessionRepository,
                                   CookieFactory cookieFactory) {
        super(idpStubsRepository, nonSuccessAuthnResponseService, samlMessageRedirectViewFactory, idpUserService, sessionRepository, cookieFactory);
    }
}
