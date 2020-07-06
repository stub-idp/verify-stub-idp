package stubidp.saml.domain.assertions;

import java.time.Instant;
import java.util.Optional;

public interface MdsAttributeValue {
    Instant getFrom();

    Instant getTo();

    boolean isVerified();
}
