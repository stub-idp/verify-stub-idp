package stubidp.utils.security.security;

import java.io.InputStream;

public interface PublicKeyInputStreamFactory {
    InputStream createInputStream(String publicKeyUri);
}
