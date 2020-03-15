package stubidp.saml.extensions.validation.errors;

import org.slf4j.event.Level;
import stubidp.saml.extensions.validation.SamlDocumentReference;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class RequestFreshnessValidationSpecification extends SamlValidationSpecificationFailure {
    private static final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
            .appendInstant(0)
            .toFormatter()
            .withZone(ZoneId.of("UTC"));

    public static final String REQUEST_TOO_OLD = "Request ID {0} too old (request issueInstant {1}, current time {2}).";

    public RequestFreshnessValidationSpecification(String errorFormat, String requestId, Instant issueInstant, Instant currentTime) {
        super(MessageFormat.format(errorFormat, requestId, dateTimeFormatter.format(issueInstant), dateTimeFormatter.format(currentTime)), false, Level.WARN);
    }

    @Override
    public SamlDocumentReference documentReference() {
        return SamlDocumentReference.unspecified();
    }
}
