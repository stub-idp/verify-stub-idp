package uk.gov.ida.rp.testrp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stubidp.saml.domain.assertions.TransactionIdaStatus;
import uk.gov.ida.rp.testrp.domain.PageErrorMessageDetails;
import uk.gov.ida.rp.testrp.domain.PageErrorMessageDetailsFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class PageErrorMessageDetailsFactoryTest {


    private PageErrorMessageDetailsFactory pageErrorMessageDetailsFactory;

    @BeforeEach
    public void setUp() throws Exception {
        pageErrorMessageDetailsFactory = new PageErrorMessageDetailsFactory();
    }

    @Test
    public void getErrorMessage_whereStatusAbsent_shouldSetHeaderAndMessageToAbsent() {

        Optional<TransactionIdaStatus> errorStatus = Optional.empty();

        PageErrorMessageDetails pageErrorMessageDetails = pageErrorMessageDetailsFactory.getErrorMessage(errorStatus);

        assertThat(pageErrorMessageDetails.getHeader()).isEqualTo(Optional.empty());
        assertThat(pageErrorMessageDetails.getMessage()).isEqualTo(Optional.empty());
    }

    @Test
    public void getErrorMessage_whereStatusPresent_shouldSetHeaderToErrorHeaderString() {

        Optional<TransactionIdaStatus> errorStatus = Optional.of(TransactionIdaStatus.AuthenticationFailed);

        PageErrorMessageDetails pageErrorMessageDetails = pageErrorMessageDetailsFactory.getErrorMessage(errorStatus);

        assertThat(pageErrorMessageDetails.getHeader()).isEqualTo(Optional.<String>of("There has been a problem signing you in."));
    }

    @Test
    public void getErrorMessage_whereStatusIsAuthenticationFailed_shouldSetAuthnFailedMessage() {

        Optional<TransactionIdaStatus> errorStatus = Optional.of(TransactionIdaStatus.AuthenticationFailed);

        PageErrorMessageDetails pageErrorMessageDetails = pageErrorMessageDetailsFactory.getErrorMessage(errorStatus);

        assertThat(pageErrorMessageDetails.getMessage()).isEqualTo(Optional.<String>of("Please try again or use an identity profile with a different certified company."));
    }

    @Test
    public void getErrorMessage_whereStatusIsRequesterError_shouldSetRequesterErrorMessage() {

        Optional<TransactionIdaStatus> errorStatus = Optional.of(TransactionIdaStatus.RequesterError);

        PageErrorMessageDetails pageErrorMessageDetails = pageErrorMessageDetailsFactory.getErrorMessage(errorStatus);

        assertThat(pageErrorMessageDetails.getMessage()).isEqualTo(Optional.<String>of("Requester Error."));
    }

    @Test
    public void getErrorMessage_whereStatusIsNoMatchingServiceMatchFromHub_shouldSetNoMatchingServiceMatchErrorMessage() {

        final TransactionIdaStatus unknownStatus = TransactionIdaStatus.NoMatchingServiceMatchFromHub;
        Optional<TransactionIdaStatus> errorStatus = Optional.of(unknownStatus);

        PageErrorMessageDetails pageErrorMessageDetails = pageErrorMessageDetailsFactory.getErrorMessage(errorStatus);

        assertThat(pageErrorMessageDetails.getMessage()).isEqualTo(Optional.<String>of("No user account matched the supplied details."));
    }

    @Test
    public void getErrorMessage_whereStatusIsNotKnown_shouldSetUnknownErrorMessage() {

        final TransactionIdaStatus unknownStatus = TransactionIdaStatus.NoAuthenticationContext;
        Optional<TransactionIdaStatus> errorStatus = Optional.of(unknownStatus);

        PageErrorMessageDetails pageErrorMessageDetails = pageErrorMessageDetailsFactory.getErrorMessage(errorStatus);

        assertThat(pageErrorMessageDetails.getMessage()).isEqualTo(Optional.<String>of(unknownStatus.toString() + " - Unknown status code encountered."));
    }
}
