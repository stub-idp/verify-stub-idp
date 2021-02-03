package uk.gov.ida.matchingserviceadapter.rest.matchingservice;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class UniversalAddressDto extends AddressDto {
    private LocalDate from;
    private Optional<LocalDate> to = Optional.empty();

    @SuppressWarnings("unused") // needed for JAXB
    private UniversalAddressDto() {
        super();
    }

    public UniversalAddressDto(
            List<String> lines,
            Optional<String> postCode,
            Optional<String> internationalPostCode,
            Optional<String> uprn,
            LocalDate from,
            Optional<LocalDate> to,
            boolean verified) {

        super(lines, postCode, internationalPostCode, uprn, verified);
        this.from = from;
        this.to = to;
    }

    public LocalDate getFrom() {
        return from;
    }

    public Optional<LocalDate> getTo() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UniversalAddressDto that = (UniversalAddressDto) o;
        return Objects.equals(from, that.from) && Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), from, to);
    }
}
