@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module saml.utils {
    exports stubidp.saml.utils.core;
    exports stubidp.saml.utils.core.domain;
    exports stubidp.saml.utils.core.transformers.outbound;
    exports stubidp.saml.utils.core.transformers.outbound.decorators;
    exports stubidp.saml.utils.hub.domain;
    exports stubidp.saml.utils.hub.factories;
    exports stubidp.saml.utils.core.transformers;
    exports stubidp.saml.utils.metadata.transformers;
    exports stubidp.saml.utils.hub.validators;
    exports stubidp.saml.utils.core.api;
    exports stubidp.saml.utils.hub.transformers.inbound.decorators;
    exports stubidp.saml.utils.hub.transformers.outbound;
    exports stubidp.saml.utils.core.transformers.inbound;

    requires com.fasterxml.jackson.annotation;
    requires org.opensaml.core;
    requires java.xml;
    requires security.utils;
    requires org.opensaml.saml.impl;
    requires org.slf4j;

    requires transitive saml.serializers;
    requires transitive saml.security;
    requires transitive org.opensaml.xmlsec;
    requires transitive org.opensaml.security;
    requires transitive saml.extensions;
    requires transitive java.validation;
    requires transitive org.joda.time;
    requires transitive org.opensaml.saml;
    requires transitive javax.inject;
}