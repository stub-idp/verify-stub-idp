@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module stubidp.test.utils {
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires dropwizard.client;
    requires dropwizard.jackson;
    requires dropwizard.util;
    requires java.servlet;
    requires java.validation;
    requires java.ws.rs;
    requires org.checkerframework.checker.qual;
    requires org.eclipse.jetty.server;
    requires org.junit.jupiter.api;
}