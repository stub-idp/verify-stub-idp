package stubidp.shared.csrf;

import java.util.Optional;

public interface CSRFView {
    Optional<String> getCsrfToken();
}
