package stubidp.utils.rest.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.utils.rest.common.CommonUrls;
import stubidp.utils.rest.common.VersionInfoDto;
import stubidp.utils.rest.configuration.ServiceNameConfiguration;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static java.text.MessageFormat.format;

// See http://stackoverflow.com/questions/1272648/reading-my-own-jars-manifest for implementation details.
@Path(CommonUrls.VERSION_INFO_ROOT)
@Produces(MediaType.APPLICATION_JSON)
public class VersionInfoResource {
    private static final Logger LOG = LoggerFactory.getLogger(VersionInfoResource.class);
    private static final String manifestFilePath = "/META-INF/MANIFEST.MF";

    private final Class<?> classInMainJar;

    public VersionInfoResource(Class<? extends ServiceNameConfiguration> classInMainJar) {
        this.classInMainJar = classInMainJar;
    }

    @GET
    public VersionInfoDto getVersionInfo() {
        Attributes manifest = getManifest();
        manifest.forEach((k, v) -> LOG.trace(format("{0}={1}", k, v)));
        String buildNumber = manifest.getValue("Build-Number");
        String gitCommit = manifest.getValue("Git-Commit");
        String buildTimestamp = manifest.getValue("Build-Timestamp");

        return new VersionInfoDto(buildNumber, gitCommit, buildTimestamp);
    }

    private Attributes getManifest() {
        try {
            LOG.trace(format("manifest: {0}, class: {1}", getManifestFilePath(), classInMainJar!=null?classInMainJar.getName():null));
            return new Manifest(classInMainJar.getResourceAsStream(getManifestFilePath()))
                    .getMainAttributes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getManifestFilePath() {
        return manifestFilePath;
    }
}
