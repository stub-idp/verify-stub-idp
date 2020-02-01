@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module stubidp.rest.utils {
    opens stubidp.utils.rest.common;
    opens stubidp.utils.rest.jerseyclient;
    opens stubidp.utils.rest.analytics;
    opens stubidp.utils.rest.truststore;

    exports stubidp.utils.rest.bundles;
    exports stubidp.utils.rest.cache;
    exports stubidp.utils.rest.common;
    exports stubidp.utils.rest.configuration;
    exports stubidp.utils.rest.filters;
    exports stubidp.utils.rest.jerseyclient;
    exports stubidp.utils.rest.restclient;
    exports stubidp.utils.rest.resources;
    exports stubidp.utils.rest.truststore;

    requires transitive com.codahale.metrics;
    requires transitive com.fasterxml.jackson.databind;
    requires transitive dropwizard.client;
    requires transitive dropwizard.core;
    requires transitive dropwizard.util;
    requires transitive jakarta.inject;
    requires transitive java.servlet;
    requires transitive java.validation;
    requires transitive java.ws.rs;
    requires transitive org.apache.httpcomponents.httpclient;
    requires transitive org.apache.httpcomponents.httpcore;
    requires transitive org.slf4j;

    requires com.fasterxml.jackson.core;
    requires com.google.common;
    requires stubidp.logging.dropwizard.logstash;
    requires dropwizard.servlets;
    requires dropwizard.jetty;
    requires dropwizard.jersey;
    requires java.xml.bind;
    requires jersey.server;
    requires logback.classic;
}