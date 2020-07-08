package stubidp.saml.utils.core.test.builders;

import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.FraudAuthnDetails;
import stubidp.saml.domain.assertions.IdentityProviderAuthnStatement;
import stubidp.saml.domain.assertions.IpAddress;

import java.util.Optional;

import static stubidp.saml.domain.assertions.IdentityProviderAuthnStatement.createIdentityProviderAuthnStatement;
import static stubidp.saml.domain.assertions.IdentityProviderAuthnStatement.createIdentityProviderFraudAuthnStatement;

public class IdentityProviderAuthnStatementBuilder {
    private Optional<FraudAuthnDetails> fraudAuthnDetails = Optional.empty();
    private AuthnContext authnContext = AuthnContext.LEVEL_1;
    private Optional<IpAddress> userIpAddress = Optional.ofNullable(IpAddressBuilder.anIpAddress().build());

    public static IdentityProviderAuthnStatementBuilder anIdentityProviderAuthnStatement() {
        return new IdentityProviderAuthnStatementBuilder();
    }

    public IdentityProviderAuthnStatement build() {
        if (fraudAuthnDetails.isPresent()) {
            return createIdentityProviderFraudAuthnStatement(fraudAuthnDetails.get(), userIpAddress.orElse(null));
        }
        return createIdentityProviderAuthnStatement(authnContext, userIpAddress.orElse(null));
    }

    public IdentityProviderAuthnStatementBuilder withAuthnContext(AuthnContext authnContext) {
        this.authnContext = authnContext;
        return this;
    }

    public IdentityProviderAuthnStatementBuilder withFraudDetails(FraudAuthnDetails fraudDetails) {
        this.fraudAuthnDetails = Optional.ofNullable(fraudDetails);
        return this;
    }

    public IdentityProviderAuthnStatementBuilder withUserIpAddress(IpAddress userIpAddress) {
        this.userIpAddress = Optional.ofNullable(userIpAddress);
        return this;
    }
}
