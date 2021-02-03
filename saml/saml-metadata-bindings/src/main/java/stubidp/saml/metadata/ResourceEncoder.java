package stubidp.saml.metadata;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;

public class ResourceEncoder {

    private ResourceEncoder() {}

    public static String entityIdAsResource(String entityId) {
        return DatatypeConverter.printHexBinary(entityId.getBytes(StandardCharsets.UTF_8)).toLowerCase();
    }
}
