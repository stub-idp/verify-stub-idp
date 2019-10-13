package stubidp.shared.views;

import io.dropwizard.views.View;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import stubidp.shared.csrf.CSRFView;
import stubidp.shared.csrf.CSRFViewRenderer;
import stubidp.shared.csrf.exceptions.CSRFConflictingFormAttributeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.shared.csrf.AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY;

public class CSRFViewRendererTest {

    private static final String CSRF_TOKEN = UUID.randomUUID().toString();

    private static class TestView extends View implements CSRFView {
        protected TestView(String templateName) {
            super(templateName);
        }
        @Override
        public Optional<String> getCsrfToken() {
            return Optional.ofNullable(CSRF_TOKEN);
        }
    }

    private static class TestNoCsrfView extends View implements CSRFView {
        protected TestNoCsrfView(String templateName) {
            super(templateName);
        }
        @Override
        public Optional<String> getCsrfToken() {
            return Optional.empty();
        }
    }

    private CSRFViewRenderer csrfViewRenderer = new CSRFViewRenderer();

    @Test
    public void shouldIgnoreAnyContentThatHasNoForm() throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        csrfViewRenderer.render(new TestView("testview.ftl"), Locale.ENGLISH, byteArrayOutputStream);
        final String view = new String(byteArrayOutputStream.toByteArray());
        assertThat(view).contains("hellotestview");
        assertThat(view).doesNotContain(CSRF_PROTECT_FORM_KEY);
    }

    @Test
    public void shouldAddValueToAllForms() throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        csrfViewRenderer.render(new TestView("testview_withform.ftl"), Locale.ENGLISH, byteArrayOutputStream);
        final String view = new String(byteArrayOutputStream.toByteArray());
        final Document document = Jsoup.parse(view);
        final Elements forms = document.getElementsByTag("form");
        assertThat(forms.size()).isEqualTo(2);
        forms.forEach(form -> assertThat(form.children().stream()
                .filter(e -> e.attr("name").equals(CSRF_PROTECT_FORM_KEY)
                        && e.attr("value").equals(CSRF_TOKEN))
                .count())
                .isEqualTo(1));
    }

    @Test
    public void shouldNotOverwriteExistingValues() throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Assertions.assertThrows(CSRFConflictingFormAttributeException.class, () -> csrfViewRenderer.render(new TestView("testview_withconflictingform.ftl"), Locale.ENGLISH, byteArrayOutputStream));
    }

    @Test
    public void shouldNotAddValueToAllFormsWhenNotSet() throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        csrfViewRenderer.render(new TestNoCsrfView("testview_withform.ftl"), Locale.ENGLISH, byteArrayOutputStream);
        final String view = new String(byteArrayOutputStream.toByteArray());
        final Document document = Jsoup.parse(view);
        final Elements forms = document.getElementsByTag("form");
        assertThat(forms.size()).isEqualTo(2);
        forms.forEach(form -> assertThat(form.children().stream()
                .filter(e -> e.attr("name").equals(CSRF_PROTECT_FORM_KEY)
                        && e.attr("value").equals(CSRF_TOKEN))
                .count())
                .isEqualTo(0));
    }

}
