@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module stubidp.saml.security {
    exports stubidp.saml.security.signature;
    exports stubidp.saml.security.validators.encryptedelementtype;
    exports stubidp.saml.security.validators.issuer;
    exports stubidp.saml.security.validators.signature;
    exports stubidp.saml.security.validators;
    exports stubidp.saml.security;

    opens stubidp.saml.security;
    opens stubidp.saml.security.validators;
    opens stubidp.saml.security.validators.encryptedelementtype;
    opens stubidp.saml.security.validators.issuer;
    opens stubidp.saml.security.validators.signature;

    requires org.slf4j;
    requires org.apache.santuario.xmlsec;
    requires org.checkerframework.checker.qual;
    requires org.opensaml.core;

    requires transitive org.opensaml.xmlsec.impl;
    requires transitive org.opensaml.security;
    requires transitive java.validation;
    requires transitive java.xml;
    requires transitive net.shibboleth.utilities.java.support;
    requires transitive org.apache.commons.codec;
    requires transitive org.opensaml.saml.impl;
    requires transitive org.opensaml.saml;
    requires transitive org.opensaml.security.impl;
    requires transitive org.opensaml.xmlsec;
    requires transitive stubidp.saml.extensions;
    requires transitive stubidp.security.utils;
}