package stubidp.saml.hub.core.validators.subjectconfirmation;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.hub.core.DateTimeFreezer;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;
import stubidp.saml.hub.core.OpenSAMLRunner;
import stubidp.saml.utils.core.test.SamlTransformationErrorManagerTestHelper;
import stubidp.saml.utils.core.test.builders.SubjectConfirmationDataBuilder;

import static stubidp.saml.utils.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;

public class BasicAssertionSubjectConfirmationValidatorTest extends OpenSAMLRunner {

    private static final String REQUEST_ID = "some-request-id";

    private BasicAssertionSubjectConfirmationValidator validator;

    @BeforeEach
    public void setup() {
        validator = new BasicAssertionSubjectConfirmationValidator();
    }

    @AfterEach
    public void teardown() {
        DateTimeFreezer.unfreezeTime();
    }

    @Test
    public void validate_shouldThrowExceptionWhenSubjectConfirmationDataElementIsMissing() throws Exception {
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(null).build();
        assertExceptionMessage(subjectConfirmation, SamlTransformationErrorFactory.missingSubjectConfirmationData());
    }

    @Test
    public void validate_shouldThrowExceptionWhenSubjectConfirmationDataRecipientAttributeIsMissing() throws Exception {
        final SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(createSubjectConfirmationDataBuilder().withRecipient(null).build()).build();
        assertExceptionMessage(subjectConfirmation, SamlTransformationErrorFactory.missingBearerRecipient());
    }

    @Test
    public void validate_shouldThrowExceptionWhenSubjectConfirmationDataNotOnOrAfterAttributeIsMissing() throws Exception {
        final SubjectConfirmation subjectConfirmation = aSubjectConfirmation()
                .withSubjectConfirmationData(createSubjectConfirmationDataBuilder().withNotOnOrAfter(null).build())
                .build();

        assertExceptionMessage(subjectConfirmation, SamlTransformationErrorFactory.missingNotOnOrAfter());
    }

    @Test
    public void validate_shouldThrowExceptionWhenSubjectConfirmationDataNotOnOrAfterIsNow() throws Exception {
        DateTimeFreezer.freezeTime();
        DateTime expiredTime = DateTime.now(DateTimeZone.UTC);
        final SubjectConfirmation subjectConfirmation = aSubjectConfirmation()
                .withSubjectConfirmationData(createSubjectConfirmationDataBuilder().withNotOnOrAfter(expiredTime).build())
                .build();

        assertExceptionMessage(subjectConfirmation, SamlTransformationErrorFactory.exceededNotOnOrAfter(expiredTime));
    }

    @Test
    public void validate_shouldThrowExceptionWhenSubjectConfirmationDataNotOnOrAfterHasBeenExceeded() throws Exception {
        DateTimeFreezer.freezeTime();
        DateTime expiredTime = DateTime.now(DateTimeZone.UTC).minus(1);
        final SubjectConfirmation subjectConfirmation = aSubjectConfirmation()
                .withSubjectConfirmationData(createSubjectConfirmationDataBuilder().withNotOnOrAfter(expiredTime).build())
                .build();

        assertExceptionMessage(subjectConfirmation, SamlTransformationErrorFactory.exceededNotOnOrAfter(expiredTime));
    }

    @Test
    public void validate_shouldThrowExceptionWhenSubjectConfirmationDataNotBeforeAttributeIsSet() throws Exception {
        final SubjectConfirmation subjectConfirmation = aSubjectConfirmation()
                .withSubjectConfirmationData(createSubjectConfirmationDataBuilder().withNotBefore(DateTime.now()).build())
                .build();
        assertExceptionMessage(subjectConfirmation, SamlTransformationErrorFactory.notBeforeExists());
    }

    @Test
    public void validate_shouldDoNothingIfSubjectConfirmationDataHasAnAddressElement() throws Exception {
        final SubjectConfirmationData subjectConfirmationData = createSubjectConfirmationDataBuilder().withAddress("address").build();
        final SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(subjectConfirmationData).build();
        validator.validate(subjectConfirmation);
    }

    @Test
    public void validate_shouldThrowExceptionIfSubjectConfirmationDataInResponseToAttributeIsMissing() throws Exception {
        final SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(SubjectConfirmationDataBuilder.aSubjectConfirmationData().withInResponseTo(null).build()).build();
        assertExceptionMessage(subjectConfirmation, SamlTransformationErrorFactory.missingBearerInResponseTo());
    }

    private void assertExceptionMessage(
            final SubjectConfirmation subjectConfirmation,
            SamlValidationSpecificationFailure failure) {

        SamlTransformationErrorManagerTestHelper.validateFail(
                new SamlTransformationErrorManagerTestHelper.Action() {
                    @Override
                    public void execute() {
                        validator.validate(subjectConfirmation);
                    }
                },
                failure
        );
    }

    private SubjectConfirmationDataBuilder createSubjectConfirmationDataBuilder() {
        return SubjectConfirmationDataBuilder.aSubjectConfirmationData().withInResponseTo(REQUEST_ID);
    }
}
