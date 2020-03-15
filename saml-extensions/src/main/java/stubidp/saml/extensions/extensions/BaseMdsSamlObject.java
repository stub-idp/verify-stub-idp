package stubidp.saml.extensions.extensions;

import java.time.Instant;

public interface BaseMdsSamlObject {

    String FROM_ATTRIB_NAME = "From";
    String TO_ATTRIB_NAME = "To";
    String VERIFIED_ATTRIB_NAME = "Verified";

    Instant getFrom();

    void setFrom(Instant fromTime);

    Instant getTo();

    void setTo(Instant toTime);

    boolean getVerified();

    void setVerified(boolean verified);
}
