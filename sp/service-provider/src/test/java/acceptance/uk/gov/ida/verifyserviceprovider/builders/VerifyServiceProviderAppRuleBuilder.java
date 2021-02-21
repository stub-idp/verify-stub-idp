package acceptance.uk.gov.ida.verifyserviceprovider.builders;

import acceptance.uk.gov.ida.verifyserviceprovider.rules.VerifyServiceProviderAppExtension;
import common.uk.gov.ida.verifyserviceprovider.servers.MockMsaServer;

public class VerifyServiceProviderAppRuleBuilder {

    private MockMsaServer mockMsaServer;
    private boolean isEidasEnabled;
    private String secondaryEncryptionKey;
    private String serviceEntityIdOverride;

    public static VerifyServiceProviderAppRuleBuilder aVerifyServiceProviderAppRule() {
        return new VerifyServiceProviderAppRuleBuilder();
    }

    public VerifyServiceProviderAppExtension build(){
        return new VerifyServiceProviderAppExtension();
    }

    public VerifyServiceProviderAppRuleBuilder withMockMsaServer(MockMsaServer mockMsaServer) {
        this.mockMsaServer = mockMsaServer;
        return this;
    }

    public VerifyServiceProviderAppRuleBuilder withEidasEnabledFlag(boolean isEidasEnabled) {
        this.isEidasEnabled = isEidasEnabled;
        return this;
    }
}
