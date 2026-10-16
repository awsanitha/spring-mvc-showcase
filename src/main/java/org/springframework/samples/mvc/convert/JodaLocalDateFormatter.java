package org.springframework.samples.mvc.convert;

import java.text.ParseException;
import java.util.Locale;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import org.springframework.format.Formatter;

/**
 * Formats {@link LocalDate} using the short date style, replacing the Joda-Time
 * formatter support that Spring Framework no longer registers automatically.
 */
public class JodaLocalDateFormatter implements Formatter<LocalDate> {

	@Override
	public LocalDate parse(String text, Locale locale) throws ParseException {
		return DateTimeFormat.shortDate().withLocale(locale).parseLocalDate(text);
	}

	@Override
	public String print(LocalDate object, Locale locale) {
		return DateTimeFormat.shortDate().withLocale(locale).print(object);
	}

}
