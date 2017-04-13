/*******************************************************************************
 * Copyright (c) 2011, 2017 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *     Red Hat Inc
 *******************************************************************************/
package org.eclipse.kapua.commons.util;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Date utilities
 *
 * @since 1.0
 *
 */
public class KapuaDateUtils {

    private static final String DEFAULT_DATE_PATTERN = "MM/dd/yyyy h:mm a"; // example 24/01/2017 11:22 AM

    private static final DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern(DEFAULT_DATE_PATTERN, Locale.US)
            .withZone(ZoneOffset.UTC);

    /**
     * Get current date
     *
     * @return current date
     */
    public static Instant getKapuaSysDate() {
        return Instant.now();
    }
    
    public static ZoneId getTimeZone () {
        return ZoneOffset.UTC;
    }

    /**
     * Parse the provided String using the default pattern {@value #DEFAULT_DATE_PATTERN} and the default locale {@link #getKapuaLocale() getKapuaLocale}
     *
     * @param date
     * @return
     * @throws ParseException
     */
    public static Instant parseDate(String date) throws ParseException {
        return Instant.from(formatter.parse(date));
    }

    /**
     * Parse the provided String using the provided pattern and the default locale {@link #getKapuaLocale() getKapuaLocale}
     *
     * @param pattern
     * @param date
     * @return
     * @throws ParseException
     */
    public static Date parseDate(String pattern, String date) throws ParseException {
        DateFormat df = new SimpleDateFormat(pattern, getKapuaLocale());
        return df.parse(date);
    }

    /**
     * Format the provided Date using the provided pattern and the default locale {@link #getKapuaLocale() getKapuaLocale}
     *
     * @param pattern
     * @param date
     * @return
     * @throws ParseException
     */
    public static String formatDate(String pattern, Date date) throws ParseException {
        DateFormat df = new SimpleDateFormat(pattern, getKapuaLocale());
        return df.format(date);
    }

}
