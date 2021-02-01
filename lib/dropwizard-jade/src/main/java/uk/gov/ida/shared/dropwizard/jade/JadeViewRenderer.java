package uk.gov.ida.shared.dropwizard.jade;

import com.google.common.base.Charsets;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.exceptions.JadeException;
import de.neuland.jade4j.model.JadeModel;
import de.neuland.jade4j.template.JadeTemplate;
import io.dropwizard.views.View;
import io.dropwizard.views.ViewRenderer;
import org.glassfish.jersey.server.internal.process.MappableException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;

/**
 * A {@link io.dropwizard.views.ViewRenderer} which renders Jade ({@code .jade}) templates.
 */
public class JadeViewRenderer implements ViewRenderer {

    private final JadeConfiguration configuration;

    public JadeViewRenderer() {
        this.configuration = new JadeConfiguration();
        configuration.setTemplateLoader(new ResourceTemplateLoader());
    }

    @Override
    public boolean isRenderable(View view) {
        return view.getTemplateName().endsWith(".jade");
    }

    @Override
    public void render(
            final View view,
            final Locale locale,
            final OutputStream output) throws IOException {

        JadeModel model = new JadeModelFactory().createModel(view);
        final Charset charset = view.getCharset().orElse(Charsets.UTF_8);
        try {
            final JadeTemplate template = configuration.getTemplate(view.getTemplateName());
            try (OutputStreamWriter writer = new OutputStreamWriter(output, charset)) {
                configuration.renderTemplate(template, model, writer);
            }
        } catch (JadeException e) {
            throw new MappableException(e);
        }
    }

    @Override
    public void configure(Map<String, String> options) {

    }

    @Override
    public String getConfigurationKey() {
        return "jade";
    }
}
