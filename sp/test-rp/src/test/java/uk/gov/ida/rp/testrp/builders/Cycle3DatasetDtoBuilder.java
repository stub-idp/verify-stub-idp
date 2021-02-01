package uk.gov.ida.rp.testrp.builders;


import uk.gov.ida.rp.testrp.contract.Cycle3DatasetDto;

import java.util.HashMap;
import java.util.Map;

public class Cycle3DatasetDtoBuilder {

    private Map<String, String> attributes = new HashMap<>();

    public static Cycle3DatasetDtoBuilder aCycle3DatasetDto() {
        return new Cycle3DatasetDtoBuilder();
    }

    public Cycle3DatasetDto build() {
        if (!attributes.isEmpty()) {
            attributes.put("test-name", "test-value");
        }

        return Cycle3DatasetDto.createFromData(attributes);
    }

    public Cycle3DatasetDtoBuilder addCycle3Data(String name, String value) {
        attributes.put(name, value);
        return this;
    }
}
