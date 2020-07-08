package stubidp.saml.extensions.extensions.impl;

import stubidp.saml.extensions.extensions.StringBasedMdsAttributeValue;

import java.time.Instant;

public class StringBasedMdsAttributeValueImpl extends StringValueSamlObjectImpl implements StringBasedMdsAttributeValue {

    private Instant fromTime;
    private Instant toTime;
    private boolean verified;

    protected StringBasedMdsAttributeValueImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    @Override
    public Instant getFrom() {
        return fromTime;
    }

    @Override
    public void setFrom(Instant fromTime) {
        this.fromTime = fromTime;
    }

    @Override
    public Instant getTo() {
        return toTime;
    }

    @Override
    public void setTo(Instant toTime) {
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
