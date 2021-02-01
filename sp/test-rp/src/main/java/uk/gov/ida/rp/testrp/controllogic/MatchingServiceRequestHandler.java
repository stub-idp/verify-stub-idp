package uk.gov.ida.rp.testrp.controllogic;

import org.joda.time.LocalDate;
import uk.gov.ida.rp.testrp.contract.UniversalMatchingDatasetDto;
import uk.gov.ida.rp.testrp.contract.GenderDto;
import uk.gov.ida.rp.testrp.contract.MatchingServiceRequestDto;
import uk.gov.ida.rp.testrp.contract.MatchingServiceResponseDto;
import uk.gov.ida.rp.testrp.contract.UnknownUserCreationRequestDto;
import uk.gov.ida.rp.testrp.contract.UnknownUserCreationResponseDto;
import uk.gov.ida.rp.testrp.repositories.Session;
import uk.gov.ida.rp.testrp.repositories.SessionRepository;

import javax.inject.Inject;
import java.util.Optional;

public class MatchingServiceRequestHandler {

    private final SessionRepository sessionRepository;

    @Inject
    public MatchingServiceRequestHandler(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public MatchingServiceResponseDto handleMatchingRequest(MatchingServiceRequestDto matchingServiceRequestDto) {
        final String hashedPid = matchingServiceRequestDto.getHashedPid();
        final String requestId = matchingServiceRequestDto.getMatchId();

        delayForDemo(matchingServiceRequestDto);

        UniversalMatchingDatasetDto matchingDataset = matchingServiceRequestDto.getMatchingDataset();

        Optional<Session> session = sessionRepository.getSessionForRequestId(requestId);

        if(session.isPresent()) {
            session.get().setMatchedHashedPid(hashedPid);
            sessionRepository.updateSession(session.get().getSessionId(), session.get());
        }

        //return no match if the matching dataset matches the below two users as the acceptance tests would like
        //to create user accounts for them. these users are defined in
        // https://github.gds/gds/ida-hub-acceptance-tests/blob/master/src/test/java/uk/gov/ida/acceptance/core/support/AcceptanceTestsBase.java
        if (userMatch("J", "Moriarti", "1822-11-27", matchingDataset) &&
                !matchingServiceRequestDto.getCycle3Dataset().isPresent()) {
            return MatchingServiceResponseDto.NO_MATCH_RESPONSE;
        }

        if (userMatch("Jack", "Griffin", "1983-06-21", matchingDataset)) {
            return MatchingServiceResponseDto.NO_MATCH_RESPONSE;
        }

        if (userMatch("Martin", "Riggs", "1970-04-12", matchingDataset ) &&
                !matchingServiceRequestDto.getCycle3Dataset().isPresent()){
            return MatchingServiceResponseDto.NO_MATCH_RESPONSE;
        }

        if (session.isPresent() && session.get().forceLMSNoMatch()) {
            return MatchingServiceResponseDto.NO_MATCH_RESPONSE;
        }

        if (isSpecialNewUserMatchingDataset(matchingServiceRequestDto.getMatchingDataset())) {
            return MatchingServiceResponseDto.NO_MATCH_RESPONSE;
        }

        if (matchingServiceRequestDto.getCycle3Dataset().isPresent()) {
            return MatchingServiceResponseDto.MATCH_RESPONSE;
        }

        if (isSpecialCycle3UserMatchingDataset(matchingServiceRequestDto.getMatchingDataset())) {
            return MatchingServiceResponseDto.NO_MATCH_RESPONSE;
        }

        return MatchingServiceResponseDto.MATCH_RESPONSE;
    }

    public UnknownUserCreationResponseDto handleUserAccountCreationRequest(UnknownUserCreationRequestDto request) {
        // suppose this is ok - it is using the hashedPid as an identifier for the user after all...
        final Optional<UnknownUserCreationResponseDto> unknownUserCreationResponseDto = sessionRepository.getResponseFromSessionForHashedPid(request.getHashedPid());
        if(unknownUserCreationResponseDto.isPresent()) {
            return unknownUserCreationResponseDto.get();
        }
        return UnknownUserCreationResponseDto.SUCCESS_RESPONSE;
    }

    private boolean userMatch(String firstName, String surname, String dateOfBirth, UniversalMatchingDatasetDto matchingDataset) {
        return matchingDataset.getSurnames().stream().anyMatch(s -> s.getValue().equals(surname)) &&
            matchingDataset.getFirstName().getValue().equals(firstName) &&
            matchingDataset.getDateOfBirth().getValue().toString().equals(dateOfBirth);
    }

    private boolean isSpecialNewUserMatchingDataset(UniversalMatchingDatasetDto matchingDataset) {
        return matchingDataset.getFirstName() != null && "Jack".equals(matchingDataset.getFirstName().getValue())
            && matchingDataset.getSurnames().size() == 1 && "Griffin".equals(matchingDataset.getSurnames().get(0).getValue());

    }

    private boolean isSpecialCycle3UserMatchingDataset(final UniversalMatchingDatasetDto matchingDataset) {
        return
            matchingDataset.getFirstName() != null && "J".equals(matchingDataset.getFirstName().getValue())
                && matchingDataset.getMiddleNames() == null
                && matchingDataset.getSurnames().size() == 2
                && "Moriarti".equals(matchingDataset.getSurnames().get(0).getValue())
                && "Barnes".equals(matchingDataset.getSurnames().get(1).getValue())
                && matchingDataset.getGender() != null && matchingDataset.getGender().getValue().equals(GenderDto.NOT_SPECIFIED)
                && matchingDataset.getDateOfBirth() != null && matchingDataset.getDateOfBirth().getValue().equals(LocalDate.parse("1822-11-27"))
                && matchingDataset.getAddresses().size() == 1
                && matchingDataset.getAddresses().get(0).getLines().size() == 1
                && "10 Two St".equals(matchingDataset.getAddresses().get(0).getLines().get(0))
                && matchingDataset.getAddresses().get(0).getPostCode().isPresent()
                && "1A 2BC".equals(matchingDataset.getAddresses().get(0).getPostCode().get());
    }

    private void delayForDemo(final MatchingServiceRequestDto matchingServiceRequestDto) {
        // Force a go-slow if the user has a DoB in 1968
        // (so the "we're looking for your record" screen is visible for a while)
        if (matchingServiceRequestDto.getMatchingDataset().getDateOfBirth() != null
            && matchingServiceRequestDto.getMatchingDataset().getDateOfBirth().getValue().getYear() == 1968) {
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                // Don't really care...
            }
        }
    }

}
