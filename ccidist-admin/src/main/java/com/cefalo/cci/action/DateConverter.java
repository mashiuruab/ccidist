package com.cefalo.cci.action;

import com.cefalo.cci.utils.StringUtils;
import org.apache.struts2.util.StrutsTypeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class DateConverter extends StrutsTypeConverter {
    @Override
    public Object convertFromString(@SuppressWarnings("rawtypes") Map context, String[] values,
            @SuppressWarnings("rawtypes") Class toClass) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            if (!StringUtils.isBlank(values[0])) {
                date = new Date(sdf.parse(values[0]).getTime());
            }
        } catch (ParseException e) {
            return values[0];
        }
        return date;
    }

    @Override
    public String convertToString(@SuppressWarnings("rawtypes") Map context, Object o) {
        Date d = (Date) o;
        if (d != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(d);
        }
        return null;
    }
}
