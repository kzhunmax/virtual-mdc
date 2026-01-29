package com.github.kzhunmax.virtualmdc.wrapper;

import com.github.kzhunmax.virtualmdc.VirtualMDC;
import jakarta.servlet.*;

import java.io.IOException;
import java.util.Map;

/**
 * Servlet filter for automatic MDC propagation in web applications.
 * <p>
 *     Captures MDC at request start, generates requestId if missing, restores/clears at end.
 *     Use in web.xml or @WebFilter. Ideal for Spring Boot / Jakarta EE apps with virtual threads.
 * </p>
 */
public class MdcPropagatingServletFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        // Capture current MDC snapshot
        Map<String, String> previous = VirtualMDC.getCopyOfContextMap();

        try {
            chain.doFilter(request, response);
        } finally {
            // Restore previous MDC and clear for virtual threads
            VirtualMDC.setContextMap(previous);
            if (Thread.currentThread().isVirtual()) {
                VirtualMDC.clearThreadLocal();
            }
        }
    }

}
