package org.springframework.samples.mvc.redirect;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.springframework.format.Printer;
import org.springframework.format.Parser;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.test.web.servlet.MockMvc;

public class RedirectControllerTests {

	private MockMvc mockMvc;

	@Before
	public void setup() throws Exception {
		DefaultFormattingConversionService cs = new DefaultFormattingConversionService();
		org.joda.time.format.DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern("MM/dd/yy");
		Printer<LocalDate> printer = (object, locale) -> jodaFormatter.print(object);
		Parser<LocalDate> parser = (text, locale) -> LocalDate.parse(text, jodaFormatter);
		cs.addFormatterForFieldType(LocalDate.class, printer, parser);
		this.mockMvc = standaloneSetup(new RedirectController(cs))
				.setConversionService(cs)
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
