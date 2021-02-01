package uk.gov.ida.rp.testrp.domain;

import java.util.Optional;

public class PageErrorMessageDetails {

    private final Optional<String> header;
    private final Optional<String> message;

    public PageErrorMessageDetails(Optional<String> header, Optional<String> message) {
        this.header = header;
        this.message = message;
    }

    public Optional<String> getHeader() {
        return header;
    }

    public Optional<String> getMessage() {
        return message;
    }
}
