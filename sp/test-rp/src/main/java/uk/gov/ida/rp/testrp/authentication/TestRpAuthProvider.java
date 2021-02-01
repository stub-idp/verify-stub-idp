package uk.gov.ida.rp.testrp.authentication;

import io.dropwizard.auth.Auth;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.AbstractValueParamProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.controllogic.AuthnRequestSenderHandler;
import uk.gov.ida.rp.testrp.repositories.Session;
import uk.gov.ida.rp.testrp.tokenservice.TokenService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.function.Function;

@Singleton
public class TestRpAuthProvider extends AbstractValueParamProvider {

    private final SimpleAuthenticator authenticator;
    private final TestRpConfiguration testRpConfiguration;
    private final AuthnRequestSenderHandler authnRequestManager;
    private final TokenService tokenService;

    @Inject
    private TestRpAuthProvider(
            MultivaluedParameterExtractorProvider mpep,
            SimpleAuthenticator authenticator,
            TestRpConfiguration testRpConfiguration,
            AuthnRequestSenderHandler authnRequestManager,
            TokenService tokenService) {
        super(() -> mpep, Parameter.Source.UNKNOWN);
        this.authenticator = authenticator;
        this.testRpConfiguration = testRpConfiguration;
        this.authnRequestManager = authnRequestManager;
        this.tokenService = tokenService;
    }

    @Singleton
    private static final class SessionInjectionResolver extends ParamInjectionResolver<Auth> {
        @Inject
        public SessionInjectionResolver(TestRpAuthProvider testRpAuthProvider, Provider<ContainerRequest> request) {
            super(testRpAuthProvider, Auth.class, request);
        }
    }

    public static AbstractBinder createBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(TestRpAuthProvider.class).to(ValueParamProvider.class).in(Singleton.class);
                bind(SessionInjectionResolver.class).to(SessionInjectionResolver.class).in(Singleton.class);
            }
        };
    }

    @Override
    protected Function<ContainerRequest, ?> createValueProvider(Parameter parameter) {
        if (Session.class.equals(parameter.getRawType())) {
            return containerRequest -> new SessionFactory(authenticator, testRpConfiguration, authnRequestManager, tokenService, containerRequest).provide();
        }
        return null;
    }

}
