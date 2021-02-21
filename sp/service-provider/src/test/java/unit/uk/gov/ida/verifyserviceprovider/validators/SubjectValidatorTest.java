package unit.uk.gov.ida.verifyserviceprovider.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.validators.SubjectValidator;
import uk.gov.ida.verifyserviceprovider.validators.TimeRestrictionValidator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static stubidp.saml.test.builders.SubjectBuilder.aSubject;
import static stubidp.saml.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static stubidp.saml.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;

@ExtendWith({MockitoExtension.class})
public class SubjectValidatorTest extends OpenSAMLRunner {
    private static final String IN_RESPONSE_TO = "_some-request-id";
    private SubjectValidator subjectValidator;

    @Mock
    private TimeRestrictionValidator timeRestrictionValidator;

    @BeforeEach
    public void setUp() {
        subjectValidator = new SubjectValidator(timeRestrictionValidator);
    }

    @Test
    public void shouldThrowExceptionWhenSubjectIsMissing() throws Exception {
        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> subjectValidator.validate(null, IN_RESPONSE_TO))
                .withMessage("Subject is missing from the assertion.");
    }

    @Test
    public void shouldThrowExceptionWhenMultipleSubjectConfirmation() throws Exception {
        Subject subject = aSubject().build();
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().build();
        subject.getSubjectConfirmations().addAll(List.of(subjectConfirmation, subjectConfirmation));

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> subjectValidator.validate(subject, IN_RESPONSE_TO))
                .withMessage("Exactly one subject confirmation is expected.");
    }

    @Test
    public void shouldThrowExceptionWhenSubjectConfirmationMethodIsNotBearer() throws Exception {
        Subject subject = aSubject()
                .withSubjectConfirmation(aSubjectConfirmation().withMethod("anything-but-not-bearer").build())
                .build();

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> subjectValidator.validate(subject, IN_RESPONSE_TO))
                .withMessage("Subject confirmation method must be 'bearer'.");
    }

    @Test
    public void shouldThrowExceptionWhenSubjectConfirmationDataMissing() throws Exception {
        Subject subject = aSubject()
                .withSubjectConfirmation(aSubjectConfirmation().withSubjectConfirmationData(null).build())
                .build();

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> subjectValidator.validate(subject, IN_RESPONSE_TO))
                .withMessage("Subject confirmation data is missing from the assertion.");
    }

    @Test
    public void shouldThrowExceptionWhenSubjectConfirmationDataNotOnOrAfterIsMissing() throws Exception {
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
                aSubjectConfirmationData().withNotOnOrAfter(null).build()).build();
        Subject subject = aSubject()
                .withSubjectConfirmation(subjectConfirmation)
                .build();

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> subjectValidator.validate(subject, IN_RESPONSE_TO))
                .withMessage("Subject confirmation data must contain 'NotOnOrAfter'.");
    }

    @Test
    public void shouldThrowExceptionWhenSubjectConfirmationDataHasNoInResponseTo() throws Exception {
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
                aSubjectConfirmationData()
                        .withInResponseTo(null)
                        .build()).build();
        Subject subject = aSubject()
                .withSubjectConfirmation(subjectConfirmation)
                .build();

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> subjectValidator.validate(subject, IN_RESPONSE_TO))
                .withMessage("Subject confirmation data must contain 'InResponseTo'.");
    }

    @Test
    public void shouldThrowExceptionWhenInResponseToRequestIdDoesNotMatchTheRequestId() throws Exception {
        String expectedInResponseTo = "some-non-matching-request-id";

        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
                aSubjectConfirmationData()
                        .withInResponseTo(IN_RESPONSE_TO)
                        .build()).build();
        Subject subject = aSubject()
                .withSubjectConfirmation(subjectConfirmation)
                .build();

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> subjectValidator.validate(subject, expectedInResponseTo))
                .withMessage("'InResponseTo' must match requestId. Expected " + expectedInResponseTo + " but was " + IN_RESPONSE_TO);
    }

    @Test
    public void shouldThrowExceptionWhenNameIdIsMissing() throws Exception {
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
                aSubjectConfirmationData()
                        .withInResponseTo(IN_RESPONSE_TO)
                        .build()).build();
        Subject subject = aSubject()
                .withSubjectConfirmation(subjectConfirmation)
                .withNameId(null)
                .build();

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> subjectValidator.validate(subject, IN_RESPONSE_TO))
                .withMessage("NameID is missing from the subject of the assertion.");
    }


}