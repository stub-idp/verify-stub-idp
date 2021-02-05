package stubidp.saml.extensions;

import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.XSString;
import stubidp.saml.extensions.extensions.Address;
import stubidp.saml.extensions.extensions.Date;
import stubidp.saml.extensions.extensions.Gender;
import stubidp.saml.extensions.extensions.Gpg45Status;
import stubidp.saml.extensions.extensions.IPAddress;
import stubidp.saml.extensions.extensions.IdpFraudEventId;
import stubidp.saml.extensions.extensions.InternationalPostCode;
import stubidp.saml.extensions.extensions.Line;
import stubidp.saml.extensions.extensions.PersonName;
import stubidp.saml.extensions.extensions.PostCode;
import stubidp.saml.extensions.extensions.RequestedAttribute;
import stubidp.saml.extensions.extensions.RequestedAttributes;
import stubidp.saml.extensions.extensions.SPType;
import stubidp.saml.extensions.extensions.StatusValue;
import stubidp.saml.extensions.extensions.StringBasedMdsAttributeValue;
import stubidp.saml.extensions.extensions.UPRN;
import stubidp.saml.extensions.extensions.Verified;
import stubidp.saml.extensions.extensions.eidas.BirthName;
import stubidp.saml.extensions.extensions.eidas.CountrySamlResponse;
import stubidp.saml.extensions.extensions.eidas.CurrentAddress;
import stubidp.saml.extensions.extensions.eidas.CurrentFamilyName;
import stubidp.saml.extensions.extensions.eidas.CurrentGivenName;
import stubidp.saml.extensions.extensions.eidas.DateOfBirth;
import stubidp.saml.extensions.extensions.eidas.EidasGender;
import stubidp.saml.extensions.extensions.eidas.EncryptedAssertionKeys;
import stubidp.saml.extensions.extensions.eidas.PersonIdentifier;
import stubidp.saml.extensions.extensions.eidas.PlaceOfBirth;
import stubidp.saml.extensions.extensions.eidas.impl.BirthNameBuilder;
import stubidp.saml.extensions.extensions.eidas.impl.BirthNameMarshaller;
import stubidp.saml.extensions.extensions.eidas.impl.BirthNameUnmarshaller;
import stubidp.saml.extensions.extensions.eidas.impl.CountrySamlResponseBuilder;
import stubidp.saml.extensions.extensions.eidas.impl.CountrySamlResponseMarshaller;
import stubidp.saml.extensions.extensions.eidas.impl.CountrySamlResponseUnmarshaller;
import stubidp.saml.extensions.extensions.eidas.impl.CurrentAddressBuilder;
import stubidp.saml.extensions.extensions.eidas.impl.CurrentAddressMarshaller;
import stubidp.saml.extensions.extensions.eidas.impl.CurrentAddressUnmarshaller;
import stubidp.saml.extensions.extensions.eidas.impl.CurrentFamilyNameBuilder;
import stubidp.saml.extensions.extensions.eidas.impl.CurrentFamilyNameMarshaller;
import stubidp.saml.extensions.extensions.eidas.impl.CurrentFamilyNameUnmarshaller;
import stubidp.saml.extensions.extensions.eidas.impl.CurrentGivenNameBuilder;
import stubidp.saml.extensions.extensions.eidas.impl.CurrentGivenNameMarshaller;
import stubidp.saml.extensions.extensions.eidas.impl.CurrentGivenNameUnmarshaller;
import stubidp.saml.extensions.extensions.eidas.impl.DateOfBirthBuilder;
import stubidp.saml.extensions.extensions.eidas.impl.DateOfBirthMarshaller;
import stubidp.saml.extensions.extensions.eidas.impl.DateOfBirthUnmarshaller;
import stubidp.saml.extensions.extensions.eidas.impl.EidasGenderBuilder;
import stubidp.saml.extensions.extensions.eidas.impl.EidasGenderMarshaller;
import stubidp.saml.extensions.extensions.eidas.impl.EidasGenderUnmarshaller;
import stubidp.saml.extensions.extensions.eidas.impl.EncryptedAssertionKeysBuilder;
import stubidp.saml.extensions.extensions.eidas.impl.EncryptedAssertionKeysMarshaller;
import stubidp.saml.extensions.extensions.eidas.impl.EncryptedAssertionKeysUnmarshaller;
import stubidp.saml.extensions.extensions.eidas.impl.PersonIdentifierBuilder;
import stubidp.saml.extensions.extensions.eidas.impl.PersonIdentifierMarshaller;
import stubidp.saml.extensions.extensions.eidas.impl.PersonIdentifierUnmarshaller;
import stubidp.saml.extensions.extensions.eidas.impl.PlaceOfBirthBuilder;
import stubidp.saml.extensions.extensions.eidas.impl.PlaceOfBirthMarshaller;
import stubidp.saml.extensions.extensions.eidas.impl.PlaceOfBirthUnmarshaller;
import stubidp.saml.extensions.extensions.impl.AddressBuilder;
import stubidp.saml.extensions.extensions.impl.AddressMarshaller;
import stubidp.saml.extensions.extensions.impl.AddressUnmarshaller;
import stubidp.saml.extensions.extensions.impl.DateBuilder;
import stubidp.saml.extensions.extensions.impl.DateImpl;
import stubidp.saml.extensions.extensions.impl.GenderBuilder;
import stubidp.saml.extensions.extensions.impl.GenderImpl;
import stubidp.saml.extensions.extensions.impl.Gpg45StatusBuilder;
import stubidp.saml.extensions.extensions.impl.Gpg45StatusImpl;
import stubidp.saml.extensions.extensions.impl.IPAddressBuilder;
import stubidp.saml.extensions.extensions.impl.IPAddressImpl;
import stubidp.saml.extensions.extensions.impl.IdpFraudEventIdBuilder;
import stubidp.saml.extensions.extensions.impl.IdpFraudEventIdImpl;
import stubidp.saml.extensions.extensions.impl.InternationalPostCodeBuilder;
import stubidp.saml.extensions.extensions.impl.LineBuilder;
import stubidp.saml.extensions.extensions.impl.PersonNameBuilder;
import stubidp.saml.extensions.extensions.impl.PersonNameImpl;
import stubidp.saml.extensions.extensions.impl.PostCodeBuilder;
import stubidp.saml.extensions.extensions.impl.RequestedAttributeBuilder;
import stubidp.saml.extensions.extensions.impl.RequestedAttributeImpl;
import stubidp.saml.extensions.extensions.impl.RequestedAttributesBuilder;
import stubidp.saml.extensions.extensions.impl.RequestedAttributesImpl;
import stubidp.saml.extensions.extensions.impl.SPTypeBuilder;
import stubidp.saml.extensions.extensions.impl.SPTypeImpl;
import stubidp.saml.extensions.extensions.impl.StatusValueBuilder;
import stubidp.saml.extensions.extensions.impl.StatusValueImpl;
import stubidp.saml.extensions.extensions.impl.StringBasedMdsAttributeValueBuilder;
import stubidp.saml.extensions.extensions.impl.StringBasedMdsAttributeValueImpl;
import stubidp.saml.extensions.extensions.impl.StringValueSamlObjectImpl;
import stubidp.saml.extensions.extensions.impl.UPRNBuilder;
import stubidp.saml.extensions.extensions.impl.VerifiedBuilder;
import stubidp.saml.extensions.extensions.impl.VerifiedImpl;
import stubidp.saml.extensions.extensions.versioning.Version;
import stubidp.saml.extensions.extensions.versioning.VersionBuilder;
import stubidp.saml.extensions.extensions.versioning.VersionMarshaller;
import stubidp.saml.extensions.extensions.versioning.VersionUnmarshaller;
import stubidp.saml.extensions.extensions.versioning.application.ApplicationVersion;
import stubidp.saml.extensions.extensions.versioning.application.ApplicationVersionBuilder;
import stubidp.saml.extensions.extensions.versioning.application.ApplicationVersionMarshaller;
import stubidp.saml.extensions.extensions.versioning.application.ApplicationVersionUnmarshaller;

public abstract class IdaSamlBootstrap {

    private static boolean hasBeenBootstrapped;
    private static final Object lock = new Object();

    private IdaSamlBootstrap() {}

    public static class BootstrapException extends RuntimeException {
        public BootstrapException(Exception e) {
            super(e);
        }
    }

    public static void bootstrap() {
        synchronized (lock) {
            if (hasBeenBootstrapped) {
                return;
            }

            try {
                doBootstrapping();
            } catch (InitializationException e) {
                throw new BootstrapException(e);
            }

            hasBeenBootstrapped = true;
        }
    }

    private static void doBootstrapping() throws InitializationException {
        InitializationService.initialize();

        //HACK: Why is the string type even registered? It can't ever get the element name right in that case, can it? [Mark/Peter 15/1/2013]
        XMLObjectProviderRegistrySupport.deregisterObjectProvider(XSString.TYPE_NAME);

        XMLObjectProviderRegistrySupport.registerObjectProvider(PersonName.TYPE_NAME, new PersonNameBuilder(), PersonNameImpl.MARSHALLER, PersonNameImpl.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(Date.TYPE_NAME, new DateBuilder(), DateImpl.MARSHALLER, DateImpl.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(Address.TYPE_NAME, new AddressBuilder(), AddressMarshaller.MARSHALLER, AddressUnmarshaller.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(PostCode.DEFAULT_ELEMENT_NAME, new PostCodeBuilder(), StringValueSamlObjectImpl.MARSHALLER, StringValueSamlObjectImpl.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(InternationalPostCode.DEFAULT_ELEMENT_NAME, new InternationalPostCodeBuilder(), StringValueSamlObjectImpl.MARSHALLER, StringValueSamlObjectImpl.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(UPRN.DEFAULT_ELEMENT_NAME, new UPRNBuilder(), StringValueSamlObjectImpl.MARSHALLER, StringValueSamlObjectImpl.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(Line.DEFAULT_ELEMENT_NAME, new LineBuilder(), StringValueSamlObjectImpl.MARSHALLER, StringValueSamlObjectImpl.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(StringBasedMdsAttributeValue.TYPE_NAME, new StringBasedMdsAttributeValueBuilder(), StringBasedMdsAttributeValueImpl.MARSHALLER, StringBasedMdsAttributeValueImpl.UNMARSHALLER);
//        XMLObjectProviderRegistrySupport.registerObjectProvider(StringBasedMdsAttributeValue.DEFAULT_ELEMENT_NAME, new StringBasedMdsAttributeValueBuilder(), StringBasedMdsAttributeValueImpl.MARSHALLER, StringBasedMdsAttributeValueImpl.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(Gender.TYPE_NAME, new GenderBuilder(), GenderImpl.MARSHALLER, GenderImpl.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(IdpFraudEventId.TYPE_NAME, new IdpFraudEventIdBuilder(), IdpFraudEventIdImpl.MARSHALLER, IdpFraudEventIdImpl.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(Gpg45Status.TYPE_NAME, new Gpg45StatusBuilder(), Gpg45StatusImpl.MARSHALLER, IdpFraudEventIdImpl.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(IPAddress.TYPE_NAME, new IPAddressBuilder(), IPAddressImpl.MARSHALLER, IPAddressImpl.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(Verified.TYPE_NAME, new VerifiedBuilder(), VerifiedImpl.MARSHALLER, VerifiedImpl.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(StatusValue.DEFAULT_ELEMENT_NAME, new StatusValueBuilder(), StatusValueImpl.MARSHALLER, StatusValueImpl.UNMARSHALLER);

        // Verify Europe (eIDAS) specific providers
        XMLObjectProviderRegistrySupport.registerObjectProvider(SPType.DEFAULT_ELEMENT_NAME, new SPTypeBuilder(), SPTypeImpl.MARSHALLER, SPTypeImpl.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(RequestedAttributes.DEFAULT_ELEMENT_NAME, new RequestedAttributesBuilder(), RequestedAttributesImpl.MARSHALLER, RequestedAttributesImpl.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(RequestedAttribute.DEFAULT_ELEMENT_NAME, new RequestedAttributeBuilder(), RequestedAttributeImpl.MARSHALLER, RequestedAttributeImpl.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(CurrentGivenName.TYPE_NAME, new CurrentGivenNameBuilder(), CurrentGivenNameMarshaller.MARSHALLER, CurrentGivenNameUnmarshaller.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(CurrentFamilyName.TYPE_NAME, new CurrentFamilyNameBuilder(), CurrentFamilyNameMarshaller.MARSHALLER, CurrentFamilyNameUnmarshaller.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(DateOfBirth.TYPE_NAME, new DateOfBirthBuilder(), DateOfBirthMarshaller.MARSHALLER, DateOfBirthUnmarshaller.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(PersonIdentifier.TYPE_NAME, new PersonIdentifierBuilder(), PersonIdentifierMarshaller.MARSHALLER, PersonIdentifierUnmarshaller.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(CurrentAddress.TYPE_NAME, new CurrentAddressBuilder(), CurrentAddressMarshaller.MARSHALLER, CurrentAddressUnmarshaller.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(EidasGender.TYPE_NAME, new EidasGenderBuilder(), EidasGenderMarshaller.MARSHALLER, EidasGenderUnmarshaller.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(BirthName.TYPE_NAME, new BirthNameBuilder(), BirthNameMarshaller.MARSHALLER, BirthNameUnmarshaller.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(PlaceOfBirth.TYPE_NAME, new PlaceOfBirthBuilder(), PlaceOfBirthMarshaller.MARSHALLER, PlaceOfBirthUnmarshaller.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(CountrySamlResponse.TYPE_NAME, new CountrySamlResponseBuilder(), new CountrySamlResponseMarshaller(), new CountrySamlResponseUnmarshaller());
        XMLObjectProviderRegistrySupport.registerObjectProvider(EncryptedAssertionKeys.TYPE_NAME, new EncryptedAssertionKeysBuilder(), new EncryptedAssertionKeysMarshaller(), new EncryptedAssertionKeysUnmarshaller());

        XMLObjectProviderRegistrySupport.registerObjectProvider(Version.TYPE_NAME, new VersionBuilder(), VersionMarshaller.MARSHALLER, VersionUnmarshaller.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(ApplicationVersion.DEFAULT_ELEMENT_NAME, new ApplicationVersionBuilder(), ApplicationVersionMarshaller.MARSHALLER, ApplicationVersionUnmarshaller.UNMARSHALLER);
    }
}
