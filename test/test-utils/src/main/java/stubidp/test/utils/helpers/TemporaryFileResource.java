package stubidp.test.utils.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TemporaryFileResource implements ManagedFileResource {
    private final File tempFile;
    private final byte[] content;

    public File getTempFile() {
        return tempFile;
    }

    public String getPath() {
        return tempFile.getAbsolutePath();
    }

    public TemporaryFileResource(File tempFile, byte[] content) {
        this.tempFile = tempFile;
        this.content = content;
    }

    @Override
    public void create() {
        try {
            Files.write(tempFile.toPath(), content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete() {
      tempFile.delete();
    }
}
