package stubidp.utils.rest.cache;

public interface AssetCacheConfiguration {

    boolean shouldCacheAssets();

    String getAssetsCacheDuration();
}
