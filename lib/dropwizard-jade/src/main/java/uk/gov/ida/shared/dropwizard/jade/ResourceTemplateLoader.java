package uk.gov.ida.shared.dropwizard.jade;

import de.neuland.jade4j.template.TemplateLoader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import static java.text.MessageFormat.format;

public class ResourceTemplateLoader implements TemplateLoader {

    private static final String JADE_SUFFIX = ".jade";

    @Override
    public String getExtension() {
        return JADE_SUFFIX;
    }

    @Override
    public long getLastModified(String name) {
        return -1;
    }

    @Override
    public Reader getReader(String name) throws IOException {
        if (!name.endsWith(JADE_SUFFIX)) {
            name = name + JADE_SUFFIX;
        }
        final InputStream resourceAsStream = getClass().getResourceAsStream(name);
        if (resourceAsStream == null) {
            throw new FileNotFoundException(format("Template {0} not found.", name));
        }
        return new InputStreamReader(resourceAsStream);
    }
}