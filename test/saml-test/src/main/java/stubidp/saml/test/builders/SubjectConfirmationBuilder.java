package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import stubidp.saml.test.OpenSamlXmlObjectFactory;

import java.util.Optional;

public class SubjectConfirmationBuilder {
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    private Optional<String> method = Optional.of(SubjectConfirmation.METHOD_BEARER);
    private Optional<SubjectConfirmationData> subjectConfirmationData = Optional.of(SubjectConfirmationDataBuilder.aSubjectConfirmationData().build());

    private SubjectConfirmationBuilder() {}

    public static SubjectConfirmationBuilder aSubjectConfirmation() {
        return new SubjectConfirmationBuilder();
    }

    public SubjectConfirmation build() {
        SubjectConfirmation subjectConfirmation = openSamlXmlObjectFactory.createSubjectConfirmation();

        method.ifPresent(subjectConfirmation::setMethod);

        subjectConfirmationData.ifPresent(subjectConfirmation::setSubjectConfirmationData);

        return subjectConfirmation;
    }

    public SubjectConfirmationBuilder withMethod(String method) {
        this.method = Optional.ofNullable(method);
        return this;
    }

    public SubjectConfirmationBuilder withSubjectConfirmationData(SubjectConfirmationData subjectConfirmationData) {
        this.subjectConfirmationData = Optional.ofNullable(subjectConfirmationData);
        return this;
    }
}
