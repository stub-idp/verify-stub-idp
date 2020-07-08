package stubidp.saml.hub.core.test.builders;


import stubidp.saml.hub.metadata.domain.OrganisationDto;

public class OrganisationDtoBuilder {

    private String organisationDisplayName = "Display Name";
    private String organisationName = "MegaCorp";

    public static OrganisationDtoBuilder anOrganisationDto() {
        return new OrganisationDtoBuilder();
    }

    public OrganisationDto build() {
        return new OrganisationDto(
                organisationDisplayName,
                organisationName,
                "https://hub.ida.gov.uk");
    }

    public OrganisationDtoBuilder withDisplayName(String organisationDisplayName) {
        this.organisationDisplayName = organisationDisplayName;
        return this;
    }

    public OrganisationDtoBuilder withName(String organisationName) {
        this.organisationName = organisationName;
        return this;
    }
}
