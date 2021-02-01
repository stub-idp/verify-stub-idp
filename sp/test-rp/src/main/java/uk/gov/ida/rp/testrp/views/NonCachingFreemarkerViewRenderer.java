package uk.gov.ida.rp.testrp.views;

import freemarker.cache.NullCacheStorage;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.dropwizard.views.View;
import org.glassfish.jersey.server.ContainerException;
import uk.gov.ida.shared.dropwizard.jade.JadeViewRenderer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class NonCachingFreemarkerViewRenderer extends JadeViewRenderer {

    @Override
    public boolean isRenderable(View view) {
        return view.getTemplateName().endsWith(".ftl");
    }

    @Override
    public void render(View view,
                       Locale locale,
                       OutputStream output) throws IOException {
        try {
            final Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
            configuration.setClassForTemplateLoading(view.getClass(), "/");
            configuration.setCacheStorage(new NullCacheStorage());
            configuration.setTemplateUpdateDelayMilliseconds(1000);
            configuration.setObjectWrapper(new DefaultObjectWrapper(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS));
            configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
            configuration.removeTemplateFromCache(view.getTemplateName(), locale);
            final Template template = configuration.getTemplate(view.getTemplateName(), locale);
            template.process(view, new OutputStreamWriter(output, template.getEncoding()));

        } catch (TemplateException e) {
            throw new ContainerException(e);
        }
    }
}
