package stubidp.stubidp.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.joda.time.LocalDate;
import org.mindrot.jbcrypt.BCrypt;
import stubidp.saml.utils.core.domain.Address;
import stubidp.saml.utils.core.domain.Gender;
import stubidp.stubidp.domain.DatabaseIdpUser;
import stubidp.stubidp.domain.MatchingDatasetValue;
import stubidp.stubidp.security.BCryptHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonSerialize
@JsonInclude(value=NON_NULL)
public class IdpUserDto {

    private Optional<String> pid = Optional.empty();
    private String username;
    private String password;
    private Optional<MatchingDatasetValue<String>> firstName = Optional.empty();
    private Optional<MatchingDatasetValue<String>> middleNames = Optional.empty();
    private List<MatchingDatasetValue<String>> surname = new ArrayList<>();
    private Optional<MatchingDatasetValue<Gender>> gender = Optional.empty();
    private Optional<MatchingDatasetValue<LocalDate>> dateOfBirth = Optional.empty();
    private Optional<Address> address = Optional.empty();
    private String levelOfAssurance;

    @SuppressWarnings("unused")
    private IdpUserDto() {}

    public IdpUserDto(
            Optional<String> pid,
            String username,
            String password,
            Optional<MatchingDatasetValue<String>> firstName,
            Optional<MatchingDatasetValue<String>> middleNames,
            List<MatchingDatasetValue<String>> surnames,
            Optional<MatchingDatasetValue<Gender>> gender,
            Optional<MatchingDatasetValue<LocalDate>> dateOfBirth,
            Optional<Address> address,
            String levelOfAssurance) {

        this.pid = pid;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.middleNames = middleNames;
        this.surname = surnames;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.levelOfAssurance = levelOfAssurance;
    }

    public Optional<String> getPid() {
        return Optional.ofNullable(pid).get();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return this.password;
    }

    public Optional<MatchingDatasetValue<String>> getFirstName() {
        return firstName;
    }

    public List<MatchingDatasetValue<String>> getSurnames() {
        return surname;
    }

    public Optional<MatchingDatasetValue<LocalDate>> getDateOfBirth() {
        return dateOfBirth;
    }

    public Optional<Address> getAddress() {
        return address;
    }

    public String getLevelOfAssurance() {
        return levelOfAssurance;
    }

    public Optional<MatchingDatasetValue<String>> getMiddleNames() {
        return middleNames;
    }

    public Optional<MatchingDatasetValue<Gender>> getGender() {
        return gender;
    }

    public static IdpUserDto fromIdpUser(DatabaseIdpUser idpUser) {
        return new IdpUserDto(
                Optional.ofNullable(idpUser.getPersistentId()),
                idpUser.getUsername(),
                idpUser.getPassword(),
                getFirstValue(idpUser.getFirstnames()),
                getFirstValue(idpUser.getMiddleNames()),
                idpUser.getSurnames(),
                idpUser.getGender(),
                getFirstValue(idpUser.getDateOfBirths()),
                Optional.ofNullable(idpUser.getCurrentAddress()),
                idpUser.getLevelOfAssurance().toString()
        );
    }

    private static <T> Optional<MatchingDatasetValue<T>> getFirstValue(List<MatchingDatasetValue<T>> values) {
        if (values.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(values.get(0));
    }

    public void hashPassword() {
        if(!BCryptHelper.alreadyCrypted(this.password)) {
            this.password = BCrypt.hashpw(this.password, BCrypt.gensalt());
        }
    }
}
