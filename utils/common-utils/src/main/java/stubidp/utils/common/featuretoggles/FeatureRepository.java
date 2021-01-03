package stubidp.utils.common.featuretoggles;

class FeatureRepository {
    @SuppressWarnings({"unchecked", "rawtypes"})
    void loadFeatures(FeatureConfiguration featureConfiguration) throws ClassNotFoundException {
        for (FeatureEntry featureEntry : featureConfiguration.getFeatures()) {
            Class featureClass = Class.forName(featureConfiguration.getFeatureClass());
            ((Feature)Enum.valueOf(featureClass, featureEntry.getFeatureName())).setActive(featureEntry.isActive());
        }
    }
}
