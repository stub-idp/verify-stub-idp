package uk.gov.ida.rp.testrp.contract;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Map;

// CAUTION!!! CHANGES TO THIS CLASS WILL IMPACT MSA USERS
public class Cycle3DatasetDto {
    private Map<String, String> attributes;

    @SuppressWarnings("unused")
    private Cycle3DatasetDto() {
        // Needed by JAXB
    }

    protected Cycle3DatasetDto(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public static Cycle3DatasetDto createFromData(Map<String, String> map) {
        return new Cycle3DatasetDto(map);
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
