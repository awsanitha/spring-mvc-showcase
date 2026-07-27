package org.springframework.samples.mvc.redirect;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.text.ParseException;
import java.util.Locale;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.format.Formatter;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.test.web.servlet.MockMvc;

public class RedirectControllerTests {

	private MockMvc mockMvc;

	@Before
	public void setup() throws Exception {
		DefaultFormattingConversionService cs = new DefaultFormattingConversionService();
		cs.addFormatter(new Formatter<LocalDate>() {
			@Override
			public LocalDate parse(String text, Locale locale) throws ParseException {
				return LocalDate.parse(text, DateTimeFormat.shortDate().withLocale(locale));
			}

			@Override
			public String print(LocalDate object, Locale locale) {
				return DateTimeFormat.shortDate().withLocale(locale).print(object);
			}
		});
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
