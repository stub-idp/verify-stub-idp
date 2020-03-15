package stubidp.saml.hub.metadata.domain;

import stubidp.utils.security.security.Certificate;

import java.time.Instant;
import java.util.Collection;

public abstract class FetchedMetadata {
    private Instant validUntil;
    private String entityId;
    private OrganisationDto organisation;
    private Collection<ContactPersonDto> contactPersons;
    private Certificate signingCertificate;

    public FetchedMetadata(
        String entityId,
        Instant validUntil,
        OrganisationDto organisation,
        Collection<ContactPersonDto> contactPersons,
        Certificate signingCertificate) {
        this.entityId = entityId;
        this.validUntil = validUntil;
        this.organisation = organisation;
        this.contactPersons = contactPersons;
        this.signingCertificate = signingCertificate;
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

    public Certificate getSigningCertificate() {
        return signingCertificate;
    }
}
