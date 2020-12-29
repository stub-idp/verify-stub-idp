package stubidp.saml.hub.core.test.builders.metadata;

import stubidp.saml.hub.core.test.builders.ContactPersonDtoBuilder;
import stubidp.saml.hub.core.test.builders.OrganisationDtoBuilder;
import stubidp.saml.hub.metadata.domain.ContactPersonDto;
import stubidp.saml.hub.metadata.domain.HubIdentityProviderMetadataDto;
import stubidp.saml.hub.metadata.domain.OrganisationDto;
import stubidp.saml.hub.metadata.domain.SamlEndpointDto;
import stubidp.saml.test.builders.CertificateBuilder;
import stubidp.test.devpki.TestEntityIds;
import stubidp.utils.security.security.Certificate;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class IdentityProviderMetadataDtoBuilder {

    private final List<SamlEndpointDto> singleSignOnServiceEndpoints = new ArrayList<>();
    private String entityId = TestEntityIds.HUB_ENTITY_ID;
    private final OrganisationDto organisation = OrganisationDtoBuilder.anOrganisationDto().build();
    private final List<ContactPersonDto> contactPersons = new ArrayList<>();
    private List<Certificate> signingCertificates = null;
    private Instant validUntil = Instant.now().atZone(ZoneId.of("UTC")).plusDays(365).toInstant();
    private final List<Certificate> idpSigningCertificates = new ArrayList<>();
    private Certificate encryptionCertificate = null;

    private boolean useDefaultSingleSignOnServiceEndpoints = true;
    private boolean useDefaultContactPerson = true;

    private IdentityProviderMetadataDtoBuilder() {}

    public static IdentityProviderMetadataDtoBuilder anIdentityProviderMetadataDto() {
        return new IdentityProviderMetadataDtoBuilder();
    }

    public HubIdentityProviderMetadataDto build() {
        populateListsWithDefaults();

        return new HubIdentityProviderMetadataDto(
                singleSignOnServiceEndpoints,
                entityId,
                organisation,
                contactPersons,
                idpSigningCertificates,
                validUntil,
                signingCertificates,
                encryptionCertificate);
    }

    private void populateListsWithDefaults() {
        if (useDefaultSingleSignOnServiceEndpoints) {
            this.singleSignOnServiceEndpoints.add(SamlEndpointDto.createPostBinding(URI.create("https://hub.ida.gov.uk/SAML2/SSO/POST")));
        }
        if (useDefaultContactPerson) {
            this.contactPersons.add(ContactPersonDtoBuilder.aContactPersonDto().build());
        }
        if (signingCertificates == null && entityId != null) {
            this.withSigningCertificate(CertificateBuilder.aCertificate().withKeyUse(Certificate.KeyUse.Signing).withIssuerId(entityId).build());
        }
        if (idpSigningCertificates.isEmpty()) {
            this.idpSigningCertificates.add(CertificateBuilder.aCertificate().build());
        }
        if (encryptionCertificate == null && entityId != null) {
            this.encryptionCertificate = CertificateBuilder.aCertificate().withKeyUse(Certificate.KeyUse.Encryption).withIssuerId(entityId).build();
        }
    }

    public IdentityProviderMetadataDtoBuilder addSingleSignOnServiceEndpoint(SamlEndpointDto samlEndpoint) {
        this.singleSignOnServiceEndpoints.add(samlEndpoint);
        this.useDefaultSingleSignOnServiceEndpoints = false;
        return this;
    }

    public IdentityProviderMetadataDtoBuilder withEntityId(String entityId) {
        this.entityId = entityId;
        return this;
    }

    public IdentityProviderMetadataDtoBuilder addContactPerson(ContactPersonDto contactPerson) {
        this.contactPersons.add(contactPerson);
        this.useDefaultContactPerson = false;
        return this;
    }

    public IdentityProviderMetadataDtoBuilder withSigningCertificate(Certificate certificate) {
        this.signingCertificates = List.of(certificate);
        return this;
    }

    public IdentityProviderMetadataDtoBuilder withSigningCertificates(List<Certificate> signingCertificates) {
        this.signingCertificates = signingCertificates;
        return this;
    }

    public IdentityProviderMetadataDtoBuilder withValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
        return this;
    }

    public IdentityProviderMetadataDtoBuilder addIdpSigningCertificate(Certificate certificate) {
        this.idpSigningCertificates.add(certificate);
        return this;
    }

    public IdentityProviderMetadataDtoBuilder withHubEncryptionCertificate(Certificate certificate) {
        this.encryptionCertificate = certificate;
        return this;
    }
}
