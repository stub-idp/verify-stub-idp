package stubidp.saml.extensions.extensions.impl;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import stubidp.saml.extensions.extensions.Address;
import stubidp.saml.extensions.extensions.InternationalPostCode;
import stubidp.saml.extensions.extensions.Line;
import stubidp.saml.extensions.extensions.PostCode;
import stubidp.saml.extensions.extensions.UPRN;

import javax.xml.namespace.QName;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddressImpl extends XSAnyImpl implements Address {
    private final List<Line> lines = new ArrayList<>();
    private PostCode postCode;
    private Instant from;
    private Instant to;
    private InternationalPostCode internationalPostCode;
    private UPRN uprn;
    private boolean verified;

    AddressImpl(String namespaceURI, String localName, String namespacePrefix) {
        this(namespaceURI, localName, namespacePrefix, Address.TYPE_NAME);
    }

    AddressImpl(String namespaceURI, String localName, String namespacePrefix, QName typeName) {
        super(namespaceURI, localName, namespacePrefix);
        super.setSchemaType(typeName);
    }

    @Override
    public Instant getFrom() {
        return from;
    }

    @Override
    public void setFrom(Instant from) {
        this.from = prepareForAssignment(this.from, from);
    }

    @Override
    public Instant getTo() {
        return to;
    }

    @Override
    public void setTo(Instant to) {
        this.to = prepareForAssignment(this.to, to);
    }

    @Override
    public boolean getVerified() {
        return verified;
    }

    @Override
    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    @Override
    public List<Line> getLines() {
        return lines;
    }

    @Override
    public PostCode getPostCode() {
        return postCode;
    }

    @Override
    public void setPostCode(PostCode postCode) {
        this.postCode = postCode;
    }

    @Override
    public InternationalPostCode getInternationalPostCode() {
        return internationalPostCode;
    }

    @Override
    public void setInternationalPostCode(InternationalPostCode internationalPostCode) {
        this.internationalPostCode = internationalPostCode;
    }

    @Override
    public UPRN getUPRN() {
        return uprn;
    }

    @Override
    public void setUPRN(UPRN uprn) {
        this.uprn = uprn;
    }

    @Override
    public List<XMLObject> getOrderedChildren() {

        List<XMLObject> children = new ArrayList<>(lines);
        if (postCode != null) {
            children.add(postCode);
        }
        if (internationalPostCode != null) {
            children.add(internationalPostCode);
        }
        if (uprn != null) {
            children.add(uprn);
        }

        return Collections.unmodifiableList(children);
    }
}
