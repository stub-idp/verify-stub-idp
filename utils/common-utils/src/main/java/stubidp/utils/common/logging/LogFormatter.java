package stubidp.utils.common.logging;


import java.text.MessageFormat;
import java.util.UUID;

final class LogFormatter {

    private LogFormatter() {
    }

    public static String formatLog(final UUID errorId, final String message) {
        return MessageFormat.format("UNEXPECTED_EXCEPTION – '{'id: {0}, message: {1}'}'", errorId, message);
    }
}
