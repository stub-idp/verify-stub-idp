package stubidp.utils.rest.configuration;

public interface SSLContextConfiguration {

    TrustedSslServersConfiguration getTrustedSslServers();

    MutualAuthConfiguration getMutualAuth();
}
