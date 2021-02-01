package uk.gov.ida.rp.testrp.contract;

import java.util.Map;
import java.util.Objects;

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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cycle3DatasetDto that = (Cycle3DatasetDto) o;
        return Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributes);
    }
}
