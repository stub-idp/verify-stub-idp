//@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
//module stubidp.saml.metadata.bindings {
//    exports stubidp.saml.metadata;
//    exports stubidp.saml.metadata.factories;
//
//    requires stubidp.security.utils;
//    requires stubidp.trust.anchor;
//    requires org.opensaml.core;
//    requires org.opensaml.saml;
//    requires org.opensaml.xmlsec;
//    requires org.slf4j;
//    requires jsr305; // https://github.com/google/guava/issues/2960
//    requires java.xml;
//    requires dropwizard.core;
//    requires net.shibboleth.utilities.java.support;
//    requires org.opensaml.saml.impl;
//    requires org.opensaml.xmlsec.impl;
//    requires dropwizard.client;
//    requires com.fasterxml.jackson.annotation;
//    requires nimbus.jose.jwt;
//    requires commons.collections;
//    requires xmlsec;
//    requires dropwizard.servlets;
//    requires java.xml.crypto; // only for MetadataSignatureTrustEngineFactoryTest
//    requires java.ws.rs;
//    requires com.codahale.metrics.health;
//    requires jakarta.inject;
//    requires org.apache.httpcomponents.httpclient;
//    requires commons.codec;
//}