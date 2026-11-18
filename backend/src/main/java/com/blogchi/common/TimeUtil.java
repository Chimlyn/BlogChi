package com.blogchi.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一把 LocalDateTime 格式化成字符串返回，前端拿到就能直接显示。
 */
public final class TimeUtil {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private TimeUtil() {
    }

    public static String format(LocalDateTime time) {
        return time == null ? null : FORMATTER.format(time);
    }
}
