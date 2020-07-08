package stubidp.utils.common.featuretoggles;

public class FeatureRepository {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void loadFeatures(FeatureConfiguration featureConfiguration) throws ClassNotFoundException, NoSuchFieldException {
        for (FeatureEntry featureEntry : featureConfiguration.getFeatures()) {
            Class featureClass = Class.forName(featureConfiguration.getFeatureClass());
            ((Feature)Enum.valueOf(featureClass, featureEntry.getFeatureName())).setActive(featureEntry.isActive());
        }
    }
}
