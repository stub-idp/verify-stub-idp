package stubidp.eidas.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileReader {
    public static String readFileContent(File file) throws IOException {
        return readFileContent(file.toPath());
    }

    public static String readFileContent(String filePath) throws IOException {
        return readFileContent(Paths.get(filePath));
    }

    public static String readFileContent(Path path) throws IOException {
        return Files.readString(path);
    }
}
