package cn.xuanyuanli.core.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日期工具类
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Dates {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(Dates.class);
    /**
     * utc p8
     */
    private static final ZoneOffset UTC_P8 = ZoneOffset.of("+8");
    /**
     * 默认模式
     */
    private static final String[] DEFAULT_PATTERNS = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH", "yyyy-MM-dd"};

    /**
     * 格式化时间
     *
     * @param date    待格式化的时间
     * @param pattern 格式化规则
     * @return {@link String}
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return "";
        }

        String thisPattern = DEFAULT_PATTERNS[0];
        if (StringUtils.isNotBlank(pattern)) {
            thisPattern = pattern;
        }
        return DateFormatUtils.format(date, thisPattern);
    }

    /**
     * 格式化时间
     *
     * @param date    待格式化的时间
     * @param pattern 格式化规则
     * @param zone    时区
     * @return {@link String}
     */
    public static String formatDate(Date date, String pattern, TimeZone zone) {
        if (date == null) {
            return "";
        }
        String thisPattern = DEFAULT_PATTERNS[0];
        if (StringUtils.isNotBlank(pattern)) {
            thisPattern = pattern;
        }
        return DateFormatUtils.format(date, thisPattern, zone);
    }

    /**
     * 从秒值或毫秒值获得Date对象
     *
     * @param time epoch的秒值或毫秒值
     * @return {@link Date}
     */
    public static Date getDateFromMillis(Long time) {
        if (!validTime(time)) {
            return null;
        }
        return new Date(getMillis(time));
    }

    /**
     * 验证时间戳是否符合规则
     *
     * @param time epoch的秒值或毫秒值
     * @return boolean
     */
    private static boolean validTime(Long time) {
        if (time == null) {
            return false;
        }
        int len = time.toString().length();
        return len == 13 || len == 12 || len == 10 || len == 9;
    }

    /**
     * 获得毫秒值
     *
     * @param time epoch的秒值或毫秒值
     * @return long
     */
    private static long getMillis(Long time) {
        if (time == null) {
            return 0;
        }
        int len = time.toString().length();
        return len == 10 || len == 9 ? time * 1000 : time;
    }

    /**
     * 格式化时间
     *
     * @param time    待格式化的时间
     * @param pattern 格式化规则
     * @return {@link String}
     */
    public static String formatTimeMillis(Long time, String pattern) {
        return formatDate(getDateFromMillis(time), pattern);
    }

    /**
     * 按照{yyyy-MM-dd}格式化时间
     *
     * @param times epoch的秒值或毫秒值
     * @return {@link String}
     */
    public static String formatTimeMillisByDatePattern(long times) {
        return formatTimeMillis(times, DEFAULT_PATTERNS[3]);
    }

    /**
     * 按照{yyyy-MM-dd HH:mm:ss}格式化时间
     *
     * @param times epoch的秒值或毫秒值
     * @return {@link String}
     */
    public static String formatTimeMillisByFullDatePattern(long times) {
        return formatTimeMillis(times, DEFAULT_PATTERNS[0]);
    }

    /**
     * 根据pattern规则转换字符串为Date
     *
     * @param source  源
     * @param pattern 模式
     * @return {@link Date}
     */
    public static Date parse(String source, String pattern) {
        try {
            return new SimpleDateFormat(pattern).parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据pattern和时区转换字符串为Date
     *
     * @param source   源
     * @param pattern  模式
     * @param timeZone 时区
     * @return {@link Date}
     */
    public static Date parse(String source, String pattern, TimeZone timeZone) {
        try {
            DateFormat dateFormat = new SimpleDateFormat(pattern);
            dateFormat.setTimeZone(timeZone);
            return dateFormat.parse(source);
        } catch (ParseException e) {
            logger.error("dates", e);
        }
        return null;
    }

    /**
     * 根据{@link Dates#DEFAULT_PATTERNS}转换字符串为Date
     *
     * @param source 源
     * @return {@link Date}
     */
    public static Date parse(String source) {
        for (String pattern : DEFAULT_PATTERNS) {
            try {
                return new SimpleDateFormat(pattern).parse(source);
            } catch (ParseException ignored) {
            }
        }
        throw new RuntimeException("找不到适合的pattern");
    }

    /**
     * 解析时间,米尔斯
     *
     * @param source  源
     * @param pattern 模式
     * @return long
     * @see Dates#parse(String, String)
     */
    public static long parseToTimeMillis(String source, String pattern) {
        return parse(source, pattern).getTime();
    }

    /**
     * 解析时间,米尔斯
     *
     * @param source 源
     * @return long
     * @see Dates#parse(String)
     */
    public static long parseToTimeMillis(String source) {
        return parse(source).getTime();
    }

    /**
     * 根据时间返回当前是星期几
     *
     * @param date 日期
     * @return 0周日 1周一 2周二 3周三 4周四 5周五 6周六
     */
    public static int getWeekMark(Date date) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        // 获取当前日期是周几
        int week = 0;
        switch (cal.get(GregorianCalendar.DAY_OF_WEEK)) {
            case GregorianCalendar.MONDAY -> week = 1;
            case GregorianCalendar.TUESDAY -> week = 2;
            case GregorianCalendar.WEDNESDAY -> week = 3;
            case GregorianCalendar.THURSDAY -> week = 4;
            case GregorianCalendar.FRIDAY -> week = 5;
            case GregorianCalendar.SATURDAY -> week = 6;
            default -> {
            }
        }
        return week;
    }

    /**
     * 获得当前时间的epoch秒值
     *
     * @return long
     */
    public static long now() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 获取当前日期前一个月日期
     *
     * @return {@link Date}
     */
    public static Date getBeforeByMonth() {
        // 当前日期
        Date date = new Date();
        // 格式化对象
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 日历对象
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        // 月份减一
        calendar.add(Calendar.MONTH, -1);
        // 输出格式化的日期
        return parse(sdf.format(calendar.getTime()));
    }

    /**
     * 获得指定日期前(后)x天的日期
     *
     * @param time 时间戳
     * @param day  天数（如果day数为负数,说明是此日期前的天数）
     * @return 时间戳（秒值）
     */
    public static long beforNumDay(long time, int day) {
        Calendar c = Calendar.getInstance();
        c.setTime(parse(Dates.formatTimeMillis(time, "yyyy-MM-dd HH:mm:ss")));
        c.add(Calendar.DAY_OF_YEAR, day);
        return parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(c.getTime())).getTime() / 1000;
    }

    /**
     * 获取当前月份的最后一天
     *
     * @param date 日期
     * @return 返回日期的原始int值，从1到31
     */
    public static int getLastDayOfMonth(Date date) {
        LocalDateTime localDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return localDate.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
    }

    /**
     * 获取当前月份的最后一天
     *
     * @return 返回日期的原始int值，从1到31
     */
    public static int getLastDayOfMonth() {
        return getLastDayOfMonth(new Date());
    }

    /**
     * 获得针对目标时间的倒计时
     *
     * @param destTime epoch格式的目标时间
     * @return 数组中四个元素，依次是：日、时、分、秒
     */
    public static long[] countDown(long destTime) {
        return endDown(now(), destTime);
    }

    /**
     * 计算两个日期之间的差
     *
     * @param startTime epoch格式的开始时间
     * @param endTime   epoch格式的结束时间
     * @return 数组中四个元素，依次是：日、时、分、秒
     */
    public static long[] endDown(long startTime, long endTime) {
        Validate.isTrue(validTime(startTime), "endTime必须为秒或毫秒");
        Validate.isTrue(validTime(endTime), "startTime必须为秒或毫秒");

        startTime = getMillis(startTime) / 1000;
        endTime = getMillis(endTime) / 1000;
        long[] arr = new long[4];
        long second = endTime - startTime;
        if (second < 0) {
            arr[0] = 0;
            arr[1] = 0;
            arr[2] = 0;
            arr[3] = 0;
            return arr;
        }

        long minite = second / 60;
        long hour = minite / 60;
        long day = hour / 24;

        second = second % 60;
        minite = minite % 60;
        hour = hour % 24;

        arr[0] = day;
        arr[1] = hour;
        arr[2] = minite;
        arr[3] = second % 60;
        return arr;
    }

    /**
     * 据今天结束还有多少秒
     *
     * @return long
     */
    public static long endOfToday() {
        return (maximumTimeMillisOfToday() - System.currentTimeMillis()) / 1000;
    }

    /**
     * 今天的结束时间
     *
     * @return 返回millis值
     */
    public static long maximumTimeMillisOfToday() {
        LocalDateTime localDateTime = LocalDateTime.now();
        return Date.from(localDateTime.with(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()).getTime();
    }

    /**
     * 今天开始的时间
     *
     * @return 返回millis值
     */
    public static long minimumTimeMillisOfToday() {
        LocalDateTime localDateTime = LocalDateTime.now();
        return Date.from(localDateTime.with(LocalTime.MIN).atZone(ZoneId.systemDefault()).toInstant()).getTime();
    }

    /**
     * 以‘yyyy-MM-dd HH:mm:ss’格式化当前日期
     *
     * @return {@link String}
     */
    public static String formatNow() {
        return formatDate(new Date(), null);
    }

    /**
     * 以指定的pattern格式化当前日期
     *
     * @param pattern 模式
     * @return {@link String}
     */
    public static String formatNow(String pattern) {
        return formatDate(new Date(), pattern);
    }

    /**
     * 获取日期当天结束时间
     *
     * @param dateStr yyyy-MM-dd格式的字符串
     * @return 时间戳（秒值）
     */
    public static long endOfDate(String dateStr) {
        String today = dateStr + " 23:59:59";
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            Date date = dateformat.parse(today);
            return date.getTime() / 1000;
        } catch (ParseException e) {
            logger.error("dates", e);
            return 0;
        }
    }

    /**
     * 获取日期当天开始时间
     *
     * @param datestr yyyy-MM-dd格式的时间
     * @return 时间戳（秒值）
     */
    public static long beginOfDate(String datestr) {
        String today = datestr + " 00:00:00";
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            Date date = dateformat.parse(today);
            return date.getTime() / 1000;
        } catch (ParseException e) {
            logger.error("dates", e);
            return 0;
        }
    }

    /**
     * 获取入参日期的当月日历（包含往前和往后的补充数据，从周日开始到周六结束的完整日历）
     *
     * @param date 日期
     * @return 日历数据会从周日开始到周六结束，集合中的日期都是一天的开始时间
     */
    public static List<Date> getMonthDateList(Date date) {
        Calendar calendar = Calendar.getInstance();
        List<Date> list = new ArrayList<>();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(calendar.getTime());
        int week = cal.get(GregorianCalendar.DAY_OF_WEEK);
        // 务必从周日开始
        calendar.add(Calendar.DATE, -week);
        for (int x = 0; x < week; x++) {
            calendar.add(Calendar.DATE, 1);
            list.add(calendar.getTime());
        }
        int day = Dates.getLastDayOfMonth(date);
        // 务必从周六结束
        cal.setTime(date);
        cal.set(Calendar.DATE, day);
        week = cal.get(GregorianCalendar.DAY_OF_WEEK);
        if (week != 7) {
            week = 7 - week;
        }
        int monthCnt = week + day - 1;
        for (int i = 0; i < monthCnt; i++) {
            calendar.add(Calendar.DATE, 1);
            list.add(calendar.getTime());
        }
        return list;
    }

    /**
     * 获取当月日历天数
     *
     * @param date 日期
     * @return 集合中的日期都是一天的开始时间
     */
    public static List<Date> getCurrentMonthDateList(Date date) {
        Calendar calendar = Calendar.getInstance();
        List<Date> list = new ArrayList<>();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        int monthCnt = Dates.getLastDayOfMonth(date);
        for (int i = 0; i < monthCnt; i++) {
            calendar.add(Calendar.DATE, 1);
            list.add(calendar.getTime());
        }
        return list;
    }

    /**
     * 查看当前时间是否是所在月第一天
     *
     * @param time 时间
     * @return boolean
     */
    public static boolean isFirstDayOfMonth(Long time) {
        Calendar calendar = Calendar.getInstance();
        if (!validTime(time)) {
            return false;
        }
        calendar.setTimeInMillis(getMillis(time));
        return calendar.get(Calendar.DAY_OF_MONTH) == 1;
    }

    /**
     * 解析一个范围日期
     *
     * @param dataRange 时间区间，以 - 作为分隔符
     * @param pattern   时间格式
     * @return 秒值数组
     */
    public static Long[] parseRangeDate(String dataRange, String pattern) {
        long beginTime = 0L;
        long endTime = 0L;
        if (StringUtils.isNotBlank(dataRange)) {
            String[] dateArr = dataRange.split("-");
            beginTime = parse(dateArr[0], pattern).getTime() / 1000;
            endTime = beforNumDay(parse(dateArr[1], pattern).getTime(), 1) - 1;
        }
        return new Long[]{beginTime, endTime};
    }

    /**
     * 解析ACE模板获取的起止日期
     *
     * @param dataRange 起止时间(支持模板:MM/dd/yyyy-MM/dd/yyyy)
     * @return {@link Long[]}
     */
    public static Long[] parseAceRangeDate(String dataRange) {
        return parseRangeDate(dataRange, "MM/dd/yyyy");
    }

    /**
     * LocalDate转换为秒值（从1970年初开始计算）
     *
     * @param localDate 当地日期
     * @return long
     */
    public static long getEpochSecond(LocalDate localDate) {
        return localDate.atStartOfDay().toEpochSecond(UTC_P8);
    }

    /**
     * LocalDateTime转换为秒值（从1970年初开始计算）
     *
     * @param localDateTime 当地日期时间
     * @return long
     */
    public static long getEpochSecond(LocalDateTime localDateTime) {
        return localDateTime.toEpochSecond(UTC_P8);
    }

    /**
     * 当地日期时间
     *
     * @param date 日期
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * 两个时间戳是否为同一天
     *
     * @param time1 time1
     * @param time2 time2
     * @return boolean
     */
    public static boolean isSameDay(Long time1, Long time2) {
        LocalDate localDate1 = LocalDate.ofEpochDay(getMillis(time1) / 1000 / (3600 * 24));
        LocalDate localDate2 = LocalDate.ofEpochDay(getMillis(time2) / 1000 / (3600 * 24));
        return localDate1.isEqual(localDate2);
    }

    /**
     * 可读性好的秒值
     *
     * @param secord 西科
     * @return {@link String}
     */
    public static String humanReadableSecord(long secord) {
        return humanReadableDuration(Duration.ofSeconds(secord));
    }

    /**
     * 可读性好的毫秒值
     *
     * @param millis 米尔斯
     * @return {@link String}
     */
    public static String humanReadableMillis(long millis) {
        return humanReadableDuration(Duration.ofMillis(millis));
    }

    /**
     * 可读性好的时间量
     *
     * @param duration 持续时间
     * @return {@link String}
     */
    public static String humanReadableDuration(Duration duration) {
        if (duration.toMillis() < 1000) {
            return (duration.toMillis() / 1000.0) + "s";
        } else {
            String s = (duration.toSecondsPart() + (duration.toMillisPart() > 0 ? 1 : 0)) + "s";
            return (duration.toDaysPart() + "d" + duration.toHoursPart() + "h" + duration.toMinutesPart() + "m" + s).replaceAll(
                    "(^|(?<=d)|(?<=h)|(?<=m))0[dhms]", "");
        }
    }

    /**
     * 计算两个时间戳之间的距离
     *
     * @param unit  周期单位
     * @param time1 起点时间
     * @param time2 终点时间
     * @return long
     */
    public static long between(ChronoUnit unit, long time1, long time2) {
        LocalDateTime givenDate1 = Instant.ofEpochMilli(getMillis(time1)).atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime givenDate2 = Instant.ofEpochMilli(getMillis(time2)).atZone(ZoneId.systemDefault()).toLocalDateTime();
        return unit.between(givenDate1, givenDate2);
    }
}
