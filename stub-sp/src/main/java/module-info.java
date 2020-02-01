@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module stubsp.stubsp {
    opens stubsp.stubsp.configuration;
    opens stubsp.stubsp.domain;
    opens stubsp.stubsp.filters;
    opens stubsp.stubsp.views;
    opens stubsp.stubsp;

    exports stubsp.stubsp;
    exports stubsp.stubsp.builders;
    exports stubsp.stubsp.configuration;
    exports stubsp.stubsp.cookies;
    exports stubsp.stubsp.domain;
    exports stubsp.stubsp.filters;
    exports stubsp.stubsp.resources;
    exports stubsp.stubsp.services;
    exports stubsp.stubsp.views;

    requires transitive dropwizard.core;
    requires transitive dropwizard.configuration;
    requires transitive jersey.common;
    requires transitive hk2.api;
    requires transitive stubidp.metrics.prometheus;
    requires transitive stubidp.rest.utils;
    requires transitive stubidp.saml;
    requires transitive stubidp.saml.hub;
    requires transitive stubidp.saml.metadata.bindings;
    requires transitive stubidp.saml.utils;
    requires transitive stubidp.security.utils;
    requires transitive stubidp.shared;

    requires com.fasterxml.jackson.annotation;
    requires dropwizard.assets;
    requires java.validation;
    requires org.jboss.logging;
}