package stubidp.stubidp.services;

import com.google.common.base.Strings;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import stubidp.saml.utils.core.domain.AuthnContext;
import stubidp.stubidp.domain.DatabaseEidasUser;
import stubidp.stubidp.domain.EidasScheme;
import stubidp.stubidp.domain.EidasUser;
import stubidp.stubidp.domain.MatchingDatasetValue;
import stubidp.stubidp.exceptions.IncompleteRegistrationException;
import stubidp.stubidp.exceptions.InvalidDateException;
import stubidp.stubidp.exceptions.InvalidSessionIdException;
import stubidp.stubidp.exceptions.InvalidUsernameOrPasswordException;
import stubidp.stubidp.exceptions.UsernameAlreadyTakenException;
import stubidp.stubidp.repositories.EidasSession;
import stubidp.stubidp.repositories.EidasSessionRepository;
import stubidp.stubidp.repositories.StubCountry;
import stubidp.stubidp.repositories.StubCountryRepository;

import javax.inject.Inject;
import java.util.Optional;

public class StubCountryService {

    private final StubCountryRepository stubCountryRepository;
    private final EidasSessionRepository sessionRepository;

    @Inject
    public StubCountryService(StubCountryRepository stubCountryRepository, EidasSessionRepository sessionRepository) {
        this.stubCountryRepository = stubCountryRepository;
        this.sessionRepository = sessionRepository;
    }

    public void attachStubCountryToSession(EidasScheme eidasScheme, String username, String password, EidasSession session) throws InvalidUsernameOrPasswordException, InvalidSessionIdException {
        StubCountry stubCountry = stubCountryRepository.getStubCountryWithFriendlyId(eidasScheme);
        Optional<DatabaseEidasUser> user = stubCountry.getUser(username, password);
        attachEidasUserToSession(user, session);
    }

    public void createAndAttachIdpUserToSession(EidasScheme eidasScheme,
                                                String username, String password,
                                                EidasSession idpSessionId,
                                                String firstName,
                                                String nonLatinFirstname,
                                                String surname,
                                                String nonLatinSurname,
                                                String dob,
                                                AuthnContext levelOfAssurance) throws InvalidSessionIdException, InvalidUsernameOrPasswordException, InvalidDateException, IncompleteRegistrationException, UsernameAlreadyTakenException {
        StubCountry stubCountry = stubCountryRepository.getStubCountryWithFriendlyId(eidasScheme);
        DatabaseEidasUser user = createEidasUserInStubCountry(
                username, password, stubCountry, firstName, nonLatinFirstname,
                surname, nonLatinSurname, dob, levelOfAssurance
        );
        attachEidasUserToSession(Optional.of(user), idpSessionId);
    }

    private DatabaseEidasUser createEidasUserInStubCountry(String username,
                                                           String password,
                                                           StubCountry stubCountry,
                                                           String firstName,
                                                           String nonLatinFirstname,
                                                           String surname,
                                                           String nonLatinSurname,
                                                           String dob,
                                                           AuthnContext levelOfAssurance)
            throws InvalidDateException, IncompleteRegistrationException, UsernameAlreadyTakenException {

        if (!isMandatoryDataPresent(firstName, surname, dob, username, password)) {
            throw new IncompleteRegistrationException();
        }

        LocalDate parsedDateOfBirth;
        try {
            parsedDateOfBirth = LocalDate.parse(dob, DateTimeFormat.forPattern("yyyy-MM-dd"));
        } catch (IllegalArgumentException e) {
            throw new InvalidDateException();
        }

        boolean usernameAlreadyTaken = stubCountry.userExists(username);
        if (usernameAlreadyTaken) {
            throw new UsernameAlreadyTakenException();
        }

        return stubCountry.createUser(
                username, password,
                createMdsValue(firstName), createOptionalMdsValue(nonLatinFirstname),
                createMdsValue(surname), createOptionalMdsValue(nonLatinSurname),
                createMdsValue(parsedDateOfBirth),
                levelOfAssurance);
    }

    private void attachEidasUserToSession(Optional<DatabaseEidasUser> user, EidasSession session) throws InvalidUsernameOrPasswordException, InvalidSessionIdException {
        if (!user.isPresent()) {
            throw new InvalidUsernameOrPasswordException();
        }
        EidasUser eidasUser = createEidasUser(user);

        session.setEidasUser(eidasUser);

        if (!session.getEidasUser().isPresent()) {
            throw new InvalidSessionIdException();
        }
        sessionRepository.updateSession(session.getSessionId(), session);
    }

    private EidasUser createEidasUser(Optional<DatabaseEidasUser> optionalUser) {

        DatabaseEidasUser user = optionalUser.get();
        EidasUser eidasUser = new EidasUser(
                user.getFirstname().getValue(),
                getOptionalValue(user.getNonLatinFirstname()),
                user.getSurname().getValue(),
                getOptionalValue(user.getNonLatinSurname()),
                user.getPersistentId(),
                user.getDateOfBirth().getValue(),
                Optional.empty(),
                Optional.empty()
        );

        return eidasUser;
    }

    private Optional<String> getOptionalValue(Optional<MatchingDatasetValue<String>> fieldValue) {
        return Optional.ofNullable(fieldValue.map(MatchingDatasetValue::getValue).orElse(null));
    }

    private <T> MatchingDatasetValue<T> createMdsValue(T value) {
        return new MatchingDatasetValue<>(value, null, null, true);
    }

    private Optional<MatchingDatasetValue<String>> createOptionalMdsValue(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return Optional.empty();
        }
        return Optional.of(new MatchingDatasetValue<>(value, null, null, true));
    }

    private boolean isMandatoryDataPresent(String... args) {
        for (String arg : args) {
            if (arg == null || arg.trim().length() == 0) {
                return false;
            }
        }

        return true;
    }
}
