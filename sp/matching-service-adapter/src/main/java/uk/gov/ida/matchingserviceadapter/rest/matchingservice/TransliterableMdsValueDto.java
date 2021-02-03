package uk.gov.ida.matchingserviceadapter.rest.matchingservice;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransliterableMdsValueDto extends SimpleMdsValueDto<String> {

    private String nonLatinScriptValue;

    @SuppressWarnings("unused") // needed for JAXB
    public TransliterableMdsValueDto() {

    }

    public TransliterableMdsValueDto(String value, String nonLatinScriptValue) {
        super(value, null, null, true);
        this.nonLatinScriptValue = nonLatinScriptValue;
    }

    public TransliterableMdsValueDto(String value, String nonLatinScriptValue, LocalDate from, LocalDate to, boolean verified) {
        super(value, from, to, verified);
        this.nonLatinScriptValue = nonLatinScriptValue;
    }

    public String getNonLatinScriptValue() {
        return nonLatinScriptValue;
    }
}
