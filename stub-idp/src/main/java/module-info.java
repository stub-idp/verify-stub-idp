@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module stubidp.stubidp {
    opens db.migrations.common;
    opens db.migrations.h2;
    opens db.migrations.postgres;
    opens stubidp.stubidp.builders;
    opens stubidp.stubidp.configuration;
    opens stubidp.stubidp.domain;
    opens stubidp.stubidp.dtos;
    opens stubidp.stubidp.filters;
    opens stubidp.stubidp.repositories;
    opens stubidp.stubidp.repositories.jdbc.json;
    opens stubidp.stubidp.repositories.jdbc;
    opens stubidp.stubidp.repositories.reaper;
    opens stubidp.stubidp.resources.eidas;
    opens stubidp.stubidp.resources.idp;
    opens stubidp.stubidp.resources.singleidp;
    opens stubidp.stubidp.resources;
    opens stubidp.stubidp.saml;
    opens stubidp.stubidp.security;
    opens stubidp.stubidp.services;
    opens stubidp.stubidp;

    exports stubidp.stubidp;
    exports stubidp.stubidp.configuration;
    exports stubidp.stubidp.domain;
    exports stubidp.stubidp.domain.factories;
    exports stubidp.stubidp.listeners;
    exports stubidp.stubidp.repositories;
    exports stubidp.stubidp.repositories.reaper;
    exports stubidp.stubidp.saml.transformers;

    exports stubidp.stubidp.dtos to com.fasterxml.jackson.databind;

    requires transitive dropwizard.client;
    requires transitive dropwizard.configuration;
    requires transitive dropwizard.lifecycle;
    requires transitive dropwizard.util;
    requires transitive jakarta.inject;
    requires transitive jersey.common;
    requires transitive org.jdbi.v3.core;
    requires transitive org.opensaml.saml;
    requires transitive org.opensaml.xmlsec;
    requires transitive stubidp.metrics.prometheus;
    requires transitive stubidp.rest.utils;
    requires transitive stubidp.saml.metadata.bindings;
    requires transitive stubidp.saml.security;
    requires transitive stubidp.saml.utils;
    requires transitive stubidp.saml;
    requires transitive stubidp.shared;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.google.common;
    requires dropwizard.assets;
    requires dropwizard.core;
    requires dropwizard.jackson;
    requires dropwizard.servlets;
    requires dropwizard.views.freemarker;
    requires dropwizard.views;
    requires hk2.api;
    requires java.servlet;
    requires java.sql;
    requires java.ws.rs;
    requires jbcrypt;
    requires org.apache.commons.text;
    requires org.flywaydb.core;
    requires org.jboss.logging;
    requires org.jsoup;
    requires org.opensaml.saml.impl;
    requires org.slf4j;
    requires stubidp.common.utils;
    requires stubidp.logging.dropwizard.logstash;
    requires stubidp.saml.extensions;
    requires stubidp.saml.hub;
}