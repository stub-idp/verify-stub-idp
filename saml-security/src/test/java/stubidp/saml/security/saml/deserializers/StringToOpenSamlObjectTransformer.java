package stubidp.saml.security.saml.deserializers;

import org.opensaml.saml.saml2.core.AuthnRequest;
import stubidp.saml.security.saml.StringEncoding;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class StringToOpenSamlObjectTransformer implements Function<String, AuthnRequest> {

    private static final Pattern PATTERN = Pattern.compile("\\s");
    private static final Predicate<String> BASE64_MATCH_PREDICATE = Pattern.compile("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$").asMatchPredicate();

    private final AuthnRequestUnmarshaller authnRequestUnmarshaller;

    public StringToOpenSamlObjectTransformer(
            final AuthnRequestUnmarshaller authnRequestUnmarshaller) {
        this.authnRequestUnmarshaller = authnRequestUnmarshaller;
    }

    @Override
    public AuthnRequest apply(final String input) {
        if (Objects.isNull(input)) {
            throw new RuntimeException("SAML was null");
        }
        final String decodedInput = decode(input);
        return authnRequestUnmarshaller.fromString(decodedInput);
    }

    private String decode(String input) {
        String withoutWhitespace = PATTERN.matcher(input).replaceAll("");
        if (!BASE64_MATCH_PREDICATE.test(withoutWhitespace)) {
            throw new RuntimeException("Invalid Base64 string");
        }
        return StringEncoding.fromBase64Encoded(withoutWhitespace);
    }

}
