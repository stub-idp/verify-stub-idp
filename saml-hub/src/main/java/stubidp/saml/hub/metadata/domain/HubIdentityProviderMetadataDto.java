package stubidp.saml.hub.metadata.domain;

import stubidp.utils.security.security.Certificate;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class HubIdentityProviderMetadataDto extends MetadataDto {

    private Collection<Certificate> idpSigningCertificates;
    private Collection<SamlEndpointDto> singleSignOnEndpoints;

    public HubIdentityProviderMetadataDto(
            Collection<SamlEndpointDto> singleSignOnEndpoints,
            String entityId,
            OrganisationDto organisation,
            Collection<ContactPersonDto> contactPersons,
            Collection<Certificate> idpSigningCertificates,
            Instant validUntil,
            List<Certificate> hubSigningCertificates,
            Certificate hubEncryptionCertificate) {

        super(entityId, validUntil, organisation, contactPersons, hubSigningCertificates, Collections.singletonList(hubEncryptionCertificate));

        this.singleSignOnEndpoints = singleSignOnEndpoints;

        this.idpSigningCertificates = idpSigningCertificates;
    }

    public Collection<Certificate> getIdpSigningCertificates() {
        return idpSigningCertificates;
    }

    public Collection<SamlEndpointDto> getSingleSignOnEndpoints() {
        return singleSignOnEndpoints;
    }
}
