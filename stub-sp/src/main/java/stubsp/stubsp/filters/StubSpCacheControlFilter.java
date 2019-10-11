package stubsp.stubsp.filters;

import stubidp.utils.rest.cache.CacheControlFilter;
import stubsp.stubsp.configuration.StubSpConfiguration;

public class StubSpCacheControlFilter extends CacheControlFilter {
    public StubSpCacheControlFilter(final StubSpConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected boolean isCacheableAsset(final String localAddr) {
        return localAddr.contains("/assets/fonts/") || localAddr.contains("/assets/images/");
    }
}

