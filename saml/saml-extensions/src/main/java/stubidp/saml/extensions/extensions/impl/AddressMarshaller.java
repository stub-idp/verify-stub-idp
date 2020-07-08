package stubidp.saml.extensions.extensions.impl;

import org.opensaml.core.xml.io.Marshaller;
import stubidp.saml.extensions.extensions.Address;

public class AddressMarshaller extends BaseMdsSamlObjectMarshaller {

    public static final Marshaller MARSHALLER = new AddressMarshaller();

    protected AddressMarshaller(){
        super(Address.TYPE_LOCAL_NAME);
    }
}
