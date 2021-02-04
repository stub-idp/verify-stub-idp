@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module stubidp.security.utils {
    exports stubidp.utils.security.configuration;
    exports stubidp.utils.security.security;
    exports stubidp.utils.security.security.verification;
    exports stubidp.utils.security.security.verification.exceptions;

    opens stubidp.utils.security.configuration; // for jackson classpath scanning (in tests)
    opens stubidp.utils.security.security; // for tests too
    opens stubidp.utils.security.security.verification;

    requires com.fasterxml.jackson.annotation;
    requires org.slf4j;

    requires transitive java.validation;
    requires transitive jakarta.inject;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires transitive simpleclient;
}
