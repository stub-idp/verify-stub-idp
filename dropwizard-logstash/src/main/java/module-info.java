@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module stubidp.logging.dropwizard.logstash {
    opens stubidp.dropwizard.logstash;

    exports stubidp.dropwizard.logstash;
    exports stubidp.dropwizard.logstash.typed to com.fasterxml.jackson.databind;

    requires transitive com.fasterxml.jackson.core;
    requires transitive dropwizard.core;
    requires transitive dropwizard.logging;
    requires transitive java.validation;
    requires transitive logback.access;
    requires transitive logback.classic;
    requires transitive logback.core;
    requires transitive logstash.logback.encoder;

    requires com.fasterxml.jackson.databind;
    requires dropwizard.validation;
}