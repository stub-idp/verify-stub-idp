package stubidp.saml.stubidp.test.builders;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.test.builders.ResponseBuilder;
import stubidp.test.devpki.TestEntityIds;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubjectConfirmationDataBuilder {

    public static final int NOT_ON_OR_AFTER_DEFAULT_PERIOD_MINS = 15;

    private OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    private Optional<String> recipient = Optional.of(TestEntityIds.HUB_ENTITY_ID);
    private Optional<Instant> notOnOrAfter = Optional.ofNullable(Instant.now().atZone(ZoneId.of("UTC")).plusMinutes(NOT_ON_OR_AFTER_DEFAULT_PERIOD_MINS).toInstant());
    private Optional<Instant> notBefore = Optional.empty();
    private Optional<String> address = Optional.empty();
    private Optional<String> inResponseTo = Optional.of(ResponseBuilder.DEFAULT_REQUEST_ID);
    private List<Assertion> assertions = new ArrayList<>();
    private List<EncryptedAssertion> encryptedAssertions = new ArrayList<>();

    public static SubjectConfirmationDataBuilder aSubjectConfirmationData() {
        return new SubjectConfirmationDataBuilder();
    }

    public SubjectConfirmationData build() {
        SubjectConfirmationData subjectConfirmationData = openSamlXmlObjectFactory.createSubjectConfirmationData();
        recipient.ifPresent(subjectConfirmationData::setRecipient);
        notOnOrAfter.ifPresent(subjectConfirmationData::setNotOnOrAfter);
        notBefore.ifPresent(subjectConfirmationData::setNotBefore);
        inResponseTo.ifPresent(subjectConfirmationData::setInResponseTo);
        address.ifPresent(subjectConfirmationData::setAddress);
        subjectConfirmationData.getUnknownXMLObjects().addAll(assertions);
        subjectConfirmationData.getUnknownXMLObjects().addAll(encryptedAssertions);
        return subjectConfirmationData;
    }

    public SubjectConfirmationDataBuilder withRecipient(String recipient) {
        this.recipient = Optional.ofNullable(recipient);
        return this;
    }

    public SubjectConfirmationDataBuilder withNotOnOrAfter(Instant notOnOrAfter) {
        this.notOnOrAfter = Optional.ofNullable(notOnOrAfter);
        return this;
    }

    public SubjectConfirmationDataBuilder withNotBefore(Instant notBefore) {
        this.notBefore = Optional.ofNullable(notBefore);
        return this;
    }

    public SubjectConfirmationDataBuilder withAddress(String address) {
        this.address = Optional.ofNullable(address);
        return this;
    }

    public SubjectConfirmationDataBuilder withInResponseTo(String inResponseTo) {
        this.inResponseTo = Optional.ofNullable(inResponseTo);
        return this;
    }

    public SubjectConfirmationDataBuilder addAssertion(Assertion assertion) {
        this.assertions.add(assertion);
        return this;
    }

    public SubjectConfirmationDataBuilder addAssertions(List<Assertion> assertions) {
        this.assertions.addAll(assertions);
        return this;
    }

    public SubjectConfirmationDataBuilder addAssertion(final EncryptedAssertion assertion) {
        this.encryptedAssertions.add(assertion);
        return this;
    }
}
