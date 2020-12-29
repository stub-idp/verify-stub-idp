package stubidp.test.utils.helpers;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;

public class TemporaryFile implements BeforeEachCallback, AfterEachCallback {
    private final TemporaryFileResource temporaryFileResource;

    public TemporaryFile(TemporaryFileResource temporaryFileResource) {

        this.temporaryFileResource = temporaryFileResource;
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        temporaryFileResource.create();

    }

    @Override
    public void afterEach(ExtensionContext context) {
        temporaryFileResource.delete();
    }

    public String getPath() {
        return temporaryFileResource.getPath();
    }

    public File getTempFile() {
        return temporaryFileResource.getTempFile();
    }
}
