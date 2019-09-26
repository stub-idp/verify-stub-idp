package stubidp.stubidp.services;

import io.prometheus.client.Counter;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.EidasAuthnContext;
import stubidp.saml.extensions.extensions.eidas.CurrentAddress;
import stubidp.saml.extensions.extensions.eidas.CurrentFamilyName;
import stubidp.saml.extensions.extensions.eidas.CurrentGivenName;
import stubidp.saml.extensions.extensions.eidas.DateOfBirth;
import stubidp.saml.extensions.extensions.eidas.EidasGender;
import stubidp.saml.extensions.extensions.eidas.PersonIdentifier;
import stubidp.stubidp.StubIdpBinder;
import stubidp.stubidp.builders.EidasResponseBuilder;
import stubidp.stubidp.domain.EidasAddress;
import stubidp.stubidp.domain.EidasUser;
import stubidp.stubidp.domain.RequestedAttribute;
import stubidp.stubidp.domain.SamlResponseFromValue;
import stubidp.stubidp.repositories.EidasSession;
import stubidp.stubidp.repositories.MetadataRepository;
import stubidp.stubidp.saml.transformers.EidasResponseTransformerProvider;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import javax.xml.namespace.QName;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static stubidp.stubidp.StubIdpBinder.HUB_CONNECTOR_ENTITY_ID;
import static stubidp.stubidp.StubIdpBinder.STUB_COUNTRY_METADATA_URL;

public class EidasAuthnResponseService {

    private static final Counter sentEidasAuthnSuccessResponses = Counter.build()
            .name("stubidp_eidas_sentAuthnResponses_success_total")
            .help("Number of sent verify authn success responses.")
            .register();
    private static final Counter sentEidasAuthnFailureResponses = Counter.build()
            .name("stubidp_eidas_sentAuthnResponses_failure_total")
            .help("Number of sent eidas authn failure responses.")
            .labelNames("failure_type")
            .register();

    private enum FailureType {
        authn_failed
    }

    private final String hubConnectorEntityId;
    private final EidasResponseTransformerProvider eidasResponseTransformerProvider;
    private final MetadataRepository metadataProvider;
    private final String stubCountryMetadataUrl;

    @Inject
    public EidasAuthnResponseService(@Named(HUB_CONNECTOR_ENTITY_ID) String hubConnectorEntityId,
                                     EidasResponseTransformerProvider eidasResponseTransformerProvider,
                                     @Named(StubIdpBinder.HUB_CONNECTOR_METADATA_REPOSITORY) Optional<MetadataRepository> metadataProvider,
                                     @Named(STUB_COUNTRY_METADATA_URL) String stubCountryMetadataUrl) {
        this.hubConnectorEntityId = hubConnectorEntityId;
        this.eidasResponseTransformerProvider = eidasResponseTransformerProvider;
        this.metadataProvider = metadataProvider.get();
        this.stubCountryMetadataUrl = stubCountryMetadataUrl;
    }

    public SamlResponseFromValue<Response> getSuccessResponse(EidasSession session, String schemeId) {
        String issuerId = UriBuilder.fromUri(stubCountryMetadataUrl).build(schemeId).toString();
        URI hubUrl = metadataProvider.getAssertionConsumerServiceLocation();
        String requestId = session.getEidasAuthnRequest().getRequestId();
        List<Attribute> eidasAttributes = getEidasAttributes(session);
        DateTime issueInstant = DateTime.now();

        Response response = EidasResponseBuilder.createEidasResponse(
            issuerId,
            StatusCode.SUCCESS,
            UUID.randomUUID().toString(),
            EidasAuthnContext.EIDAS_LOA_SUBSTANTIAL,
            eidasAttributes,
            requestId,
            issueInstant,
            issueInstant,
            issueInstant,
            hubUrl.toString(),
            hubConnectorEntityId
        );

        sentEidasAuthnSuccessResponses.inc();

        return new SamlResponseFromValue<>(response, eidasResponseTransformerProvider.getTransformer(), session.getRelayState(), hubUrl);
    }

    public SamlResponseFromValue<Response> generateAuthnFailed(EidasSession session, String schemeId) {
        String issuerId = UriBuilder.fromUri(stubCountryMetadataUrl).build(schemeId).toString();
        String requestId = session.getEidasAuthnRequest().getRequestId();
        URI hubUrl = metadataProvider.getAssertionConsumerServiceLocation();

        Response eidasInvalidResponse = new EidasResponseBuilder()
                .withRandomId()
                .withStatus(StatusCode.RESPONDER, StatusCode.AUTHN_FAILED)
                .withIssuer(issuerId)
                .withInResponseTo(requestId)
                .withIssueInstant(DateTime.now())
                .withDestination(hubUrl.toString())
                .build();

        sentEidasAuthnFailureResponses.labels(FailureType.authn_failed.name()).inc();

        return new SamlResponseFromValue<>(eidasInvalidResponse, eidasResponseTransformerProvider.getTransformer(), session.getRelayState(), hubUrl);
    }

    private List<Attribute> getEidasAttributes(EidasSession session) {
        List<RequestedAttribute> requestedAttributes = session.getEidasAuthnRequest().getAttributes();
        EidasUser user = session.getEidasUser().get();

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(buildCurrentGivenNameAttribute(user.getFirstName(), user.getFirstNameNonLatin()));
        attributes.add(buildCurrentFamilyNameAttribute(user.getFamilyName(), user.getFamilyNameNonLatin()));
        attributes.add(buildDateOfBirthAttribute(user.getDateOfBirth()));
        attributes.add(buildPersonIdentifierAttribute(user.getPersistentId()));

        if (isAttributeRequested(requestedAttributes, IdaConstants.Eidas_Attributes.Gender.NAME) && user.getGender().isPresent()) {
            attributes.add(buildGenderAttribute(user.getGender().get().getValue()));
        }
        if (isAttributeRequested(requestedAttributes, IdaConstants.Eidas_Attributes.CurrentAddress.NAME) && user.getAddress().isPresent()) {
            attributes.add(buildAddressAttribute(user.getAddress().get()));
        }
        return attributes;
    }

    @SuppressWarnings({"unchecked", "cast"})
    private Attribute buildCurrentGivenNameAttribute(String name, Optional<String> givenNameNonLatinOptional) {
        XMLObjectBuilder<? extends CurrentGivenName> eidasTypeBuilder =
                (XMLObjectBuilder<? extends CurrentGivenName>) XMLObjectSupport.getBuilder(CurrentGivenName.TYPE_NAME);
        CurrentGivenName givenName = eidasTypeBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, CurrentGivenName.TYPE_NAME);
        givenName.setFirstName(name);

        CurrentGivenName[] currentGivenNames = givenNameNonLatinOptional.map(givenNameNonLatinValue -> {
            CurrentGivenName givenNameNonLatin = eidasTypeBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, CurrentGivenName.TYPE_NAME);
            givenNameNonLatin.setFirstName(givenNameNonLatinValue);
            givenNameNonLatin.setIsLatinScript(false);
            return new CurrentGivenName[]{givenName, givenNameNonLatin};
        }).orElse(new CurrentGivenName[]{givenName});

        return buildAttribute(IdaConstants.Eidas_Attributes.FirstName.NAME, IdaConstants.Eidas_Attributes.FirstName.FRIENDLY_NAME, currentGivenNames);
    }

    @SuppressWarnings({"unchecked", "cast"})
    private Attribute buildCurrentFamilyNameAttribute(String name, Optional<String> familyNameNonLatinOptional) {
        XMLObjectBuilder<? extends CurrentFamilyName> eidasTypeBuilder =
                (XMLObjectBuilder<? extends CurrentFamilyName>) XMLObjectSupport.getBuilder(CurrentFamilyName.TYPE_NAME);
        CurrentFamilyName familyName = eidasTypeBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, CurrentFamilyName.TYPE_NAME);
        familyName.setFamilyName(name);

        CurrentFamilyName[] currentFamilyNames = familyNameNonLatinOptional.map(familyNameNonLatinValue -> {
            CurrentFamilyName familyNameNonLatin = eidasTypeBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, CurrentFamilyName.TYPE_NAME);
            familyNameNonLatin.setFamilyName(familyNameNonLatinValue);
            familyNameNonLatin.setIsLatinScript(false);
            return new CurrentFamilyName[]{familyName, familyNameNonLatin};
        }).orElse(new CurrentFamilyName[]{familyName});


        return buildAttribute(IdaConstants.Eidas_Attributes.FamilyName.NAME, IdaConstants.Eidas_Attributes.FamilyName.FRIENDLY_NAME, currentFamilyNames);
    }

    @SuppressWarnings({"unchecked", "cast"})
    private Attribute buildDateOfBirthAttribute(LocalDate dateOfBirth) {
        XMLObjectBuilder<? extends DateOfBirth> eidasTypeBuilder = (XMLObjectBuilder<? extends DateOfBirth>) XMLObjectSupport.getBuilder(DateOfBirth.TYPE_NAME);
        DateOfBirth dateOfBirthAttributeValue = eidasTypeBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, DateOfBirth.TYPE_NAME);
        dateOfBirthAttributeValue.setDateOfBirth(dateOfBirth);

        return buildAttribute(IdaConstants.Eidas_Attributes.DateOfBirth.NAME, IdaConstants.Eidas_Attributes.DateOfBirth.FRIENDLY_NAME, dateOfBirthAttributeValue);
    }

    @SuppressWarnings({"unchecked", "cast"})
    private Attribute buildPersonIdentifierAttribute(String pid) {
        XMLObjectBuilder<? extends PersonIdentifier> eidasTypeBuilder = (XMLObjectBuilder<? extends PersonIdentifier>) XMLObjectSupport.getBuilder(PersonIdentifier.TYPE_NAME);
        PersonIdentifier pidAttributeValue = eidasTypeBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, PersonIdentifier.TYPE_NAME);
        pidAttributeValue.setPersonIdentifier(pid);

        return buildAttribute(IdaConstants.Eidas_Attributes.PersonIdentifier.NAME, IdaConstants.Eidas_Attributes.PersonIdentifier.FRIENDLY_NAME, pidAttributeValue);
    }

    @SuppressWarnings({"unchecked", "cast"})
    private Attribute buildGenderAttribute(String value) {
        XMLObjectBuilder<? extends EidasGender> eidasTypeBuilder = (XMLObjectBuilder<? extends EidasGender>) XMLObjectSupport.getBuilder(EidasGender.TYPE_NAME);
        EidasGender eidasGender = eidasTypeBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, EidasGender.TYPE_NAME);
        eidasGender.setValue(value);

        return buildAttribute(IdaConstants.Eidas_Attributes.Gender.NAME, IdaConstants.Eidas_Attributes.Gender.FRIENDLY_NAME, eidasGender);
    }

    @SuppressWarnings({"unchecked", "cast"})
    private Attribute buildAddressAttribute(EidasAddress address) {
        XMLObjectBuilder<? extends CurrentAddress> eidasTypeBuilder = (XMLObjectBuilder<? extends CurrentAddress>) XMLObjectSupport.getBuilder(CurrentAddress.TYPE_NAME);
        CurrentAddress currentAddress = eidasTypeBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, CurrentAddress.TYPE_NAME);

        currentAddress.setCurrentAddress(address.toBase64EncodedSaml());

        return buildAttribute(IdaConstants.Eidas_Attributes.CurrentAddress.NAME, IdaConstants.Eidas_Attributes.CurrentAddress.FRIENDLY_NAME, (XMLObject) currentAddress);
    }

    private Attribute buildAttribute(String attributeName, String attributeFriendlyName, XMLObject... attributeValue) {
        Attribute attribute = build(Attribute.DEFAULT_ELEMENT_NAME);

        attribute.setName(attributeName);
        attribute.setFriendlyName(attributeFriendlyName);
        attribute.setNameFormat(Attribute.URI_REFERENCE);
        attribute.getAttributeValues().addAll(Arrays.asList(attributeValue));

        return attribute;
    }

    @SuppressWarnings({"unchecked", "cast"})
    private static <T extends XMLObject> T build(QName elementName) {
        return (T) XMLObjectSupport.buildXMLObject(elementName);
    }

    private boolean isAttributeRequested(List<RequestedAttribute> requestedAttributes, String attributeName) {
        return requestedAttributes.stream().anyMatch(attribute -> attribute.getName().equals(attributeName));
    }
}
