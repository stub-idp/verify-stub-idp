package stubidp.saml.extensions.extensions;

import java.time.LocalDate;

public interface BaseMdsSamlObject {

    String FROM_ATTRIB_NAME = "From";
    String TO_ATTRIB_NAME = "To";
    String VERIFIED_ATTRIB_NAME = "Verified";

    LocalDate getFrom();

    void setFrom(LocalDate fromTime);

    LocalDate getTo();

    void setTo(LocalDate toTime);

    boolean getVerified();

    void setVerified(boolean verified);
}
