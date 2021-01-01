package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.metadata.Company;
import org.opensaml.saml.saml2.metadata.ContactPerson;
import org.opensaml.saml.saml2.metadata.EmailAddress;
import org.opensaml.saml.saml2.metadata.GivenName;
import org.opensaml.saml.saml2.metadata.SurName;
import org.opensaml.saml.saml2.metadata.TelephoneNumber;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContactPersonBuilder {
    private final Optional<GivenName> givenName = Optional.ofNullable(GivenNameBuilder.aGivenName().build());
    private Optional<Company> company = Optional.ofNullable(CompanyBuilder.aCompany().build());
    private final Optional<SurName> surName = Optional.ofNullable(SurNameBuilder.aSurName().build());
    private final List<EmailAddress> emailAddresses = new ArrayList<>();
    private boolean addDefaultEmailAddress = true;
    private final List<TelephoneNumber> telephoneNumbers = new ArrayList<>();
    private boolean addDefaultTelephoneNumber = true;

    private ContactPersonBuilder() {}

    public static ContactPersonBuilder aContactPerson() {
        return new ContactPersonBuilder();
    }

    public ContactPerson build() {
        ContactPerson contactPerson = new org.opensaml.saml.saml2.metadata.impl.ContactPersonBuilder().buildObject();

        givenName.ifPresent(contactPerson::setGivenName);
        surName.ifPresent(contactPerson::setSurName);
        company.ifPresent(contactPerson::setCompany);

        if (addDefaultEmailAddress) {
            emailAddresses.add(EmailAddressBuilder.anEmailAddress().build());
        }
        contactPerson.getEmailAddresses().addAll(emailAddresses);

        if (addDefaultTelephoneNumber) {
            telephoneNumbers.add(TelephoneNumberBuilder.aTelephoneNumber().build());
        }
        contactPerson.getTelephoneNumbers().addAll(telephoneNumbers);

        return contactPerson;
    }

    public ContactPersonBuilder withCompany(Company company) {
        this.company = Optional.ofNullable(company);
        return this;
    }

    public ContactPersonBuilder addEmailAddress(EmailAddress emailAddress) {
        this.emailAddresses.add(emailAddress);
        this.addDefaultEmailAddress = false;
        return this;
    }

    public ContactPersonBuilder addTelephoneNumber(TelephoneNumber number) {
        this.telephoneNumbers.add(number);
        this.addDefaultTelephoneNumber = false;
        return this;
    }
}
