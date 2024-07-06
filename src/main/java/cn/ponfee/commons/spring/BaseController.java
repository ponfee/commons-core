/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.spring;

import cn.ponfee.commons.date.JavaUtilDateFormat;
import cn.ponfee.commons.date.LocalDateTimeFormat;
import cn.ponfee.commons.model.TypedKeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.beans.PropertyEditorSupport;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Spring web base controller
 *
 * @author Ponfee
 */
public abstract class BaseController implements TypedKeyValue<String, String> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                try {
                    super.setValue(JavaUtilDateFormat.DEFAULT.parse(text));
                } catch (ParseException e) {
                    throw new IllegalArgumentException("Invalid date format: " + text);
                }
            }
        });
        binder.registerCustomEditor(LocalDateTime.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                super.setValue(LocalDateTimeFormat.DEFAULT.parse(text));
            }
        });
        binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                LocalDateTime dateTime = LocalDateTimeFormat.DEFAULT.parse(text);
                super.setValue(dateTime == null ? null : dateTime.toLocalDate());
            }
        });
        binder.registerCustomEditor(LocalTime.class, new PropertyEditorSupport() {
            private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            @Override
            public void setAsText(String text) {
                super.setValue(LocalTime.parse(text, timeFormatter));
            }
        });
    }

    public static ServletRequestAttributes getRequestAttributes() {
        return (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    }

    public static HttpServletRequest getRequest() {
        return getRequestAttributes().getRequest();
    }

    public static HttpServletResponse getResponse() {
        return getRequestAttributes().getResponse();
    }

    public static HttpSession getSession() {
        return getRequest().getSession();
    }

    @Override
    public String getValue(String key) {
        return getRequest().getParameter(key);
    }

}
