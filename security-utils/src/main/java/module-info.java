@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module security.utils {
    exports stubidp.utils.security.configuration;
    exports stubidp.utils.security.security;
    exports stubidp.utils.security.security.verification;
    exports stubidp.utils.security.security.verification.exceptions;

    requires com.fasterxml.jackson.annotation;
    requires org.slf4j;
    requires java.xml.bind; // only for base64 code in CryptoHelper...
    requires org.apache.commons.codec;

    requires transitive java.validation;
    requires transitive javax.inject;
    requires transitive com.google.common;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
}