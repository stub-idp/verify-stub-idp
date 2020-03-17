package stubidp.saml.security.saml.builders;

import org.opensaml.saml.saml2.metadata.Company;

import java.util.Objects;

public class CompanyBuilder {
    private String name = "Slate Rock and Gravel Company";

    public static CompanyBuilder aCompany(){
        return new CompanyBuilder();
    }

    public Company build() {
        Company company = new org.opensaml.saml.saml2.metadata.impl.CompanyBuilder().buildObject();
        company.setValue(name);
        return company;
    }

    public CompanyBuilder withName(String companyName) {
        Objects.requireNonNull(companyName);
        this.name = companyName;
        return this;
    }
}
