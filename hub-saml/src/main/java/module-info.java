@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module stubidp.saml.hub {
    opens stubidp.saml.hub.hub.transformers.inbound;
    opens stubidp.saml.hub.hub.transformers.outbound;
    opens stubidp.saml.hub.hub.transformers.outbound.decorators;
    opens stubidp.saml.hub.hub.validators.authnrequest;
    opens stubidp.saml.hub.hub.validators.response.idp.components;
    opens stubidp.saml.hub.hub.validators.response.idp;
    opens stubidp.saml.hub.hub.validators.response.matchingservice;
    opens stubidp.saml.hub.core;
    opens stubidp.saml.hub.core.validators.assertion;

    exports stubidp.saml.hub.core.domain;
    exports stubidp.saml.hub.core.errors;
    exports stubidp.saml.hub.core.validators.assertion;
    exports stubidp.saml.hub.core.validators.subject;
    exports stubidp.saml.hub.core.validators.subjectconfirmation;
    exports stubidp.saml.hub.core.validators;
    exports stubidp.saml.hub.core.transformers.outbound.decorators;
    exports stubidp.saml.hub.core;
    exports stubidp.saml.hub.hub.configuration;
    exports stubidp.saml.hub.hub.domain;
    exports stubidp.saml.hub.hub.exception;
    exports stubidp.saml.hub.hub.factories;
    exports stubidp.saml.hub.hub.transformers.inbound.providers;
    exports stubidp.saml.hub.hub.transformers.inbound;
    exports stubidp.saml.hub.hub.transformers.outbound.decorators;
    exports stubidp.saml.hub.hub.transformers.outbound;
    exports stubidp.saml.hub.hub.validators.authnrequest;
    exports stubidp.saml.hub.hub.validators.response.common;
    exports stubidp.saml.hub.hub.validators.response.idp.components;
    exports stubidp.saml.hub.hub.validators.response.idp;
    exports stubidp.saml.hub.hub.validators.response.matchingservice;
    exports stubidp.saml.hub.metadata;

    requires transitive jakarta.inject;
    requires transitive org.opensaml.saml;
    requires transitive stubidp.saml.security;
    requires transitive stubidp.saml.serializers;
    requires transitive stubidp.saml.utils;

    requires stubidp.saml.extensions;
    requires org.apache.santuario.xmlsec;
}
