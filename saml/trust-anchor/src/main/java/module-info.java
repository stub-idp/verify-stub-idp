@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module stubidp.trust.anchor {
    exports stubidp.eidas.trustanchor;

    opens stubidp.eidas.metadata;
    opens stubidp.eidas.trustanchor;

    requires transitive com.nimbusds.jose.jwt;

    requires java.xml;
    requires net.shibboleth.utilities.java.support;
    requires org.apache.santuario.xmlsec;
    requires org.bouncycastle.provider;
    requires org.opensaml.core;
    requires org.opensaml.saml.impl;
    requires org.opensaml.saml;
    requires org.opensaml.security.impl;
    requires org.opensaml.security;
    requires org.opensaml.xmlsec.impl;
    requires org.opensaml.xmlsec;
    requires stubidp.saml.extensions;
    requires stubidp.saml.serializers;
}
