package stubidp.utils.common;

import org.junit.jupiter.api.Test;
import stubidp.utils.common.IpAddressResolver;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class IpAddressResolverTest {

    @Test
    public void lookupIpAddress_shouldResolveHostnameToIpAddress() {
        URI someLocalhostUrl = URI.create("http://localhost/some/path");
        String ipAddress = new IpAddressResolver().lookupIpAddress(someLocalhostUrl);

        assertThat(ipAddress).isEqualTo("127.0.0.1");
    }

    @Test
    public void lookupIpAddress_shouldReturnUnableToResolveStringForUnknownHost() {
        URI someLocalhostUrl = URI.create("http://someunknownhost");
        String ipAddress = new IpAddressResolver().lookupIpAddress(someLocalhostUrl);

        assertThat(ipAddress).isEqualTo("[Unable to resolve IP Address]");
    }
}
