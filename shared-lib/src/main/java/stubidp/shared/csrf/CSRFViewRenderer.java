package stubidp.shared.csrf;

import freemarker.template.Configuration;
import io.dropwizard.views.View;
import io.dropwizard.views.freemarker.FreemarkerViewRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import stubidp.shared.csrf.exceptions.CSRFConflictingFormAttributeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

/**
 * Adds a csrf protection value to all forms in the output html, if one is present in IdpPageView
 */
public class CSRFViewRenderer extends FreemarkerViewRenderer {

    public CSRFViewRenderer() {
        super(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
    }

    @Override
    public void render(View view, Locale locale, OutputStream output) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        super.render(view, locale, byteArrayOutputStream);
        byteArrayOutputStream.close();

        if(view instanceof CSRFView && ((CSRFView)view).getCsrfToken().isPresent()) {
            org.jsoup.nodes.Document document = Jsoup.parse(byteArrayOutputStream.toString());
            final Elements nodeList = document.getElementsByTag("form");
            for (final Element item : nodeList) {
                if (item.children().stream().anyMatch(it -> (it.tag().getName().equals("input") && it.hasAttr("name") && it.attr("name").equals(AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY)))) {
                    throw new CSRFConflictingFormAttributeException();
                } else {
                    item.appendChild(new Element(Tag.valueOf("input"), "", new Attributes() {{
                        put("name", AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY);
                        put("id", AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY);
                        put("value", ((CSRFView) view).getCsrfToken().get());
                        put("type", "hidden");
                    }}));
                }
            }
            output.write(document.html().getBytes());
        } else {
            output.write(byteArrayOutputStream.toByteArray());
        }
    }
}
