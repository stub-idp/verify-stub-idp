package stubidp.saml.hub.metadata.domain;

import stubidp.utils.security.security.Certificate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class MetadataDto {
    protected List<Certificate> encryptionCertificates;
    private Instant validUntil;
    private String entityId;
    private OrganisationDto organisation;
    private Collection<ContactPersonDto> contactPersons = new ArrayList<>();
    private List<Certificate> hubSigningCertificates;

    protected MetadataDto() {}

    public MetadataDto(
            String entityId,
            Instant validUntil,
            OrganisationDto organisation,
            Collection<ContactPersonDto> contactPersons,
            List<Certificate> hubSigningCertificates, List<Certificate> encryptionCertificates) {
        this.entityId = entityId;
        this.validUntil = validUntil;
        this.organisation = organisation;
        this.contactPersons = contactPersons;
        this.hubSigningCertificates = hubSigningCertificates;
        this.encryptionCertificates = encryptionCertificates;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public String getEntityId() {
        return entityId;
    }

    public OrganisationDto getOrganisation() {
        return organisation;
    }

    public Collection<ContactPersonDto> getContactPersons() {
        return contactPersons;
    }

    public List<Certificate> getSigningCertificates() {
        return hubSigningCertificates;
    }

    public Collection<Certificate> getCertificates() {
        return Stream.<Certificate>concat(hubSigningCertificates.stream(), encryptionCertificates.stream())
                .collect(Collectors.toUnmodifiableList());
    }

    public List<Certificate> getEncryptionCertificates() {
        return encryptionCertificates;
    }
}
