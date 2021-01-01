package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.core.StatusCode;
import stubidp.saml.extensions.domain.SamlStatusCode;
import stubidp.saml.test.OpenSamlXmlObjectFactory;

import java.util.Optional;

public class StatusCodeBuilder {
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    private Optional<String> value = Optional.of(StatusCode.SUCCESS);
    private Optional<StatusCode> subStatus = Optional.empty();

    private StatusCodeBuilder() {}

    public static StatusCodeBuilder aStatusCode() {
        return new StatusCodeBuilder();
    }

    public StatusCode build() {
        StatusCode statusCode = openSamlXmlObjectFactory.createStatusCode();
        value.ifPresent(statusCode::setValue);
        subStatus.ifPresent(statusCode::setStatusCode);
        return statusCode;
    }

    public StatusCodeBuilder withValue(String value) {
        this.value = Optional.ofNullable(value);
        return this;
    }

    public StatusCodeBuilder withSubStatusCode(StatusCode subStatusCode){
        this.subStatus = Optional.ofNullable(subStatusCode);
        return this;
    }

    public StatusCodeBuilder forMatchingService() {
        this.withSubStatusCode(aStatusCode().withValue(SamlStatusCode.MATCH).build());
        return this;
    }
}
