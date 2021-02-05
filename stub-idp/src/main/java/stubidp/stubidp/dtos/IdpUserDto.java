package stubidp.stubidp.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.mindrot.jbcrypt.BCrypt;
import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.Gender;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.stubidp.domain.DatabaseIdpUser;
import stubidp.stubidp.security.BCryptHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonSerialize
@JsonInclude(NON_NULL)
public class IdpUserDto {

    private Optional<String> pid = Optional.empty();
    private String username;
    private String password;
    private Optional<SimpleMdsValue<String>> firstName = Optional.empty();
    private Optional<SimpleMdsValue<String>> middleNames = Optional.empty();
    private List<SimpleMdsValue<String>> surname = new ArrayList<>();
    private Optional<SimpleMdsValue<Gender>> gender = Optional.empty();
    private Optional<SimpleMdsValue<LocalDate>> dateOfBirth = Optional.empty();
    private Optional<Address> address = Optional.empty();
    private String levelOfAssurance;

    @SuppressWarnings("unused")
    private IdpUserDto() {}

    public IdpUserDto(
            Optional<String> pid,
            String username,
            String password,
            Optional<SimpleMdsValue<String>> firstName,
            Optional<SimpleMdsValue<String>> middleNames,
            List<SimpleMdsValue<String>> surnames,
            Optional<SimpleMdsValue<Gender>> gender,
            Optional<SimpleMdsValue<LocalDate>> dateOfBirth,
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

    public Optional<SimpleMdsValue<String>> getFirstName() {
        return firstName;
    }

    public List<SimpleMdsValue<String>> getSurnames() {
        return surname;
    }

    public Optional<SimpleMdsValue<LocalDate>> getDateOfBirth() {
        return dateOfBirth;
    }

    public Optional<Address> getAddress() {
        return address;
    }

    public String getLevelOfAssurance() {
        return levelOfAssurance;
    }

    public Optional<SimpleMdsValue<String>> getMiddleNames() {
        return middleNames;
    }

    public Optional<SimpleMdsValue<Gender>> getGender() {
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

    private static <T> Optional<SimpleMdsValue<T>> getFirstValue(List<SimpleMdsValue<T>> values) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdpUserDto that = (IdpUserDto) o;
        return Objects.equals(pid, that.pid) && Objects.equals(username, that.username) && Objects.equals(password, that.password) && Objects.equals(firstName, that.firstName) && Objects.equals(middleNames, that.middleNames) && Objects.equals(surname, that.surname) && Objects.equals(gender, that.gender) && Objects.equals(dateOfBirth, that.dateOfBirth) && Objects.equals(address, that.address) && Objects.equals(levelOfAssurance, that.levelOfAssurance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pid, username, password, firstName, middleNames, surname, gender, dateOfBirth, address, levelOfAssurance);
    }
}
