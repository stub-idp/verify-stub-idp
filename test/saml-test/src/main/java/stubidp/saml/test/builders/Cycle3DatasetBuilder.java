package stubidp.saml.test.builders;

import stubidp.saml.domain.assertions.Cycle3Dataset;

import java.util.HashMap;
import java.util.Map;

public class Cycle3DatasetBuilder {
    private final Map<String,String> attributes = new HashMap<>();

    private Cycle3DatasetBuilder() {}

    public static Cycle3DatasetBuilder aCycle3Dataset() {
        return new Cycle3DatasetBuilder();
    }

    public Cycle3Dataset build() {
        return Cycle3Dataset.createFromData(attributes);
    }

    public Cycle3DatasetBuilder addCycle3Data(String name, String value) {
        attributes.put(name, value);
        return this;
    }
}
