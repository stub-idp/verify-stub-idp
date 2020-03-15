@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module stubidp.saml {
    exports stubidp.saml.stubidp.configuration;
    exports stubidp.saml.stubidp.stub.transformers.outbound;
    exports stubidp.saml.stubidp.stub.transformers.inbound;
    
    requires transitive jakarta.inject;
    requires transitive org.opensaml.saml;
    requires transitive stubidp.saml.utils;
    requires transitive stubidp.saml.security;
    requires transitive java.validation;

    // only for tests
    opens stubidp.saml.stubidp.stub.transformers.inbound;
    opens stubidp.saml.stubidp.stub.transformers.outbound;
    opens stubidp.saml.stubidp;

    requires stubidp.saml.extensions;
    requires com.fasterxml.jackson.annotation;
    requires stubidp.saml.serializers; // compile dependency, but for tests
}