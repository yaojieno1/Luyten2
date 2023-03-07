package us.deathmarine.luyten;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间工具类
 *
 * @author ruoyi
 */
public class DateUtils {
    private static final Logger LOGGER = new Logger();

    public static String YYYY = "yyyy";

    public static String YYYY_MM = "yyyy-MM";

    public static String YYYY_MM_DD = "yyyy-MM-dd";

    public static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static String YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss:SSS";

    public static String YYYYMMDD = "yyyyMMdd";

    public static String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static String YYYYMMDDHHMMSSSSS = "yyyyMMddHHmmssSSS";


    /**
     * 获取当前Date型日期
     *
     * @return Date() 当前日期
     */
    public static Date getNowDate() {
        return new Date();
    }

    /**
     * 获得年份
     *
     * @return 年份
     */
    public static String getYear() {
        return dateTimeNow(YYYY);
    }

    /**
     * 获得年份月份
     *
     * @return 年份月份格式为yyyy-MM
     */
    public static String getYearMonth() {
        return dateTimeNow(YYYY_MM);
    }

    /**
     * 获取当前日期, 默认格式为yyyy-MM-dd
     *
     * @return String
     */
    public static String getDate() {
        return dateTimeNow(YYYY_MM_DD);
    }

    /**
     * 获得当前时间
     *
     * @return 当前时间，格式为yyyy-MM-dd HH:mm:ss
     */
    public static String getTime() {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 获得当前时间
     *
     * @return 当前时间，格式为yyyy-MM-dd HH:mm:ss:SSS
     */
    public static String getAccurateTime() {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS_SSS);
    }

    /**
     * 获得当前时间
     *
     * @return 当前时间，格式为yyyyMMdd
     */
    public static String getCompactDate() {
        return dateTimeNow(YYYYMMDD);
    }

    /**
     * 获得当前时间
     *
     * @return 当前时间，格式为yyyyMMddHHmmss
     */
    public static String getCompactTime() {
        return dateTimeNow(YYYYMMDDHHMMSS);
    }

    /**
     * 获得当前时间
     *
     * @return 当前时间，格式为yyyyMMddHHmmssSSS
     */
    public static String getCompactAccurateTime() {
        return dateTimeNow(YYYYMMDDHHMMSSSSS);
    }

    /**
     * 格式化当前日期
     *
     * @param format 格式
     * @return 格式化后的当前日期
     */
    public static String dateTimeNow(final String format) {
        return parseDateToStr(format, new Date());
    }

    /**
     * 格式化日期
     *
     * @param date 日期date
     * @return yyyy-MM-dd格式化后的日期
     */
    public static String dateTime(final Date date) {
        return parseDateToStr(YYYY_MM_DD, date);
    }

    /**
     * 转换日期
     *
     * @param format 格式
     * @param date   日期Date
     * @return 格式化后的日期
     */
    public static String parseDateToStr(final String format, final Date date) {
        if (!ObjectUtils.isEmpty(date)) {
            return new SimpleDateFormat(format).format(date);
        }
        return "";
    }

    /**
     * 转化日期字符串为Date
     *
     * @param format 格式
     * @param ts     日期字符串
     * @return 转化后的Date
     */
    public static Date dateTime(final String format, final String ts) {
        if (!StringUtils.isBlank(ts)) {
            try {
                return new SimpleDateFormat(format).parse(ts);
            } catch (ParseException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * 获取开始时间
     *
     * @param startTime 开始日期
     * @return 开始时间Date
     * @author DurantSimpson
     */
    public static Date getStartTime(final String startTime) {
        try {
            return new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS).parse(startTime + " 00:00:00");
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取结束时间
     *
     * @param endTime 结束日期
     * @return 结束时间Date
     * @author DurantSimpson
     */
    public static Date getEndTime(final String endTime) {
        try {
            return new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS).parse(endTime + " 23:59:59");
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 日期路径 即年/月/日 如2018/08/08
     */
    public static String datePath() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyy/MM/dd");
    }

    /**
     * 日期路径 即年/月/日 如20180808
     */
    public static String dateTime() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyyMMdd");
    }

    /**
     * 获取时间差
     *
     * @param time1 时间1
     * @param time2 时间1
     * @return 时间差
     */
    public static String getDistanceTime(long time1, long time2) {
        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        long diff;

        if (time1 < time2) {
            diff = time2 - time1;
        } else {
            diff = time1 - time2;
        }
        day = diff / (24 * 60 * 60 * 1000);
        hour = (diff / (60 * 60 * 1000) - day * 24);
        min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
        sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        if (day != 0) {
            return day + "天" + hour + "小时" + min + "分钟" + sec + "秒";
        }
        if (hour != 0) {
            return hour + "小时" + min + "分钟" + sec + "秒";
        }
        if (min != 0) {
            return min + "分钟" + sec + "秒";
        }
        if (sec != 0) {
            return sec + "秒";
        }
        return "0秒";
    }

    /**
     * 描述:获取上个月的最后一天.
     *
     * @return 上个月的最后一天
     */
    public static String getLastMaxMonthDate() {
        SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return dft.format(calendar.getTime());
    }

    /**
     * 描述:获取前1天的时间
     *
     * @return 前1天的时间
     */
    public static Date getBefore1() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }

    /**
     * 描述:获取前2天的时间
     *
     * @return 前2天的时间
     */
    public static Date getBefore2() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -2);
        return calendar.getTime();
    }

    /**
     * 描述:获取前3天的时间
     *
     * @return 前3天的时间
     */
    public static Date getBefore3() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -3);
        return calendar.getTime();
    }

    /**
     * 描述:获取后1天的时间
     *
     * @return 取后1天的时间
     */
    public static Date getAfter1() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }

    /**
     * 描述:获取后2天的时间
     *
     * @return 后2天的时间
     */
    public static Date getAfter2() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 2);
        return calendar.getTime();
    }

    /**
     * 描述:获取后3天的时间
     *
     * @return 后3天的时间
     */
    public static Date getAfter3() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 3);
        return calendar.getTime();
    }

    /**
     * 描述:获取上个月月份
     *
     * @return 上个月月份
     */
    public static String getLastMonthDate() {
        SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return dft.format(calendar.getTime());
    }


    /**
     * 描述:获取指定上一个月份的最后一天.
     *
     * @return 上一个月份的最后一天
     */
    public static String getLastMaxPointMonthDate(Date date) {
        SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return dft.format(calendar.getTime());
    }

    /**
     * 描述:获取指定日期上一个月的月份
     *
     * @return 指定日期上一个月的月份
     */
    public static String getLastMonth(Date date) {
        SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return dft.format(calendar.getTime());
    }

    /**
     * 获得指定日期的前一天
     *
     * @param specifiedDay 指定日期
     * @return 指定日期的前一天
     */
    public static String getSpecifiedDayBefore(String specifiedDay) {
        Calendar c = getSpecifiedDayCalendar(specifiedDay);
        if (c == null) {
            return null;
        }
        int day = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day - 1);
        return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
    }

    /**
     * 获得指定日期的后一天
     *
     * @param specifiedDay 指定日期
     * @return 指定日期的后一天
     */
    public static String getSpecifiedDayAfter(String specifiedDay) {
        Calendar c = getSpecifiedDayCalendar(specifiedDay);
        if (c == null) {
            return null;
        }
        int day = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day + 1);
        return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
    }

    private static Calendar getSpecifiedDayCalendar(String specifiedDay) {
        Calendar c = Calendar.getInstance();
        Date date;
        try {
            date = new SimpleDateFormat("yy-MM-dd").parse(specifiedDay);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
        c.setTime(date);
        return c;
    }

    /**
     * 获取当前时间的时间戳
     *
     * @return 当前时间的时间戳
     */
    public static String dateTimestamp() {
        Date date = new Date();
        return String.valueOf(date.getTime());
    }
}
