package stubidp.utils.rest.analytics;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.glassfish.jersey.server.ContainerRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.utils.rest.configuration.AnalyticsConfiguration;
import stubidp.utils.rest.configuration.AnalyticsConfigurationBuilder;

import javax.ws.rs.core.Cookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static stubidp.utils.rest.analytics.AnalyticsReporter.PIWIK_VISITOR_ID;

@ExtendWith(MockitoExtension.class)
public class AnalyticsReporterTest {

    private static final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd HH:mm:ss")
            .appendInstant(0)
            .toFormatter()
            .withZone(ZoneId.of("UTC"));

    @Mock
    private ContainerRequest requestContext;

    @Mock
    private PiwikClient piwikClient;

    private final String visitorId = "123";

    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));

    @Test
    public void shouldCallGenerateUrlAndSendToPiwikAsynchronouslyWhenReportingCustomVariable() throws Exception {
        doReturn(Map.of(PIWIK_VISITOR_ID, new Cookie(PIWIK_VISITOR_ID, visitorId))).when(requestContext).getCookies();
        when(requestContext.getRequestUri()).thenReturn(URI.create("http://localhost"));

        AnalyticsReporter analyticsReporter = spy(new AnalyticsReporter(piwikClient, new AnalyticsConfigurationBuilder().build(), clock));
        CustomVariable customVariable = new CustomVariable(2, "IDP", "Experian");

        analyticsReporter.reportCustomVariable("friendly description of URL", requestContext, customVariable);

        URI expected = analyticsReporter.generateURI("friendly description of URL", requestContext, Optional.of(customVariable), Optional.of(visitorId));
        verify(piwikClient).report(expected, requestContext);
    }

    @Test
    public void shouldCallGenerateUrlAndSendToPiwkAsynchronously() throws URISyntaxException {
        doReturn(Map.of(PIWIK_VISITOR_ID, new Cookie(PIWIK_VISITOR_ID, visitorId))).when(requestContext).getCookies();

        String friendlyDescription = "friendly description of URL";
        URI piwikUri = URI.create("piwik");

        AnalyticsReporter analyticsReporter = spy(new AnalyticsReporter(piwikClient, new AnalyticsConfigurationBuilder().build(), clock));

        doReturn(piwikUri).when(analyticsReporter).generateURI(friendlyDescription, requestContext, Optional.empty(), Optional.of(visitorId));

        analyticsReporter.report(friendlyDescription, requestContext);

        verify(piwikClient).report(piwikUri, requestContext);
    }

    @Test
    public void shouldHandleAnyExceptions() throws URISyntaxException {
        doReturn(Map.of(PIWIK_VISITOR_ID, new Cookie(PIWIK_VISITOR_ID, visitorId))).when(requestContext).getCookies();

        String friendlyDescription = "friendly description of URL";

        AnalyticsReporter analyticsReporter = spy(new AnalyticsReporter(piwikClient, new AnalyticsConfigurationBuilder().build(), clock));

        doThrow(new RuntimeException("error")).when(analyticsReporter).generateURI(friendlyDescription, requestContext, Optional.empty(), Optional.of(visitorId));

        analyticsReporter.report(friendlyDescription, requestContext);
    }

    @Test
    public void shouldGeneratePiwikUrl() throws URISyntaxException {
        Instant now = Instant.now(clock);

        when(requestContext.getHeaderString("Referer")).thenReturn("http://piwikserver/referrerUrl");
        when(requestContext.getRequestUri()).thenReturn(new URI("http://piwikserver/requestUrl"));

        AnalyticsConfiguration analyticsConfiguration = new AnalyticsConfigurationBuilder().build();
        URIBuilder expectedURI = new URIBuilder(format("http://piwik-digds.rhcloud.local/analytics?idsite={0}&rec=1&apiv=1&url=http%3A%2F%2Fpiwikserver%2FrequestUrl&urlref=http%3A%2F%2Fpiwikserver%2FreferrerUrl&_id=abc&ref=http%3A%2F%2Fpiwikserver%2FreferrerUrl&cookie=false&action_name=SERVER+friendly+description+of+URL", analyticsConfiguration.getSiteId()));

        expectedURI.addParameter("cdt", dateTimeFormatter.format(now));

        AnalyticsReporter analyticsReporter = new AnalyticsReporter(piwikClient, analyticsConfiguration, clock);

        URIBuilder testURI = new URIBuilder(analyticsReporter.generateURI("SERVER friendly description of URL", requestContext, Optional.empty(), Optional.of("abc")));

        Map<String, NameValuePair> expectedParams = expectedURI.getQueryParams().stream()
                .collect(Collectors.toMap(e -> e.getName(), e -> e));

        for (NameValuePair param : testURI.getQueryParams()) {
            assertThat(expectedParams).containsEntry(param.getName(), param);
        }

        assertThat(testURI.getQueryParams().size()).isEqualTo(expectedParams.size());
    }

    @Test
    public void shouldGeneratePiwikCustomVariableUrl() throws URISyntaxException {
        doReturn(Map.of(PIWIK_VISITOR_ID, new Cookie(PIWIK_VISITOR_ID, visitorId))).when(requestContext).getCookies();
        when(requestContext.getRequestUri()).thenReturn(URI.create("http://localhost"));

        Instant now = Instant.now(clock);
        String customVariable = "{\"1\":[\"RP\",\"HMRC BLA\"]}";

        AnalyticsConfiguration analyticsConfiguration = new AnalyticsConfigurationBuilder().build();
        URIBuilder expectedURI = new URIBuilder(format("http://piwiki-dgds.rhcloud.local/analytics?_id=123&idsite={0}&rec=1&apiv=1&action_name=page-title&cookie=false", analyticsConfiguration.getSiteId()));
        expectedURI.addParameter("_cvar", customVariable);
        expectedURI.addParameter("url", requestContext.getRequestUri().toString());
        expectedURI.addParameter("cdt", dateTimeFormatter.format(now));
        AnalyticsReporter analyticsReporter = new AnalyticsReporter(piwikClient, analyticsConfiguration, clock);
        Optional<Cookie> piwikCookie = Optional.ofNullable(requestContext.getCookies().get(PIWIK_VISITOR_ID));
        Optional<String> visitorId = piwikCookie.map(Cookie::getValue);
        Optional<CustomVariable> customVariableOptional = Optional.of(new CustomVariable(1, "RP", "HMRC BLA"));
        URIBuilder testURI = new URIBuilder(analyticsReporter.generateURI("page-title", requestContext, customVariableOptional, visitorId));

        Map<String, NameValuePair> expectedParams = expectedURI.getQueryParams().stream()
                .collect(Collectors.toMap(e -> e.getName(), e -> e));

        for (NameValuePair param : testURI.getQueryParams()) {
            assertThat(expectedParams).containsEntry(param.getName(), param);
        }

        assertThat(testURI.getQueryParams().size()).isEqualTo(expectedParams.size());
    }

    @Test
    public void simulatePageView_generatesExpectedParameters() {
        doReturn(Map.of(PIWIK_VISITOR_ID, new Cookie(PIWIK_VISITOR_ID, visitorId))).when(requestContext).getCookies();

        AnalyticsConfiguration config = new AnalyticsConfigurationBuilder().build();
        AnalyticsReporter reporter = new AnalyticsReporter(piwikClient, config, clock);
        ArgumentCaptor<URI> captor = ArgumentCaptor.forClass(URI.class);

        reporter.reportPageView("Title", requestContext, "http://page-view");

        verify(piwikClient).report(captor.capture(), eq(requestContext));
        URIBuilder uriBuilder = new URIBuilder(captor.getValue());
        checkURIBase(uriBuilder.toString(), config.getPiwikServerSideUrl());
        checkURIURL(uriBuilder, "http://page-view");
        checkURITitle(uriBuilder, "Title");
        checkParams(uriBuilder, config.getSiteId().toString());
    }

    @Test
    public void simulatePageView_includesVisitorIdIfPresent() {
        doReturn(Map.of(PIWIK_VISITOR_ID, new Cookie(PIWIK_VISITOR_ID, visitorId))).when(requestContext).getCookies();

        AnalyticsReporter reporter = new AnalyticsReporter(piwikClient, new AnalyticsConfigurationBuilder().build(), clock);
        ArgumentCaptor<URI> captor = ArgumentCaptor.forClass(URI.class);

        reporter.reportPageView("Title", requestContext, "http://page-view");

        verify(piwikClient).report(captor.capture(), eq(requestContext));
        checkVisitorId(captor.getValue().getQuery(), visitorId);
    }

    @Test
    public void simulatePageView_handlesMissingVisitorId() {
        when(requestContext.getCookies()).thenReturn(Map.of());
        AnalyticsConfiguration config = new AnalyticsConfigurationBuilder().build();
        AnalyticsReporter reporter = new AnalyticsReporter(piwikClient, config, clock);
        ArgumentCaptor<URI> captor = ArgumentCaptor.forClass(URI.class);

        reporter.reportPageView("Title", requestContext, "http://page-view");

        verify(piwikClient).report(captor.capture(), eq(requestContext));
        String query = captor.getValue().getQuery();
        checkQueryParamMissing(query, "_id");
    }

    @Test
    public void simulatePageView_doesNotReportIfAnalyticsIsDisabled() {
        AnalyticsConfiguration config = new AnalyticsConfigurationBuilder().setEnabled(false).build();
        AnalyticsReporter reporter = new AnalyticsReporter(piwikClient, config, clock);

        reporter.reportPageView("Title", requestContext, "http://page-view");

        verify(piwikClient, never()).report(any(URI.class), any(ContainerRequest.class));
    }

    private void checkCommonParams(URIBuilder uriBuilder, String siteId) {
        checkQueryParam(uriBuilder, "idsite", siteId);
        checkQueryParam(uriBuilder, "apiv", "1");
        checkQueryParam(uriBuilder, "rec", "1");
    }

    private void checkVisitorId(String query, String visitorId) {
        checkQueryParam(query, "_id", visitorId);
    }

    private void checkURIURL(URIBuilder query, String expected) {
        checkQueryParam(query, "url", expected);
    }

    private void checkURIBase(String uri, String expected) {
        assertThat(uri.indexOf(expected)).isEqualTo(0);
    }

    private void checkURITitle(URIBuilder query, String title) {
        checkQueryParam(query, "action_name", title);
    }

    private void checkQueryParam(String query, String name, String expected) {
        assertThat(queryContains(query, name, expected)).as("Looking for param %s with value %s in query %s", name,
                expected, query).isTrue();
    }

    private void checkQueryParam(URIBuilder uriBuilder, String name, String expected) {
        assertThat(uriBuilder.getQueryParams().contains(new BasicNameValuePair(name, expected))).as("Looking for param %s with value %s", name, expected).isTrue();
    }

    private boolean queryContains(String query, String name, String expectedValue) {
        String match = name + "=" + expectedValue;
        return List.of(query.split("&")).stream().anyMatch(match::equals);
    }

    private void checkQueryParamMissing(String query, String name) {
        assertThat(query.contains(name + "=")).isFalse();
    }

    private void checkParams(URIBuilder uriBuilder, String siteId) {
        checkCommonParams(uriBuilder, siteId);
        checkQueryParam(uriBuilder, "cookie", "false");
        checkQueryParam(uriBuilder, "cdt", dateTimeFormatter.format(Instant.now(clock)));
    }

}
