package stubidp.saml.domain.assertions;

public class IpAddress {
    private final String ipAddressString;

    public IpAddress(String ipAddressString) {
        this.ipAddressString = ipAddressString;
    }

    public String getStringValue() {
        return this.ipAddressString;
    }
}
