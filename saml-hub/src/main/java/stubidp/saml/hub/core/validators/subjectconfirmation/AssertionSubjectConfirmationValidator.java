package stubidp.saml.hub.core.validators.subjectconfirmation;

import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;

public class AssertionSubjectConfirmationValidator extends BasicAssertionSubjectConfirmationValidator {

    public void validate(
            SubjectConfirmation subjectConfirmation,
            String requestId,
            String expectedRecipientId) {

        super.validate(subjectConfirmation);

        SubjectConfirmationData subjectConfirmationData = subjectConfirmation.getSubjectConfirmationData();

        if (!subjectConfirmationData.getInResponseTo().equals(requestId)) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.notMatchInResponseTo(subjectConfirmationData.getInResponseTo(), requestId);
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
        if (!subjectConfirmationData.getRecipient().equals(expectedRecipientId)) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.incorrectRecipientFormat(subjectConfirmationData.getRecipient(), expectedRecipientId);
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
    }
 }
