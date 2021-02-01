package uk.gov.ida.rp.testrp.filters;

import uk.gov.ida.cache.AssetCacheConfiguration;
import uk.gov.ida.cache.CacheControlFilter;

import javax.inject.Inject;

public class SampleRpCacheControlFilter extends CacheControlFilter {

    @Inject
    public SampleRpCacheControlFilter(AssetCacheConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected boolean isCacheableAsset(String localAddr) {
        return localAddr.contains("/assets/fonts/") || localAddr.contains("/assets/images/");
    }
}
