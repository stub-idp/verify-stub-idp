package stubidp.saml.extensions.extensions.impl;

import stubidp.saml.extensions.extensions.StringBasedMdsAttributeValue;

import java.time.LocalDate;

public class StringBasedMdsAttributeValueImpl extends StringValueSamlObjectImpl implements StringBasedMdsAttributeValue {

    private LocalDate fromTime;
    private LocalDate toTime;
    private boolean verified;

    protected StringBasedMdsAttributeValueImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    @Override
    public LocalDate getFrom() {
        return fromTime;
    }

    @Override
    public void setFrom(LocalDate fromTime) {
        this.fromTime = fromTime;
    }

    @Override
    public LocalDate getTo() {
        return toTime;
    }

    @Override
    public void setTo(LocalDate toTime) {
        this.toTime = toTime;
    }

    @Override
    public boolean getVerified() {
        return verified;
    }

    @Override
    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
