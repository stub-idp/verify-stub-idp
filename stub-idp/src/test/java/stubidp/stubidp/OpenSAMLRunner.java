package stubidp.stubidp;

import stubidp.saml.extensions.IdaSamlBootstrap;

public abstract class OpenSAMLRunner {

    private static boolean initialized = false;

    static {
        if (!initialized) {
            initialized = true;
            try {
                IdaSamlBootstrap.bootstrap();
            } catch (IdaSamlBootstrap.BootstrapException e) {
                throw new RuntimeException(e);
            }
        }
    }
}