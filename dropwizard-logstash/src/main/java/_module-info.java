//@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
//module dropwizard.logstash {
//
//    opens stubidp.dropwizard.logstash; // for tests
//
//    requires dropwizard.core; // has package conflict with dropwizard.validation in io.dropwizard.validation
//    requires dropwizard.logging;
//    requires dropwizard.validation;
//    requires org.joda.time;
//    requires logback.access;
//    requires jackson.annotations;
//    requires com.fasterxml.jackson.core;
//    requires com.fasterxml.jackson.databind;
//    requires logstash.logback.encoder;
//    requires logback.core;
//    requires logback.classic;
//    requires java.validation;
//}