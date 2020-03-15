package stubidp.eidas.metadata.support.builders;

import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import stubidp.eidas.metadata.support.TestSamlObjectFactory;
import stubidp.test.devpki.TestEntityIds;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;

public class SubjectBuilder {
    private static TestSamlObjectFactory testSamlObjectFactory = new TestSamlObjectFactory();
    public static final int NOT_ON_OR_AFTER_DEFAULT_PERIOD_MINS = 15;
    public static SubjectBuilder aSubject() {
        return new SubjectBuilder();
    }

    public Subject build() {
        Subject subject = testSamlObjectFactory.createSubject();

        subject.setNameID(buildNameID());
        subject.getSubjectConfirmations().addAll(Collections.singletonList(buildSubjectConfirmation()));

        return subject;
    }

    private SubjectConfirmation buildSubjectConfirmation() {
        String method = SubjectConfirmation.METHOD_BEARER;

        SubjectConfirmation subjectConfirmation
                = testSamlObjectFactory.createSubjectConfirmation();
        subjectConfirmation.setMethod(method);

        SubjectConfirmationData subjectConfirmationData
                = buildSubjectConfirmationData();
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);

        return subjectConfirmation;
    }


    private NameID buildNameID() {
        NameID nameId = testSamlObjectFactory.createNameId(null);
        nameId.setFormat(null);

        nameId.setFormat(NameIDType.PERSISTENT);

        return nameId;
    }

    private SubjectConfirmationData buildSubjectConfirmationData() {
        SubjectConfirmationData subjectConfirmationData = testSamlObjectFactory.createSubjectConfirmationData();
        subjectConfirmationData.setRecipient(TestEntityIds.HUB_ENTITY_ID);
        subjectConfirmationData.setNotOnOrAfter(Instant.now().atZone(ZoneId.of("UTC")).plusMinutes(NOT_ON_OR_AFTER_DEFAULT_PERIOD_MINS).toInstant());
        subjectConfirmationData.setInResponseTo(ResponseBuilder.DEFAULT_REQUEST_ID);
        return subjectConfirmationData;
    }

}
