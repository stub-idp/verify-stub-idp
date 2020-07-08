@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module stubidp.common.utils {
    opens stubidp.utils.common.logging;

    exports stubidp.utils.common.string;
    exports stubidp.utils.common.xml;

    requires transitive java.xml;

    requires com.fasterxml.jackson.databind;
    requires commons.validator;
    requires dropwizard.configuration;
    requires java.validation;
    requires org.slf4j;
}
