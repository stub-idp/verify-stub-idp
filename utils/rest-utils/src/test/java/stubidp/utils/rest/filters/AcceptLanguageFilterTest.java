package stubidp.utils.rest.filters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AcceptLanguageFilterTest{

    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final String encodingHeader = "someheader";

    @BeforeEach
    void setUp(){
        ArrayList<String> headers = new ArrayList<>();
        headers.add(HttpHeaders.ACCEPT_LANGUAGE);
        headers.add(HttpHeaders.ACCEPT_ENCODING);
        when(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn("en-US,en;q=0.8,es-419;q=0.6,es;q=0.4");
        when(request.getHeader(HttpHeaders.ACCEPT_ENCODING)).thenReturn(encodingHeader);
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(headers));

    }

    @Test
    void getHeaderNames_removesAcceptLanguageHeader(){
        AcceptLanguageFilter f = new AcceptLanguageFilter();
        Enumeration<String> headerNames = new AcceptLanguageFilter.HideAcceptLanguage(request).getHeaderNames();
        verify(request).getHeaderNames();
        assertThat(headerNames.nextElement()).contains(HttpHeaders.ACCEPT_ENCODING);
    }

    @Test
    void getHeader_returnsValueOfAcceptLanguageHeaderNullForOthers(){
        AcceptLanguageFilter f = new AcceptLanguageFilter();
        assertThat(new AcceptLanguageFilter.HideAcceptLanguage(request).getHeader(HttpHeaders.ACCEPT_ENCODING)).isEqualTo(encodingHeader);
        assertThat(new AcceptLanguageFilter.HideAcceptLanguage(request).getHeader(HttpHeaders.ACCEPT_LANGUAGE)).isNull();
    }
}
