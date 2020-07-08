package stubidp.saml.utils.core.transformers.inbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.domain.assertions.Cycle3Dataset;
import stubidp.saml.test.support.SamlTransformationErrorManagerTestHelper;
import stubidp.saml.test.builders.AssertionBuilder;
import stubidp.saml.test.builders.AttributeStatementBuilder;
import stubidp.saml.test.builders.SimpleStringAttributeBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.utils.hub.errors.SamlTransformationErrorFactory.missingAttributeStatementInAssertion;

public class Cycle3DatasetFactoryTest extends OpenSAMLRunner {

    private Cycle3DatasetFactory cycle3DatasetFactory;

    @BeforeEach
    public void setup() {
        cycle3DatasetFactory = new Cycle3DatasetFactory();
    }

    @Test
    public void transform_shouldTransformAListOfAttributesToACycle3Dataset() throws Exception {
        String attributeNameOne = "attribute name one";
        String attributeNameTwo = "attribute name two";
        String attributeValueOne = "attribute value one";
        String attributeValueTwo = "attribute value two";

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(SimpleStringAttributeBuilder.aSimpleStringAttribute().withName(attributeNameOne).withSimpleStringValue(attributeValueOne).build());
        attributes.add(SimpleStringAttributeBuilder.aSimpleStringAttribute().withName(attributeNameTwo).withSimpleStringValue(attributeValueTwo).build());

        Assertion assertion = AssertionBuilder.aCycle3DatasetAssertion(attributes).buildUnencrypted();

        Cycle3Dataset cycle3Dataset = cycle3DatasetFactory.createCycle3DataSet(assertion);

        assertThat(cycle3Dataset).isNotNull();
        assertThat(cycle3Dataset.getAttributes().size()).isEqualTo(2);
        assertThat(cycle3Dataset.getAttributes().get(attributeNameOne)).isEqualTo(attributeValueOne);
        assertThat(cycle3Dataset.getAttributes().get(attributeNameTwo)).isEqualTo(attributeValueTwo);
    }

    @Test
    public void transform_shouldThrowExceptionIfThereIsMoreThanOneAttributeStatement() throws Exception {
        final Assertion assertion = AssertionBuilder.anAssertion()
                .addAttributeStatement(AttributeStatementBuilder.anAttributeStatement().build())
                .addAttributeStatement(AttributeStatementBuilder.anAttributeStatement().build())
                .buildUnencrypted();

        SamlTransformationErrorManagerTestHelper.validateFail(
                () -> cycle3DatasetFactory.createCycle3DataSet(assertion),
                missingAttributeStatementInAssertion(assertion.getID()));
    }

    @Test
    public void transform_shouldThrowExceptionIfThereIsNoAttributeStatement() throws Exception {
        final Assertion assertion = AssertionBuilder.anAssertion()
                .buildUnencrypted();
        SamlTransformationErrorManagerTestHelper.validateFail(
                () -> cycle3DatasetFactory.createCycle3DataSet(assertion),
                missingAttributeStatementInAssertion(assertion.getID()));
    }
}
