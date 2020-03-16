package stubidp.saml.security.saml.builders;

import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.metadata.AttributeAuthorityDescriptor;
import org.opensaml.saml.saml2.metadata.ContactPerson;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.Organization;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import stubidp.test.devpki.TestEntityIds;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static stubidp.saml.security.saml.builders.KeyDescriptorBuilder.aKeyDescriptor;

public class EntityDescriptorBuilder {

    private String entityId = TestEntityIds.HUB_ENTITY_ID;
    private boolean addDefaultSpServiceDescriptor = true;
    private boolean addDefaultContactPerson = true;
    private boolean shouldBeSigned = true;
    private Duration cacheDuration = Duration.ofMillis(100_000L);
    private Organization organization = OrganizationBuilder.anOrganization().build();
    private List<SPSSODescriptor> spServiceDescriptors = new ArrayList<>();
    private Instant validUntil = Instant.now().atZone(ZoneId.of("UTC")).plusDays(1).toInstant();
    private List<ContactPerson> contactPersons = new ArrayList<>();
    private String id = UUID.randomUUID().toString();
    private final SPSSODescriptor defaultSpServiceDescriptor = SPSSODescriptorBuilder.anSpServiceDescriptor().addKeyDescriptor(aKeyDescriptor().withX509ForSigning("").build()).addKeyDescriptor(aKeyDescriptor().withX509ForEncryption("").build()).build();
    private final ContactPerson defaultContactPerson = ContactPersonBuilder.aContactPerson().build();
    private Optional<IDPSSODescriptor> idpSsoDescriptor = ofNullable(IdpSsoDescriptorBuilder.anIdpSsoDescriptor().build());
    private Optional<Signature> signature = Optional.empty();
    private Optional<AttributeAuthorityDescriptor> attributeAuthorityDescriptor = Optional.empty();

    public static EntityDescriptorBuilder anEntityDescriptor() {
        return new EntityDescriptorBuilder();
    }

    public EntityDescriptor build() throws MarshallingException, SignatureException {
        EntityDescriptor entityDescriptor = new org.opensaml.saml.saml2.metadata.impl.EntityDescriptorBuilder().buildObject();

        entityDescriptor.setEntityID(entityId);

        entityDescriptor.setOrganization(organization);

        idpSsoDescriptor.ifPresent(idpssoDescriptor -> entityDescriptor.getRoleDescriptors().add(idpssoDescriptor));
        attributeAuthorityDescriptor.ifPresent(authorityDescriptor -> entityDescriptor.getRoleDescriptors().add(authorityDescriptor));

        if (addDefaultSpServiceDescriptor) {
            entityDescriptor.getRoleDescriptors().add(defaultSpServiceDescriptor);
        }

        entityDescriptor.getRoleDescriptors().addAll(spServiceDescriptors);

        entityDescriptor.setValidUntil(validUntil);

        entityDescriptor.setCacheDuration(cacheDuration);

        if (addDefaultContactPerson) {
            contactPersons.add(defaultContactPerson);
        }
        for (ContactPerson contactPerson : contactPersons) {
            entityDescriptor.getContactPersons().add(contactPerson);
        }

        entityDescriptor.setID(id);

        if (signature.isPresent() && Objects.nonNull(entityId) && !entityId.isBlank()) {
            entityDescriptor.setSignature(signature.get());

            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(entityDescriptor).marshall(entityDescriptor);
            if (shouldBeSigned) {
                Signer.signObject(entityDescriptor.getSignature());
            }
        }

        return entityDescriptor;
    }

    public EntityDescriptorBuilder withEntityId(String entityId) {
        this.entityId = entityId;
        return this;
    }

    public EntityDescriptorBuilder withOrganization(Organization organization) {
        this.organization = organization;
        return this;
    }

    public EntityDescriptorBuilder addContactPerson(ContactPerson contactPerson) {
        this.contactPersons.add(contactPerson);
        this.addDefaultContactPerson = false;
        return this;
    }

    public EntityDescriptorBuilder withIdpSsoDescriptor(IDPSSODescriptor descriptor) {
        this.idpSsoDescriptor = ofNullable(descriptor);
        return this;
    }

    public EntityDescriptorBuilder addSpServiceDescriptor(SPSSODescriptor descriptor) {
        this.spServiceDescriptors.add(descriptor);
        this.addDefaultSpServiceDescriptor = false;
        return this;
    }

    public EntityDescriptorBuilder setAddDefaultSpServiceDescriptor(boolean enabled) {
        this.addDefaultSpServiceDescriptor = enabled;
        return this;
    }

    public EntityDescriptorBuilder withValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
        return this;
    }

    public EntityDescriptorBuilder withCacheDuration(Duration duration) {
        this.cacheDuration = duration;
        return this;
    }

    public EntityDescriptorBuilder withSignature(Signature signature) {
        this.signature = ofNullable(signature);
        return this;
    }

    public EntityDescriptorBuilder withoutSigning() {
        this.shouldBeSigned = false;
        return this;
    }

    public EntityDescriptorBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public EntityDescriptorBuilder withAttributeAuthorityDescriptor(AttributeAuthorityDescriptor descriptor) {
        attributeAuthorityDescriptor = ofNullable(descriptor);
        return this;
    }
}
