package uk.gov.ida.rp.testrp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import stubidp.saml.metadata.TrustStoreConfiguration;
import stubidp.utils.rest.cache.AssetCacheConfiguration;
import stubidp.utils.rest.common.ServiceInfoConfiguration;
import stubidp.utils.rest.configuration.ServiceNameConfiguration;
import stubidp.utils.rest.restclient.RestfulClientConfiguration;
import stubidp.utils.security.configuration.DeserializablePublicKeyConfiguration;
import stubidp.utils.security.configuration.PrivateKeyConfiguration;
import uk.gov.ida.rp.testrp.saml.configuration.SamlConfigurationImpl;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestRpConfiguration extends Configuration implements AssetCacheConfiguration, ServiceNameConfiguration, RestfulClientConfiguration {

    @Valid
    @JsonProperty
    protected String assetsCacheDuration = "0";

    @Valid
    @JsonProperty
    protected boolean shouldCacheAssets = false;

    @Valid
    @NotNull
    @JsonProperty
    protected JerseyClientConfiguration httpClient;

    @Valid
    @NotNull
    @JsonProperty
    protected String javascriptPath;

    @Valid
    @NotNull
    @JsonProperty
    protected String stylesheetsPath;

    @Valid
    @NotNull
    @JsonProperty
    protected String imagesPath;

    @Valid
    @NotNull
    @JsonProperty
    protected Boolean privateBetaUserAccessRestrictionEnabled;

    @Valid
    @JsonProperty
    protected boolean hubExpectedToSignAuthnResponse = false;

    @Valid
    @NotNull
    @JsonProperty
    protected DeserializablePublicKeyConfiguration publicSigningCert;

    @Valid
    @NotNull
    @JsonProperty
    protected DeserializablePublicKeyConfiguration publicEncryptionCert;

    @Valid
    @JsonProperty
    protected boolean allowInsecureMetadataLocation = false;

    @JsonProperty
    @NotNull
    @Valid
    protected ServiceInfoConfiguration serviceInfo;

    @Valid
    @JsonProperty
    protected TrustStoreConfiguration clientTrustStoreConfiguration;

    @NotNull
    @JsonProperty
    @Valid
    protected SamlConfigurationImpl saml;

    @NotNull
    @JsonProperty
    @Valid
    protected String cookieName;

    @JsonProperty
    @Valid
    @NotNull
    protected boolean dontCacheFreemarkerTemplates;

    @Valid
    @JsonProperty
    protected boolean forceAuthentication = false;

    @NotNull
    @Valid
    @JsonProperty
    protected PrivateKeyConfiguration privateSigningKeyConfiguration;

    @NotNull
    @Valid
    @JsonProperty
    protected PrivateKeyConfiguration privateEncryptionKeyConfiguration;

    @NotNull
    @JsonProperty
    @Valid
    protected URI msaMetadataUri;

    @NotNull
    @JsonProperty
    @Valid
    protected String msaEntityId;

    @JsonProperty
    @Valid
    protected boolean shouldShowStartWithEidasButton;

    @NotNull
    @Valid
    @JsonProperty
    protected String hubEntityId = "https://signin.service.gov.uk";

    @NotNull
    @Valid
    @JsonProperty
    protected int tokenEpoch = 1;

    @JsonProperty
    protected String crossGovGaTrackerId = "";

    protected TestRpConfiguration() {}

    public String getHubEntityId() {
        return hubEntityId;
    }

    public String getJavascriptPath() {
        return javascriptPath;
    }

    public String getStylesheetsPath() {
        return stylesheetsPath;
    }

    public String getImagesPath() {
        return imagesPath;
    }

    public boolean isPrivateBetaUserAccessRestrictionEnabled() {
        return privateBetaUserAccessRestrictionEnabled;
    }

    public boolean shouldCacheAssets() {
        return shouldCacheAssets;
    }

    @Override
    public String getAssetsCacheDuration() {
        return assetsCacheDuration;
    }

    public DeserializablePublicKeyConfiguration getPublicSigningCert() { return publicSigningCert; }

    public DeserializablePublicKeyConfiguration getPublicEncryptionCert() { return publicEncryptionCert; }

    public boolean getAllowInsecureMetadataLocation() {
        return allowInsecureMetadataLocation;
    }

    public boolean isHubExpectedToSignAuthnResponse() {
        return hubExpectedToSignAuthnResponse;
    }

    public boolean getEnableRetryTimeOutConnections() {
        return false;
    }

    public String getMsaEntityId() {
        return msaEntityId;
    }

    public SamlConfigurationImpl getSamlConfiguration() {
        return saml;
    }

    public boolean getDontCacheFreemarkerTemplates() {
        return dontCacheFreemarkerTemplates;
    }

    public boolean getForceAuthentication() {
        return forceAuthentication;
    }

    public PrivateKeyConfiguration getPrivateSigningKeyConfiguration() {
        return privateSigningKeyConfiguration;
    }

    public PrivateKeyConfiguration getPrivateEncryptionKeyConfiguration() {
        return privateEncryptionKeyConfiguration;
    }

    public TrustStoreConfiguration getClientTrustStoreConfiguration() {
        return clientTrustStoreConfiguration;
    }

    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return httpClient;
    }

    public String getServiceName() {
        return serviceInfo.getName();
    }

    public URI getMsaMetadataUri() {
        return msaMetadataUri;
    }

    public boolean getShouldShowStartWithEidasButton() {
        return shouldShowStartWithEidasButton;
    }

    public int getTokenEpoch() {
        return tokenEpoch;
    }

    public String getCrossGovGaTrackerId() {
        return crossGovGaTrackerId;
    }

}
