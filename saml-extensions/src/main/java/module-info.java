@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module saml.extensions {
    exports stubidp.saml.extensions;
    exports stubidp.saml.extensions.domain;
    exports stubidp.saml.extensions.extensions;
    exports stubidp.saml.extensions.extensions.eidas;
    exports stubidp.saml.extensions.extensions.eidas.impl;
    exports stubidp.saml.extensions.validation;
    exports stubidp.saml.extensions.validation.errors;
    exports stubidp.saml.extensions.extensions.versioning;
    exports stubidp.saml.extensions.extensions.versioning.application;
    exports stubidp.saml.extensions.extensions.impl;

    requires net.shibboleth.utilities.java.support;
    requires org.opensaml.saml.impl;
    requires xmlsec;
    requires bcprov.jdk15on;
    requires org.opensaml.xmlsec;
    requires org.opensaml.xmlsec.impl;

    requires transitive org.slf4j;
    requires transitive jsr305; // https://github.com/google/guava/issues/2960
    requires transitive org.opensaml.core;
    requires transitive org.joda.time;
    requires transitive java.xml;
    requires transitive org.opensaml.saml;
}