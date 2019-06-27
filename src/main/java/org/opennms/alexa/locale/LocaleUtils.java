/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.alexa.locale;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.function.Function;

import org.opennms.core.utils.FuzzyDateFormatter;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;

public class LocaleUtils {

    public class LocaleManager {
        private final Map<String, String> mapForLocale;
        private final String locale;

        private LocaleManager(final String locale, final Map<String, String> mapForLocale) {
            this.locale = locale;
            this.mapForLocale = mapForLocale;
        }

        public String getLocale() {
            return locale;
        }

        public String text(final String id) {
            return mapForLocale.get(id);
        }

        public String replace(final String id, final Function<String, String> function) {
            return function.apply(this.mapForLocale.get(id));
        }
    }

    private static final LocaleUtils localeUtils = new LocaleUtils();
    private final Map<String, Map<String, String>> allLocaleProperties = new TreeMap<>();
    public static final String DEFAULT = "en-US";

    private LocaleUtils() {
        loadLocale("en-US");
        loadLocale("de-DE");
    }

    private void loadLocale(final String locale) {
        try {
            final Properties localeProperties = new Properties();

            localeProperties.load(this.getClass().getClassLoader().getResourceAsStream(String.format("/%s.properties", locale)));
            allLocaleProperties.put(locale, new TreeMap<>());

            for(final String propertyName : localeProperties.stringPropertyNames()) {
                allLocaleProperties.get(locale).put(propertyName, localeProperties.getProperty(propertyName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private LocaleManager newLocaleManager(final String locale) {
        return new LocaleManager(locale, allLocaleProperties.get(locale));
    }

    public static LocaleManager createLocaleManager(final String locale) {
        return localeUtils.newLocaleManager(locale);
    }

    public static LocaleManager createLocaleManager(final HandlerInput handlerInput) {
        return localeUtils.newLocaleManager(handlerInput.getRequestEnvelope().getRequest().getLocale());
    }

    public static String time(final String locale, final String time) {
        if (locale.equals(DEFAULT)) {
            return time;
        }

        final LocaleUtils.LocaleManager localeManager = LocaleUtils.createLocaleManager(locale);
        return time.replaceAll("hours", localeManager.text("HOURS"))
                .replaceAll("hour", localeManager.text("HOUR"))
                .replaceAll("minutes", localeManager.text("MINUTES"))
                .replaceAll("minute", localeManager.text("MINUTE"))
                .replaceAll("seconds", localeManager.text("SECONDS"))
                .replaceAll("second", localeManager.text("SECOND"))
                .replaceAll("years", localeManager.text("YEARS"))
                .replaceAll("year", localeManager.text("YEAR"))
                .replaceAll("months", localeManager.text("MONTHS"))
                .replaceAll("month", localeManager.text("MONTH"))
                .replaceAll("days", localeManager.text("DAYS"))
                .replaceAll("day", localeManager.text("DAY"));
    }

    public static String time(final String locale, final Date date) {
        return time(locale, FuzzyDateFormatter.calculateDifference(date, new Date()));
    }

    public static String severity(final String locale, final String severity) {
        if (locale.equals(DEFAULT)) {
            return severity;
        }
        return LocaleUtils.createLocaleManager(locale).text(severity.toUpperCase());
    }
}
