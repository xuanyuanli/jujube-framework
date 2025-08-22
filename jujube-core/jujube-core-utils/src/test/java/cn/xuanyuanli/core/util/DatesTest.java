package cn.xuanyuanli.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Dates 日期工具类测试")
class DatesTest {

    @Test
    @DisplayName("应该正确判断是否为同一天")
    public void shouldCorrectlyDetermineSameDay() {
        long t1 = Dates.getEpochSecond(LocalDateTime.of(2021, 1, 1, 10, 45, 8));
        long t2 = Dates.getEpochSecond(LocalDateTime.of(2021, 1, 1, 18, 45, 8));
        Assertions.assertTrue(Dates.isSameDay(t1, t2));

        t1 = Dates.getEpochSecond(LocalDateTime.of(2021, 1, 2, 10, 45, 8));
        t2 = Dates.getEpochSecond(LocalDateTime.of(2021, 1, 1, 18, 45, 8));
        Assertions.assertFalse(Dates.isSameDay(t1, t2));

        t1 = Dates.getEpochSecond(LocalDateTime.of(2021, 2, 1, 10, 45, 8));
        t2 = Dates.getEpochSecond(LocalDateTime.of(2021, 1, 1, 18, 45, 8));
        Assertions.assertFalse(Dates.isSameDay(t1, t2));
    }

    @Test
    @DisplayName("应该正确格式化时间戳")
    void shouldCorrectlyFormatTimeMillis() {
        assertEquals("2021-09-09 19:00:52", Dates.formatTimeMillis(1631185252L, null));
        assertEquals("2021-09-09", Dates.formatTimeMillis(1631185252L, "yyyy-MM-dd"));

        assertEquals("2021-09-09 19:00:52", Dates.formatTimeMillis(1631185252000L, null));
        assertEquals("2021-09-09 19:00:52", Dates.formatTimeMillis(1631185252456L, null));
    }

    @Test
    void getDateFromMillis() {
        assertEquals(Dates.getDateFromMillis(1631185252L)
                , Date.from(LocalDateTime.of(2021, 9, 9, 19, 0, 52).toInstant(ZoneOffset.of("+8"))));
        assertEquals(Dates.getDateFromMillis(1631185252789L)
                , Date.from(LocalDateTime.of(2021, 9, 9, 19, 0, 52, 789000000).toInstant(ZoneOffset.of("+8"))));
    }

    @Test
    void testFormatDate() {
        String result = Dates.formatDate(new GregorianCalendar(2022, Calendar.JUNE, 1, 9, 26, 10).getTime(), "yyyy-MM-dd HH:mm:ss");
        assertEquals("2022-06-01 09:26:10", result);
    }

    @Test
    void testFormatDate2() {
        String result = Dates.formatDate(new GregorianCalendar(2022, Calendar.JUNE, 1, 9, 26, 10).getTime(), "yyyy-MM-dd HH:mm:ss",
                TimeZone.getTimeZone("Asia/Shanghai"));
        assertEquals("2022-06-01 09:26:10", result);

        result = Dates.formatDate(new GregorianCalendar(2022, Calendar.JUNE, 1, 9, 26, 10).getTime(), "yyyy-MM-dd HH:mm:ss", null);
        assertEquals("2022-06-01 09:26:10", result);

        result = Dates.formatDate(new GregorianCalendar(2022, Calendar.JUNE, 1, 9, 26, 10).getTime(), "yyyy-MM-dd HH:mm:ss",
                TimeZone.getTimeZone("America/New_York"));
        assertEquals("2022-05-31 21:26:10", result);
    }

    @Test
    void testFormatTimeMillisByDatePattern() {
        assertEquals("", Dates.formatTimeMillisByDatePattern(0L));
        assertEquals("2022-06-01", Dates.formatTimeMillisByDatePattern(1654067649036L));
        assertEquals("2022-06-01", Dates.formatTimeMillisByDatePattern(1654067649L));
    }

    @Test
    void testFormatTimeMillisByFullDatePattern() {
        assertEquals("", Dates.formatTimeMillisByFullDatePattern(0L));
        assertEquals("2022-06-01 15:14:09", Dates.formatTimeMillisByFullDatePattern(1654067649036L));
        assertEquals("2022-06-01 15:14:09", Dates.formatTimeMillisByFullDatePattern(1654067649L));
        assertEquals("1997-01-24 09:00:49", Dates.formatTimeMillisByFullDatePattern(854067649L));
        assertEquals("1978-01-19 22:20:49", Dates.formatTimeMillisByFullDatePattern(254067649L));
        assertEquals("1997-01-24 09:00:49", Dates.formatTimeMillisByFullDatePattern(854067649000L));
        assertEquals("1978-01-19 22:20:49", Dates.formatTimeMillisByFullDatePattern(254067649000L));
    }

    @Test
    void testParse() {
        assertEquals(new GregorianCalendar(2022, Calendar.JUNE, 1).getTime(), Dates.parse("2022-06-01", "yyyy-MM-dd"));
    }

    @Test
    void testParse2() {
        assertEquals(new GregorianCalendar(2022, Calendar.JUNE, 1, 9, 26, 10).getTime(),
                Dates.parse("2022-05-31 21:26:10", "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("America/New_York")));
        assertEquals(new GregorianCalendar(2022, Calendar.JUNE, 1, 9, 26, 10).getTime(),
                Dates.parse("2022-06-01 09:26:10", "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("Asia/Shanghai")));
    }

    @Test
    void testParse3() {
        assertEquals(new GregorianCalendar(2022, Calendar.JUNE, 1, 9, 26, 10).getTime(),
                Dates.parse("2022-06-01 09:26:10"));
        assertEquals(new GregorianCalendar(2022, Calendar.JUNE, 1, 9, 26).getTime(),
                Dates.parse("2022-06-01 09:26"));
        assertEquals(new GregorianCalendar(2022, Calendar.JUNE, 1).getTime(),
                Dates.parse("2022-06-01"));
    }

    @Test
    void testParseToTimeMillis() {
        long result = Dates.parseToTimeMillis("2022-06-01 09:26:10", "yyyy-MM-dd HH:mm:ss");
        assertEquals(1654046770000L, result);
    }

    @Test
    void testParseToTimeMillis2() {
        long result = Dates.parseToTimeMillis("2022-06-01 09:26:10");
        assertEquals(1654046770000L, result);
    }

    @Test
    void testGetWeekMark() {
        int result = Dates.getWeekMark(new GregorianCalendar(2022, Calendar.JUNE, 1, 9, 26).getTime());
        assertEquals(3, result);
    }

    @Test
    void testNow() {
        assertEquals(System.currentTimeMillis() / 1000, Dates.now());
    }

    @Test
    void testGetBeforeByMonth() {
        Date result = Dates.getBeforeByMonth();
        assertEquals(LocalDateTime.now().getMonth().minus(1), LocalDateTime.ofInstant(result.toInstant(), ZoneId.systemDefault()).getMonth());
    }

    @Test
    void testBeforNumDay() {
        long actual = Dates.beforNumDay(1654046770000L, 5);
        assertEquals(Dates.parseToTimeMillis("2022-06-06 09:26:10") / 1000, actual);
        long actual1 = Dates.beforNumDay(1654046770000L, -5);
        assertEquals(Dates.parseToTimeMillis("2022-05-27 09:26:10") / 1000, actual1);
    }

    @Test
    void testGetLastDayOfMonth() {
        int result = Dates.getLastDayOfMonth(new GregorianCalendar(2022, Calendar.JUNE, 1).getTime());
        assertEquals(30, result);
    }

    @Test
    void testGetLastDayOfMonth2() {
        int result = Dates.getLastDayOfMonth();
        assertEquals(LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth(), result);
    }

    @Test
    void testCountDown() {
        Assertions.assertArrayEquals(new long[]{0, 0, 0, 0}, Dates.countDown(Dates.now() - 60));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Dates.countDown(Dates.now() * 10));
        Assertions.assertArrayEquals(new long[]{0, 0, 1, 0}, Dates.countDown(Dates.now() + 60));
        Assertions.assertArrayEquals(new long[]{0, 0, 0, 12}, Dates.countDown(Dates.now() + 12));
        Assertions.assertArrayEquals(new long[]{0, 1, 0, 10}, Dates.countDown(Dates.now() + 3610));
        Assertions.assertArrayEquals(new long[]{1, 0, 1, 15}, Dates.countDown(Dates.now() + 86475));
    }

    @Test
    void testEndDown() {
        long[] result = Dates.endDown(Dates.now(), Dates.now() + 60);
        Assertions.assertArrayEquals(new long[]{0, 0, 1, 0}, result);
    }

    @Test
    void testEndOfToday() {
        long result = Dates.endOfToday();
        assertEquals((Dates.maximumTimeMillisOfToday() - System.currentTimeMillis()) / 1000, result);
    }

    @Test
    void testMinimumTimeMillisOfToday() {
        long result = Dates.minimumTimeMillisOfToday();
        assertEquals(Dates.parseToTimeMillis(Dates.formatNow("yyyy-MM-dd")), result);
    }

    @Test
    void testFormatNow() {
        String result = Dates.formatNow();
        Assertions.assertNotNull(result);
    }

    @Test
    void testFormatNow2() {
        String result = Dates.formatNow("yyyy-MM-dd");
        Assertions.assertNotNull(result);
    }

    @Test
    void testEndOfDate() {
        long result = Dates.endOfDate("2022-06-01");
        assertEquals(new GregorianCalendar(2022, Calendar.JUNE, 1, 23, 59, 59).getTime().getTime() / 1000, result);
    }

    @Test
    void testBeginOfDate() {
        long result = Dates.beginOfDate("2022-06-01");
        assertEquals(new GregorianCalendar(2022, Calendar.JUNE, 1, 0, 0, 0).getTime().getTime() / 1000, result);
    }

    @Test
    void testGetMonthDateList() {
        List<Date> result = Dates.getMonthDateList(GregorianCalendar.getInstance().getTime());
        Assertions.assertTrue(result.size() >= Dates.getLastDayOfMonth());
    }

    @Test
    void testGetTotalMonthDateList() {
        List<Date> result = Dates.getCurrentMonthDateList(GregorianCalendar.getInstance().getTime());
        assertEquals(result.size(), Dates.getLastDayOfMonth());
    }

    @Test
    void testIsFirstDayOfMonth() {
        boolean result = Dates.isFirstDayOfMonth(new GregorianCalendar(2022, Calendar.JUNE, 1).getTime().getTime());
        Assertions.assertTrue(result);
        result = Dates.isFirstDayOfMonth(new GregorianCalendar(2022, Calendar.JUNE, 10).getTime().getTime());
        Assertions.assertFalse(result);
    }

    @Test
    void testParseAceRangeDate() {
        Long[] result = Dates.parseAceRangeDate("10/1/2022-10/31/2022");
        Assertions.assertArrayEquals(new Long[]{1664553600L, 1667231999L}, result);
    }

    @Test
    void testGetEpochSecond() {
        long result = Dates.getEpochSecond(LocalDate.of(2022, Month.JUNE, 1));
        assertEquals(1654012800L, result);
    }

    @Test
    void testGetEpochSecond2() {
        long result = Dates.getEpochSecond(LocalDateTime.of(2022, Month.JUNE, 1, 0, 0, 0));
        assertEquals(1654012800L, result);
    }

    @Test
    void testDateToLocalDateTime() {
        LocalDateTime result = Dates.dateToLocalDateTime(new GregorianCalendar(2022, Calendar.JUNE, 1, 9, 26, 52).getTime());
        assertEquals(LocalDateTime.of(2022, Month.JUNE, 1, 9, 26, 52), result);
    }

    @Test
    @DisplayName("应该将秒数转换为可读格式")
    void shouldConvertSecondsToHumanReadableFormat() {
        assertEquals("1m", Dates.humanReadableSecord(60));
        assertEquals("1s", Dates.humanReadableSecord(1));
        assertEquals("2h", Dates.humanReadableSecord(7200));
        assertEquals("2h1s", Dates.humanReadableSecord(7201));
        assertEquals("2h1m11s", Dates.humanReadableSecord(7271));
        assertEquals("8d8h1m11s", Dates.humanReadableSecord(720071));
    }

    @Test
    void humanReadableMillis() {
        assertEquals(Dates.humanReadableMillis(60), "0.06s");
        assertEquals(Dates.humanReadableMillis(611), "0.611s");
        assertEquals(Dates.humanReadableMillis(1000), "1s");
        assertEquals(Dates.humanReadableMillis(71000), "1m11s");
        assertEquals(Dates.humanReadableMillis(7200960), "2h1s");
        assertEquals(Dates.humanReadableMillis(7201000), "2h1s");
        assertEquals(Dates.humanReadableMillis(7271000), "2h1m11s");
        assertEquals(Dates.humanReadableMillis(720071000), "8d8h1m11s");
        assertEquals(Dates.humanReadableMillis(720071050), "8d8h1m12s");
    }
    @Test
    void humanReadableMillis2h() {
        assertEquals(Dates.humanReadableMillis(7200000), "2h");
    }

    @Test
    void humanReadableDuration() {
        assertEquals(Dates.humanReadableDuration(Duration.ofMillis(230)), "0.23s");
        assertEquals(Dates.humanReadableDuration(Duration.ofSeconds(230)), "3m50s");
        assertEquals(Dates.humanReadableDuration(Duration.ofMinutes(61)), "1h1m");
        assertEquals(Dates.humanReadableDuration(Duration.ofHours(25)), "1d1h");
        assertEquals(Dates.humanReadableDuration(Duration.ofDays(230)), "230d");
    }

    @Test
    void testBetween_sameTimes() {
        long time = System.currentTimeMillis();
        long days = Dates.between(ChronoUnit.DAYS, time, time);
        assertEquals(0, days);
    }

    @Test
    void testBetween_oneDayApart() {
        long time1 = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long time2 = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long days = Dates.between(ChronoUnit.DAYS, time1, time2);
        assertEquals(1, days);
    }

    @Test
    void testBetween_negativeDays() {
        long time1 = LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long time2 = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long days = Dates.between(ChronoUnit.DAYS, time1, time2);
        assertEquals(1, days);
    }

    @Test
    void testBetween_largeTimeDifference() {
        long time1 = Instant.parse("2000-01-01T00:00:00Z").toEpochMilli();
        long time2 = Instant.parse("2022-01-01T00:00:00Z").toEpochMilli();
        long days = Dates.between(ChronoUnit.DAYS, time1, time2);
        assertEquals(8036, days);
    }

    @Test
    void testBetween() {
        long time1 = 1635734400000L; // 2021-11-01T00:00:00Z
        long time2 = 1638326400000L; // 2021-12-01T00:00:00Z

        // Expected results
        long expectedDays = 30;
        long expectedMonths = 1;
        long expectedYears = 0;
        long expectedHours = 720;
        long expectedMinutes = 43200;
        long expectedSeconds = 2592000;

        // Actual results
        long actualDays = Dates.between(ChronoUnit.DAYS, time1, time2);
        long actualMonths = Dates.between(ChronoUnit.MONTHS, time1, time2);
        long actualYears = Dates.between(ChronoUnit.YEARS, time1, time2);
        long actualHours = Dates.between(ChronoUnit.HOURS, time1, time2);
        long actualMinutes = Dates.between(ChronoUnit.MINUTES, time1, time2);
        long actualSeconds = Dates.between(ChronoUnit.SECONDS, time1, time2);

        assertEquals(expectedDays, actualDays);
        assertEquals(expectedMonths, actualMonths);
        assertEquals(expectedYears, actualYears);
        assertEquals(expectedHours, actualHours);
        assertEquals(expectedMinutes, actualMinutes);
        assertEquals(expectedSeconds, actualSeconds);
    }
}

