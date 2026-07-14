package com.timegrid.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import static org.assertj.core.api.Assertions.assertThat;

class RequestOriginTest {

    private final RequestOrigin requestOrigin = new RequestOrigin();

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void ignoresForwardedHeaderSentByClient() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.20");
        request.addHeader("X-Forwarded-For", "10.0.0.7");
        request.addHeader("User-Agent", "Mozilla/5.0");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertThat(requestOrigin.ipAddress()).isEqualTo("192.168.1.20");
        assertThat(requestOrigin.userAgent()).isEqualTo("Mozilla/5.0");
    }

    @Test
    void returnsNullOutsideRequest() {
        assertThat(requestOrigin.ipAddress()).isNull();
        assertThat(requestOrigin.userAgent()).isNull();
    }

}