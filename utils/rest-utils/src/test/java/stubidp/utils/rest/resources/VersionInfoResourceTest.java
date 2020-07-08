package stubidp.utils.rest.resources;

import org.junit.jupiter.api.Test;
import stubidp.utils.rest.common.VersionInfoDto;
import stubidp.utils.rest.resources.VersionInfoResource;

public class VersionInfoResourceTest {

    @Test
    public void shouldLoadDataFromManifest() throws Exception {
        VersionInfoResource versionInfoResource = new VersionInfoResource();

        VersionInfoDto versionInfo = versionInfoResource.getVersionInfo();
        // note this should not throw an exception... but setting it up to test is hard :-(
    }

}
