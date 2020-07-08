package stubidp.utils.common.featuretoggles;

import stubidp.utils.common.featuretoggles.Feature;

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
