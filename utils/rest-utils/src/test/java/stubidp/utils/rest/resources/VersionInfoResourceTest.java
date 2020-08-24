package stubidp.utils.rest.resources;

import org.junit.jupiter.api.Test;
import stubidp.utils.rest.common.VersionInfoDto;
import stubidp.utils.rest.configuration.ServiceNameConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionInfoResourceTest {

    private static class TestVersionInfoResource extends VersionInfoResource {
        public TestVersionInfoResource() {
            super(ServiceNameConfiguration.class);
        }

        @Override
        protected String getManifestFilePath() {
             return "/TESTMANIFEST.MF";
        }
    }

    @Test
    public void shouldLoadDataFromManifest() throws Exception {
        VersionInfoResource versionInfoResource = new TestVersionInfoResource();
        VersionInfoDto versionInfo = versionInfoResource.getVersionInfo();
        assertThat(versionInfo.getBuildNumber()).isEqualTo("1");
        assertThat(versionInfo.getGitCommit()).isEqualTo("f00");
        assertThat(versionInfo.getCreatedDate()).isEqualTo("2020-08-24T20:04:45+00:00");
    }
}