package stubidp.saml.hub.core.validators.subjectconfirmation;

import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.support.SamlTransformationErrorManagerTestHelper;
import stubidp.saml.test.builders.SubjectConfirmationDataBuilder;

import java.time.Instant;
import java.time.ZoneId;

import static stubidp.saml.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;

public class BasicAssertionSubjectConfirmationValidatorTest extends OpenSAMLRunner {

    private static final String REQUEST_ID = "some-request-id";
    private final BasicAssertionSubjectConfirmationValidator validator = new BasicAssertionSubjectConfirmationValidator();

    @Test
    public void validate_shouldThrowExceptionWhenSubjectConfirmationDataElementIsMissing() {
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(null).build();
        assertExceptionMessage(subjectConfirmation, SamlTransformationErrorFactory.missingSubjectConfirmationData());
    }

    @Test
    public void validate_shouldThrowExceptionWhenSubjectConfirmationDataRecipientAttributeIsMissing() {
        final SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(createSubjectConfirmationDataBuilder().withRecipient(null).build()).build();
        assertExceptionMessage(subjectConfirmation, SamlTransformationErrorFactory.missingBearerRecipient());
    }

    @Test
    public void validate_shouldThrowExceptionWhenSubjectConfirmationDataNotOnOrAfterAttributeIsMissing() {
        final SubjectConfirmation subjectConfirmation = aSubjectConfirmation()
                .withSubjectConfirmationData(createSubjectConfirmationDataBuilder().withNotOnOrAfter(null).build())
                .build();

        assertExceptionMessage(subjectConfirmation, SamlTransformationErrorFactory.missingNotOnOrAfter());
    }

    @Test
    public void validate_shouldThrowExceptionWhenSubjectConfirmationDataNotOnOrAfterIsNow() {
        Instant expiredTime = Instant.now().atZone(ZoneId.of("UTC")).minusHours(1).toInstant();
        final SubjectConfirmation subjectConfirmation = aSubjectConfirmation()
                .withSubjectConfirmationData(createSubjectConfirmationDataBuilder().withNotOnOrAfter(expiredTime).build())
                .build();

        assertExceptionMessage(subjectConfirmation, SamlTransformationErrorFactory.exceededNotOnOrAfter(expiredTime));
    }

    @Test
    public void validate_shouldThrowExceptionWhenSubjectConfirmationDataNotOnOrAfterHasBeenExceeded() {
        Instant expiredTime = Instant.now().atZone(ZoneId.of("UTC")).minusHours(1).toInstant();
        final SubjectConfirmation subjectConfirmation = aSubjectConfirmation()
                .withSubjectConfirmationData(createSubjectConfirmationDataBuilder().withNotOnOrAfter(expiredTime).build())
                .build();

        assertExceptionMessage(subjectConfirmation, SamlTransformationErrorFactory.exceededNotOnOrAfter(expiredTime));
    }

    @Test
    public void validate_shouldThrowExceptionWhenSubjectConfirmationDataNotBeforeAttributeIsSet() {
        final SubjectConfirmation subjectConfirmation = aSubjectConfirmation()
                .withSubjectConfirmationData(createSubjectConfirmationDataBuilder().withNotBefore(Instant.now()).build())
                .build();
        assertExceptionMessage(subjectConfirmation, SamlTransformationErrorFactory.notBeforeExists());
    }

    @Test
    public void validate_shouldDoNothingIfSubjectConfirmationDataHasAnAddressElement() {
        final SubjectConfirmationData subjectConfirmationData = createSubjectConfirmationDataBuilder().withAddress("address").build();
        final SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(subjectConfirmationData).build();
        validator.validate(subjectConfirmation);
    }

    @Test
    public void validate_shouldThrowExceptionIfSubjectConfirmationDataInResponseToAttributeIsMissing() {
        final SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(SubjectConfirmationDataBuilder.aSubjectConfirmationData().withInResponseTo(null).build()).build();
        assertExceptionMessage(subjectConfirmation, SamlTransformationErrorFactory.missingBearerInResponseTo());
    }

    private void assertExceptionMessage(
            final SubjectConfirmation subjectConfirmation,
            SamlValidationSpecificationFailure failure) {

        SamlTransformationErrorManagerTestHelper.validateFail(
                () -> validator.validate(subjectConfirmation),
                failure
        );
    }

    private SubjectConfirmationDataBuilder createSubjectConfirmationDataBuilder() {
        return SubjectConfirmationDataBuilder.aSubjectConfirmationData().withInResponseTo(REQUEST_ID);
    }
}
