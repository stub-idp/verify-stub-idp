@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module saml.metadata.bindings {
    exports stubidp.saml.metadata;
    exports stubidp.saml.metadata.factories;

    requires org.opensaml.core;
    requires org.opensaml.saml;
    requires org.opensaml.xmlsec;
    requires slf4j.api;
    requires security.utils;
    requires jsr305; // https://github.com/google/guava/issues/2960
    requires java.xml;
    requires dropwizard.core;
    requires net.shibboleth.utilities.java.support;
    requires org.opensaml.saml.impl;
    requires org.opensaml.xmlsec.impl;
    requires javax.inject;
    requires javax.ws.rs.api;
    requires dropwizard.client;
    requires httpclient;
    requires com.fasterxml.jackson.annotation;
    requires nimbus.jose.jwt;
    requires commons.collections;
    requires xmlsec;
    requires org.joda.time;
    requires trust.anchor;
    requires metrics.healthchecks;
    requires dropwizard.servlets;
    requires java.xml.crypto; // only for MetadataSignatureTrustEngineFactoryTest
}