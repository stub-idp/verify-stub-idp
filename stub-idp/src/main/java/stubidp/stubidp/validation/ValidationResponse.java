package stubidp.stubidp.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ValidationResponse {
    private final boolean isOk;
    private final List<String> messages;

    public static ValidationResponse aValidResponse() {
        return new ValidationResponse(true, new ArrayList<>());
    }

    public static ValidationResponse anInvalidResponse(List<String> messages) {
        return new ValidationResponse(false, messages);
    }

    private ValidationResponse(boolean ok, List<String> messages) {
        this.isOk = ok;
        this.messages = messages;
    }

    public boolean isOk() {
        return isOk;
    }

    public List<String> getMessages() {
        return messages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationResponse that = (ValidationResponse) o;
        return isOk == that.isOk && Objects.equals(messages, that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isOk, messages);
    }
}
