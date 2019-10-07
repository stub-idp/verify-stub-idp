package stubsp.stubsp.views;

import io.dropwizard.views.View;

import static java.nio.charset.StandardCharsets.UTF_8;

public class StubSpView extends View {
    private final String pageTitle;
    private final String subTemplateName;

    protected StubSpView(String pageTitle, String subTemplateName) {
        super("stubspView.ftl", UTF_8);
        this.pageTitle = pageTitle;
        this.subTemplateName = subTemplateName;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public String getSubTemplateName() {
        return subTemplateName;
    }
}
