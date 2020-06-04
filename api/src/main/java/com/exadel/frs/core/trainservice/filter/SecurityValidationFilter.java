package com.exadel.frs.core.trainservice.filter;

import static com.exadel.frs.core.trainservice.enums.ValidationResult.OK;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;
import com.exadel.frs.core.trainservice.exception.ModelNotFoundException;
import com.exadel.frs.core.trainservice.exception.BadFormatModelKeyException;
import com.exadel.frs.core.trainservice.handler.ResponseExceptionHandler;
import com.exadel.frs.core.trainservice.service.ModelServicePg;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Filter created to validate if this application has access to requested model
 */

@Component
@RequiredArgsConstructor
@Order(1)
public class SecurityValidationFilter implements Filter {

    private final ModelServicePg modelService;
    private final ResponseExceptionHandler handler;
    private final ObjectMapper objectMapper;

    @Override
    public void init(final FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(
            final ServletRequest servletRequest,
            final ServletResponse servletResponse,
            final FilterChain filterChain
    ) throws IOException, ServletException {
        val httpRequest = (HttpServletRequest) servletRequest;
        val httpResponse = (HttpServletResponse) servletResponse;

        Map<String, List<String>> headersMap = Collections
                .list(httpRequest.getHeaderNames())
                .stream()
                .collect(toMap(
                        Function.identity(),
                        h -> Collections.list(httpRequest.getHeaders(h))
                ));

        var apiKey = headersMap.getOrDefault(X_FRS_API_KEY_HEADER, emptyList());

        val key = apiKey.get(0);
        try {
            UUID.fromString(key);
        } catch (Exception e) {
            val objectResponseEntity = handler.handleBadFormatModelKeyException(new BadFormatModelKeyException());
            buildException(httpResponse, objectResponseEntity);

            return;
        }
        val validationResult = modelService.validateModelKey(key);
        if (validationResult != OK) {
            val objectResponseEntity = handler.handleNotFoundException(new ModelNotFoundException());
            buildException(httpResponse, objectResponseEntity);

            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }

    @SneakyThrows
    private void buildException(final HttpServletResponse response, final ResponseEntity<?> responseEntity) {
        response.setStatus(responseEntity.getStatusCode().value());
        response.getWriter().append(objectMapper.writeValueAsString(responseEntity.getBody()));
        response.getWriter().flush();
    }
}