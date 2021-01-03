@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module stubidp.saml.utils {
    opens stubidp.saml.utils.core.domain;
    opens stubidp.saml.utils.core.transformers.inbound;
    opens stubidp.saml.utils.core.transformers.outbound.decorators;
    opens stubidp.saml.utils.core.transformers;
    opens stubidp.saml.utils.hub.validators;
    opens stubidp.saml.utils.metadata.transformers;
    opens stubidp.saml.utils.core.transformers.outbound;
    opens stubidp.saml.utils.hub.transformers.outbound;
    opens stubidp.saml.utils.hub.factories;

    exports stubidp.saml.utils.core.api;
    exports stubidp.saml.utils.core.domain;
    exports stubidp.saml.utils.core.transformers.inbound;
    exports stubidp.saml.utils.core.transformers.outbound.decorators;
    exports stubidp.saml.utils.core.transformers.outbound;
    exports stubidp.saml.utils.core.transformers;
    exports stubidp.saml.utils.core;
    exports stubidp.saml.utils.hub.factories;
    exports stubidp.saml.utils.hub.transformers.inbound.decorators;
    exports stubidp.saml.utils.hub.transformers.outbound;
    exports stubidp.saml.utils.hub.validators;
    exports stubidp.saml.utils.metadata.transformers;

    requires transitive jakarta.inject;
    requires transitive java.validation;
    requires transitive org.opensaml.saml;
    requires transitive org.opensaml.security;
    requires transitive org.opensaml.xmlsec;
    requires transitive stubidp.saml.domain;
    requires transitive stubidp.saml.extensions;
    requires transitive stubidp.saml.security;
    requires transitive stubidp.saml.serializers;

    requires com.fasterxml.jackson.annotation;
    requires java.xml;
    requires org.opensaml.core;
    requires org.opensaml.saml.impl;
    requires org.slf4j;
    requires stubidp.security.utils;
}