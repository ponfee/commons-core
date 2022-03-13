package code.ponfee.commons.util;

import com.google.common.base.Preconditions;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;

import java.util.Date;

import static code.ponfee.commons.util.WrappedFastDateFormat.PATTERN14;

/**
 * 1990-04-15 00:00:00这天调整了夏令时，即在4月15号0点的时候将表调快了一小时，导致这一天少了一小时。
 * 
 * 1986年4月，中国中央有关部门发出“在全国范围内实行夏时制的通知”，具体作法是：每年从四月中旬第一个星
 * 期日的凌晨2时整（北京时间），将时钟拨快一小时，即将表针由2时拨至3时，夏令时开始；到九月中旬第一个
 * 星期日的凌晨2时整（北京夏令时），再将时钟拨回一小时，即将表针由2时拨至1时，夏令时结束。从1986年到
 * 1991年的六个年度，除1986年因是实行夏时制的第一年，从5月4日开始到9月14日结束外，其它年份均按规定的
 * 时段施行。在夏令时开始和结束前几天，新闻媒体均刊登有关部门的通告。1992年起，夏令时暂停实行。
 * 
 * 时间周期，计算周期性的时间段
 * @author Ponfee
 */
public enum DatePeriods {

    HOURLY() {
        @Override
        Interval compute(LocalDateTime original, LocalDateTime target, int step, int next) {
            Period period = new Period(original, target, PeriodType.hours());
            LocalDateTime begin = original.plusHours(calc(period.getHours(), step, next));
            return new Interval(begin, begin.plusHours(step));
        }
    },

    DAILY() {
        @Override
        Interval compute(LocalDateTime original, LocalDateTime target, int step, int next) {
            Period period = new Period(original, target, PeriodType.days());
            LocalDateTime begin = original.plusDays(calc(period.getDays(), step, next));
            return new Interval(begin, begin.plusDays(step));
        }
    },

    WEEKLY() {
        @Override
        Interval compute(LocalDateTime original, LocalDateTime target, int step, int next) {
            Period period = new Period(original, target, PeriodType.weeks());
            LocalDateTime begin = original.plusWeeks(calc(period.getWeeks(), step, next));
            return new Interval(begin, begin.plusWeeks(step));
        }
    },

    MONTHLY() {
        @Override
        Interval compute(LocalDateTime original, LocalDateTime target, int step, int next) {
            Period period = new Period(original, target, PeriodType.months());
            LocalDateTime begin = original.plusMonths(calc(period.getMonths(), step, next));
            return new Interval(begin, begin.plusMonths(step));
        }
    },

    QUARTERLY() {
        @Override
        Interval compute(LocalDateTime original, LocalDateTime target, int step, int next) {
            return MONTHLY.next(original, target, step * 3, next);
        }
    },

    SEMIANNUAL() {
        @Override
        Interval compute(LocalDateTime original, LocalDateTime target, int step, int next) {
            return MONTHLY.next(original, target, step * 6, next);
        }
    },

    ANNUAL() {
        @Override
        Interval compute(LocalDateTime original, LocalDateTime target, int step, int next) {
            Period period = new Period(original, target, PeriodType.years());
            LocalDateTime begin = original.plusYears(calc(period.getYears(), step, next));
            return new Interval(begin, begin.plusYears(step));
        }
    };

    // 2018-01-01: the first day of year, month, week
    private static final LocalDateTime ORIGINAL = DateTimeFormat
        .forPattern("yyyy-MM-dd HH:mm:ss.SSS")
        .parseLocalDateTime("2018-01-01 00:00:00.000");

    /**
     * Template method pattern
     * 
     * compute the next interval
     * 
     * @param original
     * @param target
     * @param step
     * @param next
     * @return
     */
    public final Interval next(LocalDateTime original, LocalDateTime target, int step, int next) {
        Preconditions.checkArgument(step > 0, "Step must be positive number.");
        Preconditions.checkArgument(!original.isAfter(target), "Original cannot after target date.");

        // original.withTime(original.getHourOfDay(), 0, 0, 0)
        return this.compute(
            original/*.withMillisOfDay(0)*/,
            target/*.withMillisOfDay(0)*/, 
            step, next
        );
    }

    /**
     * Compute the next period based original and reference target
     *
     * @param original the period original
     * @param target   the target of next reference
     * @param step     the period step
     * @param next     the next of target period
     * @return Interval{begin, end}
     */
    abstract Interval compute(LocalDateTime original, LocalDateTime target, 
                              int step, int next);

    public final Interval next(Date original, Date target, int step, int next) {
        return next(new LocalDateTime(original), new LocalDateTime(target), step, next);
    }

    public final Interval next(Date target, int step, int next) {
        return next(ORIGINAL, new LocalDateTime(target), step, next);
    }

    public final Interval next(Date target, int next) {
        return next(ORIGINAL, new LocalDateTime(target), 1, next);
    }

    private static int calc(int qty, int step, int next) {
        return (qty / step + next) * step;
    }

    public static final class Interval {
        private final Date begin;
        private final Date end;

        private Interval(LocalDateTime begin, LocalDateTime end) {
            this.begin = begin.toDate();
            this.end = end.minusMillis(1).toDate();
        }

        public Date begin() {
            return begin;
        }

        public Date end() {
            return end;
        }

        @Override
        public String toString() {
            return PATTERN14.format(begin) + " ~ " + PATTERN14.format(end);
        }
    }

}
