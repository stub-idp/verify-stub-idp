@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module stubidp.saml.metadata.bindings {
    opens stubidp.saml.metadata;
    opens stubidp.saml.metadata.factories;

    exports stubidp.saml.metadata.bundle;
    exports stubidp.saml.metadata.factories;
    exports stubidp.saml.metadata;

    requires transitive com.codahale.metrics.health;
    requires transitive dropwizard.client;
    requires transitive dropwizard.core;
    requires transitive dropwizard.servlets;
    requires transitive jakarta.inject;
    requires transitive java.validation;
    requires transitive java.ws.rs;
    requires transitive net.shibboleth.utilities.java.support;
    requires transitive nimbus.jose.jwt;
    requires transitive org.checkerframework.checker.qual;
    requires transitive org.opensaml.core;
    requires transitive org.opensaml.saml.impl;
    requires transitive org.opensaml.saml;
    requires transitive org.opensaml.security.impl;
    requires transitive org.opensaml.security;
    requires transitive org.opensaml.xmlsec;
    requires transitive org.opensaml.xmlsec.impl;
    requires transitive stubidp.security.utils;

    requires dropwizard.validation;
    requires java.xml.crypto;
    requires java.xml;
    requires org.apache.httpcomponents.httpclient;
    requires org.slf4j;
    requires stubidp.saml.extensions;
    requires stubidp.saml.serializers;
    requires stubidp.trust.anchor;
    requires org.apache.santuario.xmlsec;
    requires org.bouncycastle.provider;
}
