@SuppressWarnings("requires-automatic")
module stub.idp.saml {
    requires javax.inject;
    requires org.joda.time;
    requires org.opensaml.saml;
    requires saml.utils;
    requires saml.security;
    requires saml.extensions;
    requires com.fasterxml.jackson.annotation;
    requires java.validation;
}