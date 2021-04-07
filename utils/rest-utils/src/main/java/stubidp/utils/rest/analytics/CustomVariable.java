package stubidp.utils.rest.analytics;

import static java.lang.String.format;

public record CustomVariable(int index, String name, String value) {

    protected String getJson() {
        return format("{\"%s\":[\"%s\",\"%s\"]}", index(), name(), value());
    }

}
