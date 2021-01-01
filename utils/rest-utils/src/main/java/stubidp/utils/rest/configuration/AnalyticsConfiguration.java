package stubidp.utils.rest.configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class AnalyticsConfiguration {

    protected AnalyticsConfiguration() {

    }

    @NotNull
    @Valid
    protected Boolean enabled;

    @Valid
    protected Integer siteId;

    @Valid
    protected String piwikBaseUrl;

    @Valid
    protected String piwikServerSideUrl;

    public String getPiwikServerSideUrl() {
        return piwikServerSideUrl;
    }

    public Integer getSiteId() {
        return siteId;
    }

    public String getPiwikBaseUrl() {
        return piwikBaseUrl;
    }

    public boolean getEnabled() {
        return enabled;
    }
}
