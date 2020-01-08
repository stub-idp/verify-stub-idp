package stubidp.stubidp.services;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.mindrot.jbcrypt.BCrypt;
import stubidp.saml.utils.core.domain.Address;
import stubidp.saml.utils.core.domain.AuthnContext;
import stubidp.saml.utils.core.domain.Gender;
import stubidp.stubidp.domain.DatabaseIdpUser;
import stubidp.stubidp.domain.MatchingDatasetValue;
import stubidp.stubidp.exceptions.IncompleteRegistrationException;
import stubidp.stubidp.exceptions.InvalidDateException;
import stubidp.stubidp.exceptions.InvalidSessionIdException;
import stubidp.stubidp.exceptions.InvalidUsernameOrPasswordException;
import stubidp.stubidp.exceptions.UsernameAlreadyTakenException;
import stubidp.stubidp.repositories.Idp;
import stubidp.stubidp.repositories.IdpSession;
import stubidp.stubidp.repositories.IdpSessionRepository;
import stubidp.stubidp.repositories.IdpStubsRepository;
import stubidp.utils.rest.common.SessionId;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static java.util.Arrays.asList;

public class IdpUserService {
    private final IdpSessionRepository sessionRepository;
    private final IdpStubsRepository idpStubsRepository;

    @Inject
    public IdpUserService(
            IdpSessionRepository sessionRepository,
            IdpStubsRepository idpStubsRepository) {

        this.sessionRepository = sessionRepository;
        this.idpStubsRepository = idpStubsRepository;
    }

    public void attachIdpUserToSession(String idpName, String username, String password, SessionId idpSessionId) throws InvalidUsernameOrPasswordException, InvalidSessionIdException {
        Idp idp = idpStubsRepository.getIdpWithFriendlyId(idpName);
        Optional<DatabaseIdpUser> user = idp.getUser(username, password);
        attachIdpUserToSession(user, idpSessionId);
    }

    public void createAndAttachIdpUserToSession(String idpName,
                                                String firstname, String surname,
                                                String addressLine1, String addressLine2, String addressTown, String addressPostCode,
                                                AuthnContext levelOfAssurance,
                                                String dateOfBirth,
                                                Optional<Gender> gender,
                                                String username, String password,
                                                SessionId idpSessionId) throws InvalidSessionIdException, IncompleteRegistrationException, InvalidDateException, UsernameAlreadyTakenException, InvalidUsernameOrPasswordException {
        Idp idp = idpStubsRepository.getIdpWithFriendlyId(idpName);
        DatabaseIdpUser user = createUserInIdp(firstname, surname, addressLine1, addressLine2, addressTown, addressPostCode, levelOfAssurance, dateOfBirth, gender, username, password, idp);
        attachIdpUserToSession(Optional.ofNullable(user), idpSessionId);
    }

    public DatabaseIdpUser createIdpUser(String idpName,
                                                String firstname, String surname,
                                                String addressLine1, String addressLine2, String addressTown, String addressPostCode,
                                                AuthnContext levelOfAssurance,
                                                String dateOfBirth,
                                                String username, String password
                                                ) throws IncompleteRegistrationException, InvalidDateException, UsernameAlreadyTakenException {
        Idp idp = idpStubsRepository.getIdpWithFriendlyId(idpName);
        return createUserInIdp(firstname, surname, addressLine1, addressLine2, addressTown, addressPostCode, levelOfAssurance, dateOfBirth, Optional.empty(), username, password, idp);
    }

    public void attachIdpUserToSession(Optional<DatabaseIdpUser> user, SessionId idpSessionId) throws InvalidUsernameOrPasswordException, InvalidSessionIdException {

        if (user.isEmpty()) {
            throw new InvalidUsernameOrPasswordException();
        }

        Optional<IdpSession> session = sessionRepository.get(idpSessionId);

        if (session.isEmpty()) {
            throw new InvalidSessionIdException();
        }

        session.get().setIdpUser(user);
        sessionRepository.updateSession(session.get().getSessionId(), session.get());
    }

    private DatabaseIdpUser createUserInIdp(String firstname, String surname, String addressLine1, String addressLine2, String addressTown, String addressPostCode, final AuthnContext _levelOfAssurance, String dateOfBirth, Optional<Gender> gender, String username, String password, Idp idp) throws IncompleteRegistrationException, InvalidDateException, UsernameAlreadyTakenException {
        if (!isMandatoryDataPresent(firstname, surname, addressLine1, addressLine2, addressTown, addressPostCode, dateOfBirth, username, password)) {
            throw new IncompleteRegistrationException();
        }

        LocalDate parsedDateOfBirth;
        try {
            parsedDateOfBirth = LocalDate.parse(dateOfBirth, DateTimeFormat.forPattern("yyyy-MM-dd"));
        } catch (IllegalArgumentException e) {
            throw new InvalidDateException();
        }

        boolean usernameAlreadyTaken = idp.userExists(username);
        if (usernameAlreadyTaken) {
            throw new UsernameAlreadyTakenException();
        }

        Address address = new Address(asList(addressLine1, addressLine2, addressTown), addressPostCode, null, null, null, null, false);

        AuthnContext levelOfAssurance = _levelOfAssurance;
        if ("LevelZeroUser".equals(username)) {
            levelOfAssurance = AuthnContext.LEVEL_X;
        }

        return idp.createUser(
                Optional.empty(),
                Collections.singletonList(createMdsValue(Optional.ofNullable(firstname))),
                Collections.emptyList(),
                Collections.singletonList(createMdsValue(Optional.ofNullable(surname))),
                gender.map(IdpUserService::createSimpleMdsValue2),
                Collections.singletonList(createMdsValue(Optional.ofNullable(parsedDateOfBirth))),
                Collections.singletonList(address),
                username,
                BCrypt.hashpw(password, BCrypt.gensalt()),
                levelOfAssurance);
    }

    private boolean isMandatoryDataPresent(String... args) {
        for (String arg : args) {
            if (arg == null || arg.trim().length() == 0) {
                return false;
            }
        }

        return true;
    }

    private static <T> MatchingDatasetValue<T> createMdsValue(Optional<T> value) {
        return new MatchingDatasetValue<>(value.get(), null, null, true);
    }

    public static DatabaseIdpUser createRandomUser() {
        return new DatabaseIdpUser(
                "tempuser",
                UUID.randomUUID().toString(),
                "ifitellyouthen...",
                Collections.singletonList(createSimpleMdsValue2("firstname")),
                Collections.emptyList(),
                Collections.singletonList(createSimpleMdsValue2("smith")),
                Optional.of(createSimpleMdsValue2(Gender.FEMALE)),
                Collections.emptyList(),
                Collections.singletonList(
                        new Address(asList("line1", "line2"), "KT23 4XD", null, "fhfhf", DateTime.parse("2000-01-01"), DateTime.parse("2013-05-05"), false)
                ),
                AuthnContext.LEVEL_2);
    }

    private static <T> MatchingDatasetValue<T> createSimpleMdsValue2(T value) {
        return new MatchingDatasetValue<>(value, DateTime.parse("2000-01-01"), DateTime.parse("2013-01-03"), false);
    }

}
