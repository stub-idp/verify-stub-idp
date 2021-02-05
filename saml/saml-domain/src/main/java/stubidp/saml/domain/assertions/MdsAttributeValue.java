package stubidp.saml.domain.assertions;

import java.time.LocalDate;

public interface MdsAttributeValue {
    LocalDate getFrom();

    LocalDate getTo();

    boolean isVerified();
}
