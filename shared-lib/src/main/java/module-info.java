@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module stubidp.shared {
    opens assets.shared.scripts;
    opens stubidp.shared.configuration;
    opens stubidp.shared.cookies;
    opens stubidp.shared.csrf;
    opens stubidp.shared.views;

    exports stubidp.shared.cookies;
    exports stubidp.shared.csrf;
    exports stubidp.shared.domain;
    exports stubidp.shared.exceptions;
    exports stubidp.shared.views;
    exports stubidp.shared.configuration;
    exports stubidp.shared.repositories;

    requires transitive dropwizard.views.freemarker;
    requires transitive dropwizard.views;
    requires transitive jakarta.inject;
    requires transitive java.annotation;
    requires transitive java.ws.rs;
    requires transitive stubidp.rest.utils;
    requires transitive stubidp.security.utils;
    requires transitive stubidp.saml.metadata.bindings;

    requires freemarker;
    requires org.jboss.logging;
    requires org.jsoup;
    requires org.slf4j;
}