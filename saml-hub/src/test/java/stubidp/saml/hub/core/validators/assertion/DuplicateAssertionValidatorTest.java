package stubidp.saml.hub.core.validators.assertion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import stubidp.saml.hub.validators.authnrequest.ConcurrentMapIdExpirationCache;
import stubidp.saml.hub.validators.authnrequest.IdExpirationCache;
import stubidp.saml.test.OpenSAMLRunner;

import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.authnStatementAlreadyReceived;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.duplicateMatchingDataset;
import static stubidp.saml.test.support.SamlTransformationErrorManagerTestHelper.validateFail;
import static stubidp.saml.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.test.builders.SubjectBuilder.aSubject;
import static stubidp.saml.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static stubidp.saml.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;

public class DuplicateAssertionValidatorTest extends OpenSAMLRunner {

    private ConcurrentMap<String, Instant> duplicateIds;
    private DuplicateAssertionValidator duplicateAssertionValidator;

    @BeforeEach
    public void setUp() {
        duplicateIds = new ConcurrentHashMap<>();
        duplicateIds.put("duplicate", Instant.now().atZone(ZoneId.of("UTC")).plusMinutes(5).toInstant());
        duplicateIds.put("expired-duplicate", Instant.now().atZone(ZoneId.of("UTC")).minusMinutes(2).toInstant());

        IdExpirationCache<String> idExpirationCache = new ConcurrentMapIdExpirationCache<>(duplicateIds);
        duplicateAssertionValidator = new DuplicateAssertionValidatorImpl(idExpirationCache);
    }

    @Test
    public void validateAuthnStatementAssertion_shouldPassIfTheAssertionIsNotADuplicateOfAPreviousOne() {
        Assertion assertion = anAssertion().withId("not-duplicate").buildUnencrypted();
        duplicateAssertionValidator.validateAuthnStatementAssertion(assertion);
    }

    @Test
    public void validateAuthnStatementAssertion_shouldPassIfTwoAssertionsHaveTheSameIdButTheFirstAssertionHasExpired() {
        Instant futureDate = Instant.now().atZone(ZoneId.of("UTC")).plusMinutes(6).toInstant();

        Assertion assertion = createAssertion("expired-duplicate", futureDate);
        duplicateAssertionValidator.validateAuthnStatementAssertion(assertion);

        assertThat(duplicateIds.get("expired-duplicate")).isEqualTo(futureDate);
    }

    @Test
    public void validateAuthnStatementAssertion_shouldThrowAnExceptionIfTheAssertionIsADuplicateOfAPreviousOne() {
        Assertion assertion = anAssertion().withId("duplicate").buildUnencrypted();
        validateFail(
            ()-> duplicateAssertionValidator.validateAuthnStatementAssertion(assertion),
            authnStatementAlreadyReceived("duplicate")
        );
    }

    @Test
    public void validateAuthnStatementAssertion_shouldStoreTheAssertionIdIfNotADuplicate() {
        Instant futureDate = Instant.now().atZone(ZoneId.of("UTC")).plusMinutes(6).toInstant();

        Assertion assertion = createAssertion("new-id", futureDate);
        duplicateAssertionValidator.validateAuthnStatementAssertion(assertion);

        assertThat(duplicateIds.get("new-id")).isEqualTo(futureDate);
    }

    @Test
    public void validateMatchingDataSetAssertion_shouldPassIfTheAssertionIsNotADuplicateOfAPreviousOne() {
        Assertion assertion = anAssertion().withId("not-duplicate").buildUnencrypted();
        duplicateAssertionValidator.validateMatchingDataSetAssertion(assertion, "issuer");
    }

    @Test
    public void validateMatchingDataSetAssertion_shouldPassIfTwoAssertionsHaveTheSameIdButTheFirstAssertionHasExpired() {
        Instant futureDate = Instant.now().atZone(ZoneId.of("UTC")).plusMinutes(6).toInstant();

        Assertion assertion = createAssertion("expired-duplicate", futureDate);
        duplicateAssertionValidator.validateMatchingDataSetAssertion(assertion, "issuer");

        assertThat(duplicateIds.get("expired-duplicate")).isEqualTo(futureDate);
    }

    @Test
    public void validateMatchingDataSetAssertion_shouldThrowAnExceptionIfTheAssertionIsADuplicateOfAPreviousOne() {
        Assertion assertion = anAssertion().withId("duplicate").buildUnencrypted();
        validateFail(
            ()-> duplicateAssertionValidator.validateMatchingDataSetAssertion(assertion, "issuer"),
            duplicateMatchingDataset("duplicate", "issuer")
        );
    }

    @Test
    public void validateMatchingDataSetAssertion_shouldStoreTheAssertionIdIfNotADuplicate() {
        Instant futureDate = Instant.now().atZone(ZoneId.of("UTC")).plusMinutes(6).toInstant();

        Assertion assertion = createAssertion("new-id", futureDate);
        duplicateAssertionValidator.validateMatchingDataSetAssertion(assertion, "issuer");

        assertThat(duplicateIds.get("new-id")).isEqualTo(futureDate);
    }

    private Assertion createAssertion(String id, Instant notOnOrAfter) {
        SubjectConfirmationData subjectConfirmationData = aSubjectConfirmationData()
            .withNotOnOrAfter(notOnOrAfter).build();
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation()
            .withSubjectConfirmationData(subjectConfirmationData).build();
        Subject subject = aSubject()
            .withSubjectConfirmation(subjectConfirmation).build();
        return anAssertion()
            .withId(id)
            .withSubject(subject)
            .buildUnencrypted();
    }
}
