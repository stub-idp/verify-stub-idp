package stubidp.saml.extensions.extensions.eidas.impl;

import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import stubidp.saml.extensions.extensions.eidas.TransliterableString;

public abstract class AbstractTransliterableString extends XSAnyImpl implements TransliterableString {

    private Boolean isLatinScript = true;

    AbstractTransliterableString(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    @Override
    public Boolean isLatinScript() {
        return isLatinScript;
    }

    @Override
    public void setIsLatinScript(Boolean isLatinScript) {
        this.isLatinScript = prepareForAssignment(this.isLatinScript, isLatinScript);
    }
}
