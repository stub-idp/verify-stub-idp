package stubidp.stubidp.builders;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;

import stubidp.saml.security.SignatureFactory;

import static stubidp.stubidp.StubIdpEidasBinder.COUNTRY_METADATA_SIGNATURE_FACTORY;

@Singleton
public class CountryMetadataSigningHelper {
    private final SignatureFactory signatureFactory;

    @Inject
    public CountryMetadataSigningHelper(@Named(COUNTRY_METADATA_SIGNATURE_FACTORY) SignatureFactory signatureFactory) {
        this.signatureFactory = signatureFactory;
    }

    public <T extends SignableSAMLObject> T sign(T signableSAMLObject) throws MarshallingException, SignatureException {
        signableSAMLObject.setSignature(signatureFactory.createSignature());
        XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(signableSAMLObject).marshall(signableSAMLObject);
        Signer.signObject(signableSAMLObject.getSignature());

        return signableSAMLObject;
    }
}
