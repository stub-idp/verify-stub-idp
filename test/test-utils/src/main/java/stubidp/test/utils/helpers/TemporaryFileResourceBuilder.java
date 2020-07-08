package stubidp.test.utils.helpers;

import java.io.File;
import java.io.IOException;

public class TemporaryFileResourceBuilder {
    private byte[] content;
    private File file;

    private TemporaryFileResourceBuilder() {
    }

    public static TemporaryFileResourceBuilder aTemporaryFileResource() {
        return new TemporaryFileResourceBuilder();
    }

    public TemporaryFileResourceBuilder file(File file) {
        this.file = file;
        return this;
    }

    public TemporaryFileResourceBuilder content(byte[] content) {
        this.content = content;
        return this;
    }

    public TemporaryFileResourceBuilder content(String content) {
        this.content = content.getBytes();
        return this;
    }

    public TemporaryFileResource build() {
        if(file == null) {
            try {
                file = File.createTempFile("test-file", null, null);
                file.deleteOnExit();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return new TemporaryFileResource(file, content);
    }
}
