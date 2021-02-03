package uk.gov.ida.matchingserviceadapter.rest.matchingservice;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class VerifyAddressDto extends AddressDto {
    private LocalDate fromDate;
    private Optional<LocalDate> toDate = Optional.empty();

    @SuppressWarnings("unused") // needed for JAXB
    private VerifyAddressDto() {
        super();
    }

    public VerifyAddressDto(
            List<String> lines,
            Optional<String> postCode,
            Optional<String> internationalPostCode,
            Optional<String> uprn,
            LocalDate fromDate,
            Optional<LocalDate> toDate,
            boolean verified) {

        super(lines, postCode, internationalPostCode, uprn, verified);
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public Optional<LocalDate> getToDate() {
        return toDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        VerifyAddressDto that = (VerifyAddressDto) o;
        return Objects.equals(fromDate, that.fromDate) && Objects.equals(toDate, that.toDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fromDate, toDate);
    }
}
