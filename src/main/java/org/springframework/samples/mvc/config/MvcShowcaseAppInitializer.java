package org.springframework.samples.mvc.config;

import jakarta.servlet.Filter;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletRegistration.Dynamic;

import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * Initialize the Servlet container. This class is detected by the Servlet
 * container on startup.
 */
public class MvcShowcaseAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer{

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class[] { RootConfig.class };
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class[] { WebMvcConfig.class };
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "/" };
	}

	@Override
	protected Filter[] getServletFilters() {
		return new Filter[] { new DelegatingFilterProxy("csrfFilter") };
	}

	@Override
	protected void customizeRegistration(Dynamic registration) {
		// Configure multipart (max 2MB per file, 4MB total, no temp file threshold)
		registration.setMultipartConfig(new MultipartConfigElement("", 2097152L, 4194304L, 0));
	}

}
