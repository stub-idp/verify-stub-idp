package stubidp.utils.common.manifest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ManifestReaderTest {

    private final ManifestReader manifestReader = new ManifestReader();

    @Test
    public void shouldReadAttributeValueForAClassFromAJarFile() throws IOException {
        String implementationVersion = manifestReader.getAttributeValueFor(Test.class, "Implementation-Version");

        assertThat(implementationVersion).isNotEmpty();
    }

    @Test
    public void shouldThrowExceptionWhenManifestFileDoesNotExist() {
        final IOException exception = Assertions.assertThrows(IOException.class, () -> manifestReader.getAttributeValueFor(manifestReader.getClass(), "any-attribute-name"));
        assertThat(exception.getMessage()).isEqualTo("Manifest file not found for the given class.");
    }

    @Test
    public void shouldThrowExceptionWhenAttributeDoesNotExist() {
        final IOException exception = Assertions.assertThrows(IOException.class, () -> manifestReader.getAttributeValueFor(Test.class, "some-unknown-attribute"));
        assertThat(exception.getMessage()).isEqualTo("Unknown attribute name");
    }
}