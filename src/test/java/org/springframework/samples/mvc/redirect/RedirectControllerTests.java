package org.springframework.samples.mvc.redirect;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.junit.Before;
import org.junit.Test;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.samples.mvc.convert.JodaLocalDateFormatter;
import org.springframework.test.web.servlet.MockMvc;

public class RedirectControllerTests {

	private MockMvc mockMvc;

	@Before
	public void setup() throws Exception {
		// Spring no longer auto-registers the Joda-Time formatters
		DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
		conversionService.addFormatter(new JodaLocalDateFormatter());
		this.mockMvc = standaloneSetup(new RedirectController(conversionService))
				.setConversionService(conversionService)
				.alwaysExpect(status().isFound()).build();
	}

	@Test
	public void uriTemplate() throws Exception {
		this.mockMvc.perform(get("/redirect/uriTemplate"))
				.andExpect(redirectedUrl("/redirect/a123?date=12%2F31%2F11"));
	}

	@Test
	public void uriComponentsBuilder() throws Exception {
		this.mockMvc.perform(get("/redirect/uriComponentsBuilder"))
				.andExpect(redirectedUrl("/redirect/a123?date=12/31/11"));
	}

}
