@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module stubidp.common.utils {
    opens stubidp.utils.common;
    opens stubidp.utils.common.email;
    opens stubidp.utils.common.featuretoggles;
    opens stubidp.utils.common.logging;
    opens stubidp.utils.common.xml;
    opens stubidp.utils.common.manifest;

    exports stubidp.utils.common.string;
    exports stubidp.utils.common.xml;

    requires transitive java.xml;

    requires com.fasterxml.jackson.databind;
    requires commons.validator;
    requires dropwizard.configuration;
    requires java.validation;
    requires org.slf4j;
}
