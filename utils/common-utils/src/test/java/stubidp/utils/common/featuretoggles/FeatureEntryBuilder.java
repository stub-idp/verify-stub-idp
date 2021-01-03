package stubidp.utils.common.featuretoggles;

public class FeatureEntryBuilder {
    private String featureName;
    private boolean isActive;

    public static FeatureEntryBuilder aFeatureEntry() {
        return new FeatureEntryBuilder();
    }

    public FeatureEntryBuilder withFeatureName(String featureName) {
        this.featureName = featureName;
        return this;
    }

    public FeatureEntryBuilder isActive(boolean isActive) {
        this.isActive = isActive;
        return this;
    }

    public FeatureEntry build() {
        return new FeatureEntry(){
            @Override
            public String getFeatureName() {
                return FeatureEntryBuilder.this.featureName;
            }

            @Override
            public boolean isActive() {
                return isActive;
            }
        };
    }

}
