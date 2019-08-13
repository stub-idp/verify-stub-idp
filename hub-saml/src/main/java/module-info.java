@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module hub.saml {
    requires org.joda.time;
    requires saml.utils;
    requires javax.inject;
    requires saml.security;
    requires saml.extensions;
    requires dropwizard.util;
    requires commons.lang;
}
