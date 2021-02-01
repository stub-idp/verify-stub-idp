package uk.gov.ida.rp.testrp.builders;

import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.impl.AttributeImpl;
import stubidp.saml.extensions.extensions.impl.AddressImpl;
import stubidp.saml.extensions.extensions.impl.LineImpl;
import stubidp.saml.extensions.extensions.impl.PersonNameImpl;

import java.util.List;

public class AttributeBuilder {

    public class MyAttributeImpl extends AttributeImpl {
        public MyAttributeImpl() {
            super("urn:oasis:names:tc:SAML:2.0:assertion", "Attribute", "saml2");
        }
    }

    public class MyPersonNameImpl extends PersonNameImpl {
        public MyPersonNameImpl() {
            super("urn:oasis:names:tc:SAML:2.0:assertion", "AttributeValue", "saml2");
        }
    }

    public class MyAddressImpl extends AddressImpl {
        public MyAddressImpl() {
            super("urn:oasis:names:tc:SAML:2.0:assertion", "AttributeValue", "saml2");
        }
    }

    public class MyLineImpl extends LineImpl {
        public MyLineImpl(String line) {
            super("urn:oasis:names:tc:SAML:2.0:assertion", "AttributeValue", "saml2");
            setValue(line);
        }
    }

    private AttributeBuilder() {

    }

    public static AttributeBuilder anAttribute() {
        return new AttributeBuilder();
    }

    public Attribute withFirstName(String firstname) {
        final MyAttributeImpl myAttribute = new MyAttributeImpl();
        myAttribute.setName("firstname");
        myAttribute.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified");
        myAttribute.setFriendlyName("firstname");
        final MyPersonNameImpl myPersonName = new MyPersonNameImpl();
        myPersonName.setLanguage("en-GB");
        myPersonName.setValue(firstname);
        myPersonName.setVerified(false);
        myAttribute.getAttributeValues().add(myPersonName);
        return myAttribute;
    }

    public Attribute withSurname(String surname) {
        final MyAttributeImpl myAttribute = new MyAttributeImpl();
        myAttribute.setName("surname");
        myAttribute.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified");
        myAttribute.setFriendlyName("surname");
        final MyPersonNameImpl myPersonName = new MyPersonNameImpl();
        myPersonName.setLanguage("en-GB");
        myPersonName.setValue(surname);
        myPersonName.setVerified(false);
        myAttribute.getAttributeValues().add(myPersonName);
        return myAttribute;
    }

    public Attribute withAddressHistory(List<String> addresses) {
        final MyAttributeImpl myAttribute = new MyAttributeImpl();
        myAttribute.setName("surname");
        myAttribute.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified");
        myAttribute.setFriendlyName("addresshistory");

        for(String address : addresses) {
            final MyAddressImpl myAddress = new MyAddressImpl();
            myAddress.getLines().add(new MyLineImpl(address));
            myAddress.setVerified(false);
            myAttribute.getAttributeValues().add(myAddress);
        }
        return myAttribute;
    }

}
