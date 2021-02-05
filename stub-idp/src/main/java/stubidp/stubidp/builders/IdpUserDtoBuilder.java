package stubidp.stubidp.builders;

import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.Gender;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.stubidp.dtos.IdpUserDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IdpUserDtoBuilder {

    private Optional<String> pid = Optional.empty();
    private Optional<SimpleMdsValue<String>> firstName = Optional.empty();
    private Optional<SimpleMdsValue<String>> middleNames = Optional.empty();
    private final List<SimpleMdsValue<String>> surnames = new ArrayList<>();
    private Optional<SimpleMdsValue<Gender>> gender = Optional.empty();
    private Optional<SimpleMdsValue<LocalDate>> dateOfBirth = Optional.empty();
    private Optional<Address> address = Optional.empty();
    private String userName;
    private String password;
    private String levelOfAssurance;

    public static IdpUserDtoBuilder anIdpUserDto() {
        return new IdpUserDtoBuilder();
    }

    public IdpUserDto build() {
        return new IdpUserDto(
                pid,
                userName,
                password,
                firstName,
                middleNames,
                surnames,
                gender,
                dateOfBirth,
                address,
                levelOfAssurance
        );
    }

    public IdpUserDtoBuilder withPid(String pid) {
        this.pid = Optional.ofNullable(pid);
        return this;
    }

    public IdpUserDtoBuilder withUserName(final String userName) {
        this.userName = userName;
        return this;
    }

    public IdpUserDtoBuilder withPassword(final String password) {
        this.password = password;
        return this;
    }

    public IdpUserDtoBuilder withFirsName(final SimpleMdsValue<String> firstName) {
        this.firstName = Optional.ofNullable(firstName);
        return this;
    }

    public IdpUserDtoBuilder withMiddleNames(final SimpleMdsValue<String> middleNames) {
        this.middleNames = Optional.ofNullable(middleNames);
        return this;
    }

    public IdpUserDtoBuilder addSurname(final SimpleMdsValue<String> surname) {
        this.surnames.add(surname);
        return this;
    }

    public IdpUserDtoBuilder withGender(final SimpleMdsValue<Gender> gender) {
        this.gender = Optional.ofNullable(gender);
        return this;
    }

    public IdpUserDtoBuilder withDateOfBirth(final SimpleMdsValue<LocalDate> dateOfBirth) {
        this.dateOfBirth = Optional.ofNullable(dateOfBirth);
        return this;
    }

    public IdpUserDtoBuilder withAddress(final Address address) {
        this.address = Optional.ofNullable(address);
        return this;
    }

    public IdpUserDtoBuilder withLevelOfAssurance(final String levelOfAssurance) {
        this.levelOfAssurance = levelOfAssurance;
        return this;
    }
}
