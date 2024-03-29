package com.fc.pass.util;

import com.vladmihalcea.hibernate.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeUtils {
    public static final DateTimeFormatter YYYY_MM_DD_HH_MM = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static String format(final LocalDateTime localDateTime) {
        return localDateTime.format(YYYY_MM_DD_HH_MM);
    }

    public static String format(final LocalDateTime localDateTime, DateTimeFormatter formatter) {
        return localDateTime.format(formatter);
    }
    public static LocalDateTime parse(final String localDateTimeString) {
        if (StringUtils.isBlank(localDateTimeString)) {
            return null;
        }
        return LocalDateTime.parse(localDateTimeString, YYYY_MM_DD_HH_MM);
    }
}
