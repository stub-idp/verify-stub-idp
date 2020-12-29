package stubidp.saml.domain.assertions;

import java.time.Instant;

public interface MdsAttributeValue {
    Instant getFrom();

    Instant getTo();

    boolean isVerified();
}
