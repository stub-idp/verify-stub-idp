package stubidp.saml.utils.core.domain;

import java.time.Instant;
import java.util.Optional;

public interface MdsAttributeValue {
    Instant getFrom();

    Optional<Instant> getTo();

    boolean isVerified();
}
