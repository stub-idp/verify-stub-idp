package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.metadata.TelephoneNumber;

import java.util.Optional;

public class TelephoneNumberBuilder {
    private Optional<String> value = Optional.ofNullable("01632 960000");// For more fake numbers, see http://stakeholders.ofcom.org.uk/telecoms/numbering/guidance-tele-no/numbers-for-drama

    private TelephoneNumberBuilder() {}

    public static TelephoneNumberBuilder aTelephoneNumber() {
        return new TelephoneNumberBuilder();
    }

    public TelephoneNumber build() {
        TelephoneNumber telephoneNumber = new org.opensaml.saml.saml2.metadata.impl.TelephoneNumberBuilder().buildObject();
        telephoneNumber.setValue(value.get());
        return telephoneNumber;
    }

    public TelephoneNumberBuilder withNumber(String number) {
        this.value = Optional.ofNullable(number);
        return this;
    }
}
