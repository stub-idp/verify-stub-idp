package uk.gov.ida.rp.testrp.domain;

import stubidp.saml.domain.assertions.TransactionIdaStatus;

import javax.inject.Inject;
import java.util.Optional;

import static java.text.MessageFormat.format;

public class PageErrorMessageDetailsFactory {

    @Inject
    public PageErrorMessageDetailsFactory() { }

    public PageErrorMessageDetails getErrorMessage(Optional<TransactionIdaStatus> idpErrorCode) {
        Optional<String> header = Optional.empty();
        Optional<String> message = Optional.empty();

        if(idpErrorCode.isPresent()) {
            header = Optional.of("There has been a problem signing you in.");

            switch(idpErrorCode.get()) {
                case AuthenticationFailed:
                    message = Optional.of("Please try again or use an identity profile with a different certified company.");
                    break;
                case RequesterError:
                    message = Optional.of("Requester Error.");
                    break;
                case NoMatchingServiceMatchFromHub:
                    message = Optional.of("No user account matched the supplied details.");
                    break;
                default:
                    message = Optional.of(format("{0} - Unknown status code encountered.", idpErrorCode.get()));
                    break;
            }
        }

        return new PageErrorMessageDetails(header, message);
    }
}
