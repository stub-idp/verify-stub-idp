package stubidp.utils.common.featuretoggles;

public enum IdaFeatures implements Feature {

    UIRework,
    EncodeAssertions;

    private boolean active;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
