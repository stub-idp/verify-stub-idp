@SuppressWarnings("requires-automatic")
module stub.idp.saml {
    exports stubidp.saml.stubidp.configuration;
    exports stubidp.saml.stubidp.stub.transformers.outbound;
    exports stubidp.saml.stubidp.stub.transformers.inbound;
    
    requires javax.inject;
    requires org.joda.time;
    requires org.opensaml.saml;
    requires saml.utils;
    requires saml.security;
    requires saml.extensions;
    requires com.fasterxml.jackson.annotation;
    requires java.validation;
    requires saml.serializers; // compile dependency, but for tests
}