package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.core.Attribute;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.Gender;
import stubidp.saml.test.OpenSamlXmlObjectFactory;

import java.time.LocalDate;
import java.util.Optional;

public class GenderAttributeBuilder_1_1 {
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();

    private Optional<LocalDate> from = Optional.empty();
    private Optional<LocalDate> to = Optional.empty();
    private Optional<String> value = Optional.empty();
    private boolean verified = false;

    private GenderAttributeBuilder_1_1() {}

    public static GenderAttributeBuilder_1_1 aGender_1_1() {
        return new GenderAttributeBuilder_1_1();
    }

    public Attribute build() {

        Attribute genderAttribute = openSamlXmlObjectFactory.createAttribute();
        genderAttribute.setFriendlyName(IdaConstants.Attributes_1_1.Gender.FRIENDLY_NAME);
        genderAttribute.setName(IdaConstants.Attributes_1_1.Gender.NAME);
        genderAttribute.setNameFormat(Attribute.UNSPECIFIED);

        return buildAttribute(genderAttribute);
    }

    public Attribute buildEidasGender() {

        Attribute genderAttribute = openSamlXmlObjectFactory.createAttribute();
        genderAttribute.setFriendlyName(IdaConstants.Eidas_Attributes.Gender.FRIENDLY_NAME);
        genderAttribute.setName(IdaConstants.Eidas_Attributes.Gender.NAME);
        genderAttribute.setNameFormat(Attribute.UNSPECIFIED);

        return buildAttribute(genderAttribute);
    }

    private Attribute buildAttribute(Attribute genderAttribute) {
        Gender genderAttributeValue = openSamlXmlObjectFactory.createGenderAttributeValue(value.orElse("Male"));
        from.ifPresent(genderAttributeValue::setFrom);
        to.ifPresent(genderAttributeValue::setTo);
        genderAttributeValue.setVerified(verified);
        genderAttribute.getAttributeValues().add(genderAttributeValue);
        return genderAttribute;
    }

    public GenderAttributeBuilder_1_1 withFrom(LocalDate from) {
        this.from = Optional.ofNullable(from);
        return this;
    }

    public GenderAttributeBuilder_1_1 withTo(LocalDate to) {
        this.to = Optional.ofNullable(to);
        return this;
    }

    public GenderAttributeBuilder_1_1 withValue(String name) {
        this.value = Optional.ofNullable(name);
        return this;
    }

    public GenderAttributeBuilder_1_1 withVerified(boolean verified) {
        this.verified = verified;
        return this;
    }
}
