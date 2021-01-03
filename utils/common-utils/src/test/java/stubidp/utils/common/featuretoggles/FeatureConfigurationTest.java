package stubidp.utils.common.featuretoggles;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class FeatureConfigurationTest {

    @Test
    void loadFromFile_shouldCreateConfiguration() {
        Yaml yaml = new Yaml(new Constructor(FeatureConfiguration.class));
        String fileName = "stubidp.utils.common.featuretoggles/feature-toggles.yml";
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream(fileName);

        // Parse the YAML file and return the output as a series of Maps and Lists
        FeatureConfiguration featureConfiguration = yaml.load(is);

        assertThat(featureConfiguration.getFeatureClass()).isEqualTo("IdaFeatures");
        assertThat(featureConfiguration.getFeatures().size()).isEqualTo(2);
        assertThat(featureConfiguration.getFeatures().get(0).isActive()).isEqualTo(false);
        assertThat(featureConfiguration.getFeatures().get(0).getFeatureName()).isEqualTo("UIRework");
        assertThat(featureConfiguration.getFeatures().get(1).isActive()).isEqualTo(true);
        assertThat(featureConfiguration.getFeatures().get(1).getFeatureName()).isEqualTo("EncodeAssertions");
    }

    @Test
    void loadFromFile_shouldCreateConfigurationForEmptyFeatureList() {
        Yaml yaml = new Yaml(new Constructor(FeatureConfiguration.class));
        String fileName = "stubidp.utils.common.featuretoggles/no-features.yml";
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream(fileName);

        // Parse the YAML file and return the output as a series of Maps and Lists
        FeatureConfiguration featureConfiguration = yaml.load(is);

        assertThat(featureConfiguration.getFeatureClass()).isEqualTo("IdaFeatures");
        assertThat(featureConfiguration.getFeatures().size()).isEqualTo(0);
        assertThat(featureConfiguration.getFeatures()).isNotNull();
    }
}
