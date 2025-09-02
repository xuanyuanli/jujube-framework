package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Dates 日期工具类测试")
class DatesTest {

    @Nested
    @DisplayName("日期比较测试")
    class DateComparisonTests {

        @Test
        @DisplayName("isSameDay_应该返回true_当两个时间戳是同一天时")
        void isSameDay_shouldReturnTrue_whenTimestampsAreOnSameDay() {
            // Arrange
            long t1 = Dates.getEpochSecond(LocalDateTime.of(2021, 1, 1, 10, 45, 8));
            long t2 = Dates.getEpochSecond(LocalDateTime.of(2021, 1, 1, 18, 45, 8));

            // Act & Assert
            assertThat(Dates.isSameDay(t1, t2)).isTrue();
        }

        @Test
        @DisplayName("isSameDay_应该返回false_当两个时间戳不是同一天时")
        void isSameDay_shouldReturnFalse_whenTimestampsAreNotOnSameDay() {
            // Arrange
            long t1 = Dates.getEpochSecond(LocalDateTime.of(2021, 1, 2, 10, 45, 8));
            long t2 = Dates.getEpochSecond(LocalDateTime.of(2021, 1, 1, 18, 45, 8));

            // Act & Assert
            assertThat(Dates.isSameDay(t1, t2)).isFalse();
        }

        @Test
        @DisplayName("isSameDay_应该返回false_当两个时间戳不是同一月时")
        void isSameDay_shouldReturnFalse_whenTimestampsAreNotInSameMonth() {
            // Arrange
            long t1 = Dates.getEpochSecond(LocalDateTime.of(2021, 2, 1, 10, 45, 8));
            long t2 = Dates.getEpochSecond(LocalDateTime.of(2021, 1, 1, 18, 45, 8));

            // Act & Assert
            assertThat(Dates.isSameDay(t1, t2)).isFalse();
        }
    }

    @Nested
    @DisplayName("时间格式化测试")
    class TimeFormattingTests {

        @Test
        @DisplayName("formatTimeMillis_应该正确格式化时间戳_当使用不同格式时")
        void formatTimeMillis_shouldFormatCorrectly_whenUsingDifferentFormats() {
            // Act & Assert
            assertThat(Dates.formatTimeMillis(1631185252L, null)).isEqualTo("2021-09-09 19:00:52");
            assertThat(Dates.formatTimeMillis(1631185252L, "yyyy-MM-dd")).isEqualTo("2021-09-09");
            assertThat(Dates.formatTimeMillis(1631185252000L, null)).isEqualTo("2021-09-09 19:00:52");
            assertThat(Dates.formatTimeMillis(1631185252456L, null)).isEqualTo("2021-09-09 19:00:52");
        }

        @Test
        @DisplayName("getDateFromMillis_应该正确转换毫秒数为Date_当输入不同精度时")
        void getDateFromMillis_shouldConvertMillisToDate_whenInputDifferentPrecision() {
            // Act & Assert
            assertThat(Dates.getDateFromMillis(1631185252L))
                .isEqualTo(Date.from(LocalDateTime.of(2021, 9, 9, 19, 0, 52).toInstant(ZoneOffset.of("+8"))));
            assertThat(Dates.getDateFromMillis(1631185252789L))
                .isEqualTo(Date.from(LocalDateTime.of(2021, 9, 9, 19, 0, 52, 789000000).toInstant(ZoneOffset.of("+8"))));
        }

        @Test
        @DisplayName("formatDate_应该正确格式化Date对象_当使用指定格式时")
        void formatDate_shouldFormatDateCorrectly_whenUsingSpecifiedFormat() {
            // Arrange
            Date date = new GregorianCalendar(2022, Calendar.JUNE, 1, 9, 26, 10).getTime();
            
            // Act
            String result = Dates.formatDate(date, "yyyy-MM-dd HH:mm:ss");
            
            // Assert
            assertThat(result).isEqualTo("2022-06-01 09:26:10");
        }

        @Test
        @DisplayName("formatDate_应该根据时区正确格式化_当使用不同时区时")
        void formatDate_shouldFormatWithTimeZone_whenUsingDifferentTimeZones() {
            // Arrange
            Date date = new GregorianCalendar(2022, Calendar.JUNE, 1, 9, 26, 10).getTime();
            String pattern = "yyyy-MM-dd HH:mm:ss";
            
            // Act & Assert
            assertThat(Dates.formatDate(date, pattern, TimeZone.getTimeZone("Asia/Shanghai")))
                .isEqualTo("2022-06-01 09:26:10");
            
            assertThat(Dates.formatDate(date, pattern, null))
                .isEqualTo("2022-06-01 09:26:10");
            
            assertThat(Dates.formatDate(date, pattern, TimeZone.getTimeZone("America/New_York")))
                .isEqualTo("2022-05-31 21:26:10");
        }

        @Test
        @DisplayName("formatTimeMillisByDatePattern_应该按日期格式格式化_当输入时间戳时")
        void formatTimeMillisByDatePattern_shouldFormatByDatePattern_whenInputTimestamp() {
            // Act & Assert
            assertThat(Dates.formatTimeMillisByDatePattern(0L)).isEqualTo("");
            assertThat(Dates.formatTimeMillisByDatePattern(1654067649036L)).isEqualTo("2022-06-01");
            assertThat(Dates.formatTimeMillisByDatePattern(1654067649L)).isEqualTo("2022-06-01");
        }

        @Test
        @DisplayName("formatTimeMillisByFullDatePattern_应该按完整日期格式格式化_当输入时间戳时")
        void formatTimeMillisByFullDatePattern_shouldFormatByFullDatePattern_whenInputTimestamp() {
            // Act & Assert
            assertThat(Dates.formatTimeMillisByFullDatePattern(0L)).isEqualTo("");
            assertThat(Dates.formatTimeMillisByFullDatePattern(1654067649036L)).isEqualTo("2022-06-01 15:14:09");
            assertThat(Dates.formatTimeMillisByFullDatePattern(1654067649L)).isEqualTo("2022-06-01 15:14:09");
            assertThat(Dates.formatTimeMillisByFullDatePattern(854067649L)).isEqualTo("1997-01-24 09:00:49");
            assertThat(Dates.formatTimeMillisByFullDatePattern(254067649L)).isEqualTo("1978-01-19 22:20:49");
            assertThat(Dates.formatTimeMillisByFullDatePattern(854067649000L)).isEqualTo("1997-01-24 09:00:49");
            assertThat(Dates.formatTimeMillisByFullDatePattern(254067649000L)).isEqualTo("1978-01-19 22:20:49");
        }

        @Test
        @DisplayName("formatNow_应该格式化当前时间_当不指定格式时")
        void formatNow_shouldFormatCurrentTime_whenNoFormatSpecified() {
            // Act
            String result = Dates.formatNow();
            
            // Assert
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("formatNow_应该格式化当前时间_当指定格式时")
        void formatNow_shouldFormatCurrentTime_whenFormatSpecified() {
            // Act
            String result = Dates.formatNow("yyyy-MM-dd");
            
            // Assert
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("时间解析测试")
    class TimeParsingTests {

        @Test
        @DisplayName("parse_应该正确解析日期字符串_当使用指定格式时")
        void parse_shouldParseCorrectly_whenUsingSpecifiedFormat() {
            // Arrange
            Date expected = new GregorianCalendar(2022, Calendar.JUNE, 1).getTime();
            
            // Act
            Date result = Dates.parse("2022-06-01", "yyyy-MM-dd");
            
            // Assert
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("parse_应该根据时区解析时间_当指定时区时")
        void parse_shouldParseWithTimeZone_whenTimeZoneSpecified() {
            // Arrange
            Date expected = new GregorianCalendar(2022, Calendar.JUNE, 1, 9, 26, 10).getTime();
            String pattern = "yyyy-MM-dd HH:mm:ss";
            
            // Act & Assert
            assertThat(Dates.parse("2022-05-31 21:26:10", pattern, TimeZone.getTimeZone("America/New_York")))
                .isEqualTo(expected);
            assertThat(Dates.parse("2022-06-01 09:26:10", pattern, TimeZone.getTimeZone("Asia/Shanghai")))
                .isEqualTo(expected);
        }

        @Test
        @DisplayName("parse_应该自动识别格式解析_当使用默认解析时")
        void parse_shouldParseWithAutoFormat_whenUsingDefaultParsing() {
            // Act & Assert
            assertThat(Dates.parse("2022-06-01 09:26:10"))
                .isEqualTo(new GregorianCalendar(2022, Calendar.JUNE, 1, 9, 26, 10).getTime());
            assertThat(Dates.parse("2022-06-01 09:26"))
                .isEqualTo(new GregorianCalendar(2022, Calendar.JUNE, 1, 9, 26).getTime());
            assertThat(Dates.parse("2022-06-01"))
                .isEqualTo(new GregorianCalendar(2022, Calendar.JUNE, 1).getTime());
        }

        @Test
        @DisplayName("parseToTimeMillis_应该解析为时间戳_当使用指定格式时")
        void parseToTimeMillis_shouldParseToTimestamp_whenUsingSpecifiedFormat() {
            // Act
            long result = Dates.parseToTimeMillis("2022-06-01 09:26:10", "yyyy-MM-dd HH:mm:ss");
            
            // Assert
            assertThat(result).isEqualTo(1654046770000L);
        }

        @Test
        @DisplayName("parseToTimeMillis_应该解析为时间戳_当使用默认格式时")
        void parseToTimeMillis_shouldParseToTimestamp_whenUsingDefaultFormat() {
            // Act
            long result = Dates.parseToTimeMillis("2022-06-01 09:26:10");
            
            // Assert
            assertThat(result).isEqualTo(1654046770000L);
        }

        @Test
        @DisplayName("parseAceRangeDate_应该解析Ace日期范围_当输入范围字符串时")
        void parseAceRangeDate_shouldParseAceDateRange_whenInputRangeString() {
            // Act
            Long[] result = Dates.parseAceRangeDate("10/1/2022-10/31/2022");
            
            // Assert
            assertThat(result).containsExactly(1664553600L, 1667231999L);
        }
    }

    @Nested
    @DisplayName("时间获取测试")
    class TimeGettingTests {

        @Test
        @DisplayName("now_应该返回当前时间戳_当获取当前时间时")
        void now_shouldReturnCurrentTimestamp_whenGettingCurrentTime() {
            // Act
            long result = Dates.now();
            
            // Assert
            assertThat(result).isEqualTo(System.currentTimeMillis() / 1000);
        }

        @Test
        @DisplayName("getWeekMark_应该返回星期标记_当输入日期时")
        void getWeekMark_shouldReturnWeekMark_whenInputDate() {
            // Arrange
            Date date = new GregorianCalendar(2022, Calendar.JUNE, 1, 9, 26).getTime();
            
            // Act
            int result = Dates.getWeekMark(date);
            
            // Assert
            assertThat(result).isEqualTo(3);
        }


        @Test
        @DisplayName("getBeforeByMonth_应该返回上月日期_当获取上月时间时")
        void getBeforeByMonth_shouldReturnLastMonthDate_whenGettingLastMonth() {
            // Act
            Date result = Dates.getBeforeByMonth();
            
            // Assert
            Month expectedMonth = LocalDateTime.now().getMonth().minus(1);
            Month actualMonth = LocalDateTime.ofInstant(result.toInstant(), ZoneId.systemDefault()).getMonth();
            assertThat(actualMonth).isEqualTo(expectedMonth);
        }

        @Test
        @DisplayName("beforNumDay_应该返回前后数天时间_当指定天数时")
        void beforNumDay_shouldReturnBeforeAfterDaysTime_whenSpecifyingDays() {
            // Act
            long actual = Dates.beforNumDay(1654046770000L, 5);
            long actual1 = Dates.beforNumDay(1654046770000L, -5);
            
            // Assert
            assertThat(actual).isEqualTo(Dates.parseToTimeMillis("2022-06-06 09:26:10") / 1000);
            assertThat(actual1).isEqualTo(Dates.parseToTimeMillis("2022-05-27 09:26:10") / 1000);
        }

        @Test
        @DisplayName("getLastDayOfMonth_应该返回月末天数_当指定日期时")
        void getLastDayOfMonth_shouldReturnLastDayOfMonth_whenSpecifiedDate() {
            // Arrange
            Date date = new GregorianCalendar(2022, Calendar.JUNE, 1).getTime();
            
            // Act
            int result = Dates.getLastDayOfMonth(date);
            
            // Assert
            assertThat(result).isEqualTo(30);
        }

        @Test
        @DisplayName("getLastDayOfMonth_应该返回当月末天数_当不指定日期时")
        void getLastDayOfMonth_shouldReturnCurrentMonthLastDay_whenNoDateSpecified() {
            // Act
            int result = Dates.getLastDayOfMonth();
            
            // Assert
            int expected = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("getEpochSecond_应该返回时间戳_当输入LocalDate时")
        void getEpochSecond_shouldReturnTimestamp_whenInputLocalDate() {
            // Act
            long result = Dates.getEpochSecond(LocalDate.of(2022, Month.JUNE, 1));
            
            // Assert
            assertThat(result).isEqualTo(1654012800L);
        }

        @Test
        @DisplayName("getEpochSecond_应该返回时间戳_当输入LocalDateTime时")
        void getEpochSecond_shouldReturnTimestamp_whenInputLocalDateTime() {
            // Act
            long result = Dates.getEpochSecond(LocalDateTime.of(2022, Month.JUNE, 1, 0, 0, 0));
            
            // Assert
            assertThat(result).isEqualTo(1654012800L);
        }

        @Test
        @DisplayName("dateToLocalDateTime_应该转换Date为LocalDateTime_当输入Date对象时")
        void dateToLocalDateTime_shouldConvertDateToLocalDateTime_whenInputDate() {
            // Arrange
            Date date = new GregorianCalendar(2022, Calendar.JUNE, 1, 9, 26, 52).getTime();
            
            // Act
            LocalDateTime result = Dates.dateToLocalDateTime(date);
            
            // Assert
            assertThat(result).isEqualTo(LocalDateTime.of(2022, Month.JUNE, 1, 9, 26, 52));
        }
    }

    @Nested
    @DisplayName("倒计时和统计测试")
    class CountdownAndStatisticsTests {

        @Test
        @DisplayName("countDown_应该返回倒计时数组_当输入未来时间戳时")
        void countDown_shouldReturnCountdownArray_whenInputFutureTimestamp() {
            // Act & Assert
            assertThat(Dates.countDown(Dates.now() - 60)).containsExactly(0L, 0L, 0L, 0L);
            assertThatThrownBy(() -> Dates.countDown(Dates.now() * 10))
                .isInstanceOf(IllegalArgumentException.class);
            assertThat(Dates.countDown(Dates.now() + 60)).containsExactly(0L, 0L, 1L, 0L);
            assertThat(Dates.countDown(Dates.now() + 12)).containsExactly(0L, 0L, 0L, 12L);
            assertThat(Dates.countDown(Dates.now() + 3610)).containsExactly(0L, 1L, 0L, 10L);
            assertThat(Dates.countDown(Dates.now() + 86475)).containsExactly(1L, 0L, 1L, 15L);
        }

        @Test
        @DisplayName("endDown_应该返回结束倒计时_当指定开始和结束时间时")
        void endDown_shouldReturnEndCountdown_whenSpecifyingStartAndEndTime() {
            // Act
            long[] result = Dates.endDown(Dates.now(), Dates.now() + 60);
            
            // Assert
            assertThat(result).containsExactly(0L, 0L, 1L, 0L);
        }

        @Test
        @DisplayName("endOfToday_应该返回今天结束剩余秒数_当获取今日结束时间时")
        void endOfToday_shouldReturnSecondsToEndOfToday_whenGettingEndOfToday() {
            // Act
            long result = Dates.endOfToday();
            
            // Assert
            long expected = (Dates.maximumTimeMillisOfToday() - System.currentTimeMillis()) / 1000;
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("minimumTimeMillisOfToday_应该返回今天开始时间戳_当获取今日开始时间时")
        void minimumTimeMillisOfToday_shouldReturnStartOfTodayTimestamp_whenGettingStartOfToday() {
            // Act
            long result = Dates.minimumTimeMillisOfToday();
            
            // Assert
            long expected = Dates.parseToTimeMillis(Dates.formatNow("yyyy-MM-dd"));
            assertThat(result).isEqualTo(expected);
        }



        @Test
        @DisplayName("endOfDate_应该返回指定日期的末尾时间_当输入日期字符串时")
        void endOfDate_shouldReturnEndOfSpecifiedDate_whenInputDateString() {
            // Act
            long result = Dates.endOfDate("2022-06-01");
            
            // Assert
            long expected = new GregorianCalendar(2022, Calendar.JUNE, 1, 23, 59, 59).getTime().getTime() / 1000;
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("beginOfDate_应该返回指定日期的开始时间_当输入日期字符串时")
        void beginOfDate_shouldReturnBeginOfSpecifiedDate_whenInputDateString() {
            // Act
            long result = Dates.beginOfDate("2022-06-01");
            
            // Assert
            long expected = new GregorianCalendar(2022, Calendar.JUNE, 1, 0, 0, 0).getTime().getTime() / 1000;
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("日期列表和特殊判断测试")
    class DateListAndSpecialTests {

        @Test
        @DisplayName("getMonthDateList_应该返回月日期列表_当输入指定日期时")
        void getMonthDateList_shouldReturnMonthDateList_whenInputSpecifiedDate() {
            // Act
            List<Date> result = Dates.getMonthDateList(GregorianCalendar.getInstance().getTime());
            
            // Assert
            assertThat(result.size()).isGreaterThanOrEqualTo(Dates.getLastDayOfMonth());
        }

        @Test
        @DisplayName("getCurrentMonthDateList_应该返回当月日期列表_当输入指定日期时")
        void getCurrentMonthDateList_shouldReturnCurrentMonthDateList_whenInputSpecifiedDate() {
            // Act
            List<Date> result = Dates.getCurrentMonthDateList(GregorianCalendar.getInstance().getTime());
            
            // Assert
            assertThat(result).hasSize(Dates.getLastDayOfMonth());
        }

        @Test
        @DisplayName("isFirstDayOfMonth_应该正确判断是否为月初_当输入不同日期时")
        void isFirstDayOfMonth_shouldCorrectlyDetermineFirstDay_whenInputDifferentDates() {
            // Act & Assert
            assertThat(Dates.isFirstDayOfMonth(new GregorianCalendar(2022, Calendar.JUNE, 1).getTime().getTime()))
                .isTrue();
            assertThat(Dates.isFirstDayOfMonth(new GregorianCalendar(2022, Calendar.JUNE, 10).getTime().getTime()))
                .isFalse();
        }
    }

    @Nested
    @DisplayName("可读时间格式测试")
    class HumanReadableTimeTests {





        @Test
        @DisplayName("humanReadableSecord_应该转换秒数为可读格式_当输入不同秒数时")
        void humanReadableSecord_shouldConvertSecondsToReadableFormat_whenInputDifferentSeconds() {
            // Act & Assert
            assertThat(Dates.humanReadableSecord(60)).isEqualTo("1m");
            assertThat(Dates.humanReadableSecord(1)).isEqualTo("1s");
            assertThat(Dates.humanReadableSecord(7200)).isEqualTo("2h");
            assertThat(Dates.humanReadableSecord(7201)).isEqualTo("2h1s");
            assertThat(Dates.humanReadableSecord(7271)).isEqualTo("2h1m11s");
            assertThat(Dates.humanReadableSecord(720071)).isEqualTo("8d8h1m11s");
        }

        @Test
        @DisplayName("humanReadableMillis_应该转换毫秒数为可读格式_当输入不同毫秒数时")
        void humanReadableMillis_shouldConvertMillisToReadableFormat_whenInputDifferentMillis() {
            // Act & Assert
            assertThat(Dates.humanReadableMillis(60)).isEqualTo("0.06s");
            assertThat(Dates.humanReadableMillis(611)).isEqualTo("0.611s");
            assertThat(Dates.humanReadableMillis(1000)).isEqualTo("1s");
            assertThat(Dates.humanReadableMillis(71000)).isEqualTo("1m11s");
            assertThat(Dates.humanReadableMillis(7200960)).isEqualTo("2h1s");
            assertThat(Dates.humanReadableMillis(7201000)).isEqualTo("2h1s");
            assertThat(Dates.humanReadableMillis(7271000)).isEqualTo("2h1m11s");
            assertThat(Dates.humanReadableMillis(720071000)).isEqualTo("8d8h1m11s");
            assertThat(Dates.humanReadableMillis(720071050)).isEqualTo("8d8h1m12s");
        }
        @Test
        @DisplayName("humanReadableMillis_应该返回2h_当输入2小时毫秒数时")
        void humanReadableMillis_shouldReturn2h_whenInput2HoursInMillis() {
            // Act & Assert
            assertThat(Dates.humanReadableMillis(7200000)).isEqualTo("2h");
        }

        @Test
        @DisplayName("humanReadableDuration_应该转换Duration为可读格式_当输入不同时间间隔时")
        void humanReadableDuration_shouldConvertDurationToReadableFormat_whenInputDifferentDurations() {
            // Act & Assert
            assertThat(Dates.humanReadableDuration(Duration.ofMillis(230))).isEqualTo("0.23s");
            assertThat(Dates.humanReadableDuration(Duration.ofSeconds(230))).isEqualTo("3m50s");
            assertThat(Dates.humanReadableDuration(Duration.ofMinutes(61))).isEqualTo("1h1m");
            assertThat(Dates.humanReadableDuration(Duration.ofHours(25))).isEqualTo("1d1h");
            assertThat(Dates.humanReadableDuration(Duration.ofDays(230))).isEqualTo("230d");
        }
    }

    @Nested
    @DisplayName("时间间隔计算测试")
    class TimeIntervalTests {

        @Test
        @DisplayName("between_应该返回0_当两个时间相同时")
        void between_shouldReturnZero_whenSameTimes() {
            // Arrange
            long time = System.currentTimeMillis();
            
            // Act
            long days = Dates.between(ChronoUnit.DAYS, time, time);
            
            // Assert
            assertThat(days).isEqualTo(0);
        }

        @Test
        @DisplayName("between_应该返回1_当两个时间相差一天时")
        void between_shouldReturnOne_whenOneDayApart() {
            // Arrange
            long time1 = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long time2 = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            
            // Act
            long days = Dates.between(ChronoUnit.DAYS, time1, time2);
            
            // Assert
            assertThat(days).isEqualTo(1);
        }

        @Test
        @DisplayName("between_应该返回绝对值_当第一个时间在前时")
        void between_shouldReturnAbsoluteValue_whenFirstTimeIsEarlier() {
            // Arrange
            long time1 = LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long time2 = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            
            // Act
            long days = Dates.between(ChronoUnit.DAYS, time1, time2);
            
            // Assert
            assertThat(days).isEqualTo(1);
        }

        @Test
        @DisplayName("between_应该正确计算大时间间隔_当时间间隔很大时")
        void between_shouldCalculateLargeTimeDifference_whenTimeDifferenceIsLarge() {
            // Arrange
            long time1 = Instant.parse("2000-01-01T00:00:00Z").toEpochMilli();
            long time2 = Instant.parse("2022-01-01T00:00:00Z").toEpochMilli();
            
            // Act
            long days = Dates.between(ChronoUnit.DAYS, time1, time2);
            
            // Assert
            assertThat(days).isEqualTo(8036);
        }

        @Test
        @DisplayName("between_应该正确计算各种时间单位_当使用不同时间单位时")
        void between_shouldCalculateCorrectlyForAllTimeUnits_whenUsingDifferentTimeUnits() {
            // Arrange
            long time1 = 1635734400000L; // 2021-11-01T00:00:00Z
            long time2 = 1638326400000L; // 2021-12-01T00:00:00Z

            // Act
            long actualDays = Dates.between(ChronoUnit.DAYS, time1, time2);
            long actualMonths = Dates.between(ChronoUnit.MONTHS, time1, time2);
            long actualYears = Dates.between(ChronoUnit.YEARS, time1, time2);
            long actualHours = Dates.between(ChronoUnit.HOURS, time1, time2);
            long actualMinutes = Dates.between(ChronoUnit.MINUTES, time1, time2);
            long actualSeconds = Dates.between(ChronoUnit.SECONDS, time1, time2);

            // Assert
            assertThat(actualDays).isEqualTo(30);
            assertThat(actualMonths).isEqualTo(1);
            assertThat(actualYears).isEqualTo(0);
            assertThat(actualHours).isEqualTo(720);
            assertThat(actualMinutes).isEqualTo(43200);
            assertThat(actualSeconds).isEqualTo(2592000);
        }
    }
}

