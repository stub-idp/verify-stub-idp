package stubidp.saml.hub.transformers.inbound;

import stubidp.saml.hub.domain.CountryAuthenticationStatus;

public class CountryAuthenticationStatusUnmarshaller extends AuthenticationStatusUnmarshallerBase<CountryAuthenticationStatus.Status, CountryAuthenticationStatus> {
    public CountryAuthenticationStatusUnmarshaller() {
        super(new SamlStatusToCountryAuthenticationStatusCodeMapper(), new CountryAuthenticationStatus.CountryAuthenticationStatusFactory());
    }
}
