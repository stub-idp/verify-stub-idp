package uk.gov.ida.matchingserviceadapter.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import uk.gov.ida.matchingserviceadapter.validators.AssertionTimeRestrictionValidator;
import uk.gov.ida.matchingserviceadapter.validators.SubjectValidator;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static stubidp.saml.test.builders.NameIdBuilder.aNameId;
import static stubidp.saml.test.builders.SubjectBuilder.aSubject;
import static stubidp.saml.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static stubidp.saml.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;

@ExtendWith(MockitoExtension.class)
public class SubjectValidatorTest extends OpenSAMLRunner {
    private static final String IN_RESPONSE_TO = "_some-request-id";
    private SubjectValidator subjectValidator;

    @Mock
    private AssertionTimeRestrictionValidator timeRestrictionValidator;

    @BeforeEach
    public void setUp() {
        subjectValidator = new SubjectValidator(timeRestrictionValidator);
    }

    @Test
    public void shouldThrowExceptionWhenSubjectIsMissing() {
        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> subjectValidator.validate(null, IN_RESPONSE_TO))
                .withMessage("Subject is missing from the assertion.");
    }

    @Test
    public void shouldThrowExceptionWhenSubjectConfirmationDataMissing() {
        Subject subject = aSubject()
                .withSubjectConfirmation(aSubjectConfirmation().withSubjectConfirmationData(null).build())
                .build();

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> subjectValidator.validate(subject, IN_RESPONSE_TO))
                .withMessage("Subject confirmation data is missing from the assertion.");
    }

    @Test
    public void shouldThrowExceptionWhenSubjectConfirmationDataNotOnOrAfterIsMissing() {
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
    public void shouldThrowExceptionWhenSubjectConfirmationDataHasNoInResponseTo() {
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
    public void shouldThrowExceptionWhenNameIdIsMissing() {
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

    @Test
    public void shouldThrowExceptionWhenNameIdFormatIsMissing() {
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
                aSubjectConfirmationData()
                        .withInResponseTo(IN_RESPONSE_TO)
                        .build()).build();
        Subject subject = aSubject()
                .withSubjectConfirmation(subjectConfirmation)
                .withNameId(aNameId().withFormat(null).build())
                .build();

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> subjectValidator.validate(subject, IN_RESPONSE_TO))
                .withMessage("NameID format is missing or empty in the subject of the assertion.");
    }

    @Test
    public void shouldThrowExceptionWhenNameIdFormatIsEmpty() {
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
                aSubjectConfirmationData()
                        .withInResponseTo(IN_RESPONSE_TO)
                        .build()).build();
        Subject subject = aSubject()
                .withSubjectConfirmation(subjectConfirmation)
                .withNameId(aNameId().withFormat("").build())
                .build();

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> subjectValidator.validate(subject, IN_RESPONSE_TO))
                .withMessage("NameID format is missing or empty in the subject of the assertion.");
    }

    @Test
    public void shouldThrowExceptionWhenNameIdFormatIsNotValid() {
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
                aSubjectConfirmationData()
                        .withInResponseTo(IN_RESPONSE_TO)
                        .build()).build();
        Subject subject = aSubject()
                .withSubjectConfirmation(subjectConfirmation)
                .withNameId(aNameId().withFormat("invalid-nameid-format").build())
                .build();

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> subjectValidator.validate(subject, IN_RESPONSE_TO))
                .withMessage("NameID format is invalid in the subject of the assertion.");
    }
}