package acceptance.uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ComplianceToolResponseDto {

    private final Status status;

    @JsonCreator
    public ComplianceToolResponseDto(@JsonProperty("status") Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public static final class Status {
        private final String message;
        private final String status;

        @JsonCreator
        public Status(@JsonProperty("message") String message,
                      @JsonProperty("status") String status) {
            this.message = message;
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public String getStatus() {
            return status;
        }
    }
}
