package uk.gov.ida.shared.dropwizard.jade;

import de.neuland.jade4j.model.JadeModel;
import io.dropwizard.views.View;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.text.MessageFormat.format;
import static org.assertj.core.api.Assertions.assertThat;

public class JadeModelFactoryTest {

    public final String EXPECTED_KEY = "customStringField";

    public JadeModelFactory factory;

    @BeforeEach
    public void setUp() throws Exception {
        factory = new JadeModelFactory();
    }

    @Test
    public void createModel_shouldIncludePublicGettersInCustomView() throws Exception {
        CustomJadeView view = new CustomJadeView("");

        JadeModel model = factory.createModel(view);

        assertThat(model.containsKey(EXPECTED_KEY)).as(format("Model does not contain expected key: {0}", EXPECTED_KEY)).isTrue();
        assertThat((String) model.get(EXPECTED_KEY)).isEqualTo(view.getCustomStringField());
    }

    @Test
    public void createModel_shouldIncludeInheritedGetters() throws Exception {
        CustomJadeSubview view = new CustomJadeSubview("");

        JadeModel model = factory.createModel(view);

        assertThat(model.containsKey(EXPECTED_KEY)).as(format("Model does not contain expected key: {0}", EXPECTED_KEY)).isTrue();
        assertThat((String) model.get(EXPECTED_KEY)).isEqualTo(view.getCustomStringField());
    }

    @Test
    public void createModel_shouldExcludeGettersThatTakeParams() throws Exception {
        final String excludedKey = "withParam";
        final String excludedValue = "value to exclude";
        View view = new CustomJadeView("") {
            @SuppressWarnings("unused")
            public String getWithParam(final String someParam) {
                return excludedValue;
            }
        };

        JadeModel model = factory.createModel(view);

        assertThat(model.containsKey(excludedKey)).as(format("Model contains key: {0}", excludedKey)).isFalse();
        assertThat(model.containsValue(excludedValue)).as(format("Model contains expectedValue: {0}", excludedValue)).isFalse();
    }

    @Test
    public void createModel_shouldExcludeMethodsWithoutGetPrefix() throws Exception {
        final String excludedKey = "aGetMethod";
        final String excludedValue = "expectedValue to exclude";
        View view = new CustomJadeView("") {
            @SuppressWarnings("unused")
            public String notAGetMethod() {
                return excludedValue;
            }
        };

        JadeModel model = factory.createModel(view);

        assertThat(model.containsKey(excludedKey)).as(format("Model contains key: {0}", excludedKey)).isFalse();
        assertThat(model.containsValue(excludedValue)).as(format("Model contains expectedValue: {0}", excludedValue)).isFalse();
    }

    @Test
    public void createModel_shouldExcludeProtectedMethods() throws Exception {
        final String exludedKey = "protected";
        final String excludedValue = "excluded value";
        View view = new CustomJadeView("") {
            @SuppressWarnings("unused")
            protected String getProtected() {
                return excludedValue;
            }
        };

        JadeModel model = factory.createModel(view);

        assertThat(model.containsKey(exludedKey)).as(format("Model contains key: {0}", exludedKey)).isFalse();
        assertThat(model.containsValue(excludedValue)).as(format("Model contains expectedValue: {0}", excludedValue)).isFalse();
    }

    private static class CustomJadeView extends View {
        protected CustomJadeView(final String templateName) {
            super(templateName);
        }

        public String getCustomStringField() {
            return "some value";
        }
    }

    private static class CustomJadeSubview extends CustomJadeView {
        protected CustomJadeSubview(final String templateName) {
            super(templateName);
        }
    }
}
