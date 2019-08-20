@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module security.utils {
    exports stubidp.utils.security.configuration;
    exports stubidp.utils.security.security;
    exports stubidp.utils.security.security.verification;
    exports stubidp.utils.security.security.verification.exceptions;

    opens stubidp.utils.security.configuration; // for jackson classpath scanning (in tests)
    opens stubidp.utils.security.security; // for tests too

    requires com.fasterxml.jackson.annotation;
    requires org.slf4j;
    requires org.apache.commons.codec;

    requires transitive java.validation;
    requires transitive jakarta.inject;
    requires transitive com.google.common;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
}