package com.cefalo.cci.utils;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public abstract class DateUtils {
    public static String DEFAULT_TZ = "GMT";
    public static String DATE_FORMAT = "yyyy-MM-dd";

    private DateUtils() {

    }

    public static Date convertDateWithTZ(Date date, String tz) {
        if (date == null) {
            return null;
        }
        DateTime dt = new DateTime(date, DateTimeZone.forID(tz));
        return dt.toDate();
    }

    public static long convertTimeStampWithTZ(long timestamp) {
        DateTime dateTime = new DateTime(timestamp, DateTimeZone.forID(DEFAULT_TZ));
        return dateTime.getMillis();
    }

    public static Date convertDateWithTZ(Date date) {
        if (date == null) {
            return null;
        }
        return convertDateWithTZ(date, DEFAULT_TZ);
    }

    public static Date convertDateFormatTZ(String dateStr) throws IllegalArgumentException {
        return convertDateFormatTZ(dateStr, DATE_FORMAT);
    }

    public static Date convertDateFormatTZ(String dateStr, String format) throws IllegalArgumentException {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(format);
        DateTime dateTime = formatter.parseDateTime(dateStr);
        return convertDateWithTZ(dateTime.toDate());
    }

    public static Date convertDateStrToDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(DATE_FORMAT);
        DateTime dateTime = formatter.parseDateTime(dateStr);
        return dateTime.toDate();
    }

    public static String convertDateToDateStr(Date date) {
        date = convertDateWithTZ(date);
        DateTimeFormatter formatter = DateTimeFormat.forPattern(DATE_FORMAT);
        return formatter.print(date.getTime());
    }

    public static boolean isDateInDefaultDateFormat(String dateStr) {
        return dateStr.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    public static Date convertStringMetaDate(String createDate) {
        Date convertedDate = null;
        try {
            if (!StringUtils.isBlank(createDate)) {
                convertedDate = convertDateStrToDate(createDate);
            } else {
                convertedDate = convertDateWithTZ(new Date());
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Date metadata is in invalid format. Valid format is yyyy-MM-dd");
        }

        return convertedDate;
    }
}
