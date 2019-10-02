package stubidp.stubidp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.jdbi.v3.core.Jdbi;
import org.opensaml.saml.saml2.core.AuthnRequest;
import stubidp.stubidp.auth.ManagedAuthFilterInstaller;
import stubidp.stubidp.configuration.IdpStubsConfiguration;
import stubidp.stubidp.configuration.StubIdpConfiguration;
import stubidp.stubidp.cookies.CookieFactory;
import stubidp.stubidp.cookies.HmacValidator;
import stubidp.stubidp.domain.factories.StubTransformersFactory;
import stubidp.stubidp.filters.SessionCookieValueMustExistAsASessionFilter;
import stubidp.stubidp.listeners.StubIdpsFileListener;
import stubidp.stubidp.repositories.AllIdpsUserRepository;
import stubidp.stubidp.repositories.IdpSessionRepository;
import stubidp.stubidp.repositories.IdpStubsRepository;
import stubidp.stubidp.repositories.UserRepository;
import stubidp.stubidp.repositories.jdbc.JDBIIdpSessionRepository;
import stubidp.stubidp.repositories.jdbc.JDBIUserRepository;
import stubidp.stubidp.repositories.jdbc.UserMapper;
import stubidp.stubidp.repositories.reaper.ManagedStaleSessionReaper;
import stubidp.stubidp.services.AuthnRequestReceiverService;
import stubidp.stubidp.services.GeneratePasswordService;
import stubidp.stubidp.services.IdpUserService;
import stubidp.stubidp.services.UserService;
import stubidp.stubidp.views.SamlResponseRedirectViewFactory;
import stubidp.utils.rest.jerseyclient.JsonResponseProcessor;
import stubidp.utils.rest.truststore.EmptyKeyStoreProvider;
import stubidp.utils.security.configuration.SecureCookieConfiguration;
import stubidp.utils.security.configuration.SecureCookieKeyStore;
import stubidp.utils.security.security.HmacDigest;
import stubidp.utils.security.security.IdGenerator;
import stubidp.utils.security.security.SecureCookieKeyConfigurationKeyStore;
import stubidp.utils.security.security.X509CertificateFactory;

import javax.inject.Singleton;
import javax.ws.rs.core.GenericType;
import java.security.KeyStore;
import java.util.function.Function;

public class StubIdpBinder extends AbstractBinder {

    public static final String IS_SECURE_COOKIE_ENABLED = "isSecureCookieEnabled";

    private final StubIdpConfiguration stubIdpConfiguration;
    private final Environment environment;

    StubIdpBinder(StubIdpConfiguration stubIdpConfiguration,
                  Environment environment) {
        this.stubIdpConfiguration = stubIdpConfiguration;
        this.environment = environment;
    }

    @Override
    protected void configure() {
        bind(SamlResponseRedirectViewFactory.class).to(SamlResponseRedirectViewFactory.class);
        bind(IdGenerator.class).to(IdGenerator.class);
        bind(X509CertificateFactory.class).to(X509CertificateFactory.class);

        bind(AllIdpsUserRepository.class).in(Singleton.class).to(AllIdpsUserRepository.class);
        bind(IdpStubsRepository.class).in(Singleton.class).to(IdpStubsRepository.class);
        bind(StubIdpsFileListener.class).in(Singleton.class).to(StubIdpsFileListener.class);
        final ObjectMapper objectMapper = Jackson.newObjectMapper();
        final UserMapper userMapper = new UserMapper(objectMapper);
        bind(userMapper).to(UserMapper.class);
        final Jdbi jdbi = Jdbi.create(stubIdpConfiguration.getDatabaseConfiguration().getUrl());
        bind(jdbi).to(Jdbi.class);
        bind(JDBIUserRepository.class).in(Singleton.class).to(UserRepository.class);
        bind(JDBIIdpSessionRepository.class).in(Singleton.class).to(IdpSessionRepository.class);

        bind(new EmptyKeyStoreProvider().get()).to(KeyStore.class);

        //must be eager singletons to be auto injected
        // Elegant-hack: this is how we install the basic auth filter, so we can use a guice injected user repository
        bind(ManagedAuthFilterInstaller.class).in(Singleton.class).to(ManagedAuthFilterInstaller.class);

        // FIXME: needed for both eidas + idp
        bind(AuthnRequestReceiverService.class).to(AuthnRequestReceiverService.class);
        final StubTransformersFactory stubTransformersFactory = new StubTransformersFactory();
        bind(stubTransformersFactory.getStringToAuthnRequest()).to(new GenericType<Function<String, AuthnRequest>>() {});

        bind(GeneratePasswordService.class).to(GeneratePasswordService.class);
        bind(IdpUserService.class).to(IdpUserService.class);
        bind(UserService.class).to(UserService.class);

        bind(ManagedStaleSessionReaper.class).in(Singleton.class).to(ManagedStaleSessionReaper.class);

        bind(JsonResponseProcessor.class).to(JsonResponseProcessor.class);

        // secure cookie config
        bind(stubIdpConfiguration.getSecureCookieConfiguration().isSecure()).named(IS_SECURE_COOKIE_ENABLED).to(Boolean.class);
        bind(stubIdpConfiguration.getSecureCookieConfiguration()).to(SecureCookieConfiguration.class);
        bind(HmacValidator.class).to(HmacValidator.class);
        bind(HmacDigest.class).to(HmacDigest.class);
        bind(new HmacDigest.HmacSha256MacFactory()).to(HmacDigest.HmacSha256MacFactory.class);
        bind(SecureCookieKeyConfigurationKeyStore.class).to(SecureCookieKeyStore.class);
        bind(CookieFactory.class).to(CookieFactory.class);

        // other
        bind(new DefaultConfigurationFactoryFactory<IdpStubsConfiguration>()
                .create(IdpStubsConfiguration.class, environment.getValidator(), environment.getObjectMapper(), ""))
                .to(new GenericType<ConfigurationFactory<IdpStubsConfiguration>>() {});

        bind(SessionCookieValueMustExistAsASessionFilter.class).to(SessionCookieValueMustExistAsASessionFilter.class);
    }
}
