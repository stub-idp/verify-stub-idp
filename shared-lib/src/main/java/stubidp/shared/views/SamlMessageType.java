package stubidp.shared.views;

public enum SamlMessageType {
    SAML_RESPONSE("SAMLResponse"),
    SAML_REQUEST("SAMLRequest") ;

    SamlMessageType(String formName) {
        this.formName = formName;
    }

    private final String formName;
    public String toString() {
        return formName;
    }
}
