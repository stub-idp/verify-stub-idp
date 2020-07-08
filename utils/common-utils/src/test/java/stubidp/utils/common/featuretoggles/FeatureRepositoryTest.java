package stubidp.utils.common.featuretoggles;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.utils.common.featuretoggles.FeatureEntryBuilder.aFeatureEntry;
import static stubidp.utils.common.featuretoggles.IdaFeatures.EncodeAssertions;
import static stubidp.utils.common.featuretoggles.IdaFeatures.UIRework;

public class FeatureRepositoryTest {

    @Test
    public void should_loadActive() throws Exception {
        FeatureConfiguration configuration = FeatureConfigurationBuilder.aFeatureConfiguration()
                .withFeatureClass(IdaFeatures.class.getCanonicalName())
                .withFeature(aFeatureEntry().withFeatureName(UIRework.name()).isActive(true).build())
                .withFeature(aFeatureEntry().withFeatureName(EncodeAssertions.name()).isActive(false).build())
                .build();

        FeatureRepository systemUnderTest = new FeatureRepository();
        systemUnderTest.loadFeatures(configuration);

        assertThat(UIRework.isActive()).isTrue();
    }

    @Test
    public void should_loadInactive() throws Exception {
        FeatureConfiguration configuration = FeatureConfigurationBuilder.aFeatureConfiguration()
                .withFeatureClass(IdaFeatures.class.getCanonicalName())
                .withFeature(aFeatureEntry().withFeatureName(UIRework.name()).isActive(true).build())
                .withFeature(aFeatureEntry().withFeatureName(EncodeAssertions.name()).isActive(false).build())
                .build();

        FeatureRepository systemUnderTest = new FeatureRepository();
        systemUnderTest.loadFeatures(configuration);

        assertThat(EncodeAssertions.isActive()).isFalse();
    }
}    
