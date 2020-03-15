package stubidp.saml.utils.core.transformers.outbound;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import stubidp.saml.utils.OpenSAMLRunner;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.domain.AssertionRestrictions;
import stubidp.saml.utils.core.domain.HubAssertion;
import stubidp.saml.utils.core.domain.PersistentId;
import stubidp.saml.utils.core.test.builders.HubAssertionBuilder;
import stubidp.saml.utils.core.test.builders.PersistentIdBuilder;

public class OutboundAssertionToSubjectTransformerTest extends OpenSAMLRunner {

    private OutboundAssertionToSubjectTransformer transformer;

    @BeforeEach
    public void setup() {
        transformer = new OutboundAssertionToSubjectTransformer(new OpenSamlXmlObjectFactory());
    }

    @Test
    public void transform_shouldAddSubjectConfirmationData() {
        HubAssertion assertion = HubAssertionBuilder.aHubAssertion().build();

        final Subject subject = transformer.transform(assertion);

        final SubjectConfirmation subjectConfirmation = subject.getSubjectConfirmations().get(0);
        Assertions.assertThat(subjectConfirmation.getMethod()).isEqualTo(SubjectConfirmation.METHOD_BEARER);
        final AssertionRestrictions assertionRestrictions = assertion.getAssertionRestrictions();
        Assertions.assertThat(subjectConfirmation.getSubjectConfirmationData().getRecipient()).isEqualTo(assertionRestrictions.getRecipient());
        Assertions.assertThat(subjectConfirmation.getSubjectConfirmationData().getNotOnOrAfter()).isEqualTo(assertionRestrictions.getNotOnOrAfter());
        Assertions.assertThat(subjectConfirmation.getSubjectConfirmationData().getInResponseTo()).isEqualTo(assertionRestrictions.getInResponseTo());
    }

    @Test
    public void transform_shouldTransformPersistentId() {
        PersistentId persistentId = PersistentIdBuilder.aPersistentId().build();
        HubAssertion assertion = HubAssertionBuilder.aHubAssertion().withPersistentId(persistentId).build();

        Subject subject = transformer.transform(assertion);

        NameID nameID = subject.getNameID();
        Assertions.assertThat(nameID.getValue()).isEqualTo(persistentId.getNameId());
        Assertions.assertThat(nameID.getFormat()).isEqualTo(NameIDType.PERSISTENT);

    }
}
