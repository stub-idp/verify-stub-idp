package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import stubidp.saml.test.OpenSamlXmlObjectFactory;

import java.util.Optional;

public class NameIdBuilder {
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    private String value = "default-pid";
    private Optional<String> format = Optional.of(NameIDType.PERSISTENT);
    private Optional<String> nameQualifier = Optional.empty();
    private Optional<String> spNameQualifier = Optional.empty();

    private NameIdBuilder() {}

    public static NameIdBuilder aNameId() {
        return new NameIdBuilder();
    }

    public NameID build() {
        NameID nameId = openSamlXmlObjectFactory.createNameId(value);
        nameId.setFormat(null);
        format.ifPresent(nameId::setFormat);
        nameQualifier.ifPresent(nameId::setNameQualifier);
        spNameQualifier.ifPresent(nameId::setSPNameQualifier);
        return nameId;
    }

    public NameIdBuilder withValue(String value) {
        this.value = value;
        return this;
    }

    public NameIdBuilder withFormat(String format) {
        this.format = Optional.ofNullable(format);
        return this;
    }

    public NameIdBuilder withNameQualifier(String nameQualifier) {
        this.nameQualifier = Optional.ofNullable(nameQualifier);
        return this;
    }

    public NameIdBuilder withSpNameQualifier(String spNameQualifier) {
        this.spNameQualifier = Optional.ofNullable(spNameQualifier);
        return this;
    }
}
