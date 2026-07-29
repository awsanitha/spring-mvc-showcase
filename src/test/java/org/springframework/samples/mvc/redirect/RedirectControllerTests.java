package org.springframework.samples.mvc.redirect;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.joda.time.LocalDate;
import org.joda.time.format.ISODateTimeFormat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.test.web.servlet.MockMvc;

public class RedirectControllerTests {

	private MockMvc mockMvc;

	@Before
	public void setup() throws Exception {
		DefaultFormattingConversionService cs = new DefaultFormattingConversionService();
		cs.addConverter(new Converter<LocalDate, String>() {
			@Override
			public String convert(LocalDate source) {
				return ISODateTimeFormat.date().print(source);
			}
		});
		cs.addConverter(new Converter<String, LocalDate>() {
			@Override
			public LocalDate convert(String source) {
				return LocalDate.parse(source, ISODateTimeFormat.date());
			}
		});
		this.mockMvc = standaloneSetup(new RedirectController(cs))
				.setConversionService(cs)
				.alwaysExpect(status().isFound()).build();
	}

	@Test
	public void uriTemplate() throws Exception {
		this.mockMvc.perform(get("/redirect/uriTemplate"))
				.andExpect(redirectedUrl("/redirect/a123?date=2011-12-31"));
	}

	@Test
	public void uriComponentsBuilder() throws Exception {
		this.mockMvc.perform(get("/redirect/uriComponentsBuilder"))
				.andExpect(redirectedUrl("/redirect/a123?date=2011-12-31"));
	}

}
