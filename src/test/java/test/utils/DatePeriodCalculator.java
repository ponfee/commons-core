package test.utils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.time.FastDateFormat;
import org.joda.time.LocalDateTime;

import code.ponfee.commons.util.DatePeriods;
import code.ponfee.commons.util.DatePeriods.Interval;
import code.ponfee.commons.util.Dates;

/**
 * 周期计算
 * @author Ponfee
 */
public class DatePeriodCalculator {

    private static final Date STARTING_DATE = 
        Dates.toDate("1990-01-01 13:00:00", "yyyy-MM-dd HH:mm:ss");

    private final Date starting; // 最开始的周期（起点）时间
    private final Date target; // 待计算时间
    private final Periods period; // 周期类型

    public DatePeriodCalculator(Date target, Periods period) {
        this(STARTING_DATE, target, period);
    }

    public DatePeriodCalculator(Date starting, Date target, Periods period) {
        this.starting = Dates.startOfDay(starting);
        this.target = Dates.endOfDay(target);
        this.period = period;
    }

    /**
     * @param quantity 周期数量
     * @param next     目标周期的下next个周期
     * @return
     */
    public Date[] calculate(int quantity, int next) {
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be positive number");
        }
        if (starting.after(target)) {
            throw new IllegalArgumentException("starting cannot after target date");
        }

        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(starting);
        c2.setTime(target);
        Date startDate = null;
        int cycleNum, year;
        Calendar tmp;
        float days;
        switch (period) {
            case WEEKLY:
                quantity *= 7;
            case DAILY:
                days = c2.get(Calendar.DAY_OF_YEAR) - c1.get(Calendar.DAY_OF_YEAR); // 间隔天数
                year = c2.get(Calendar.YEAR);
                tmp = (Calendar) c1.clone();
                while (tmp.get(Calendar.YEAR) != year) {
                    days += tmp.getActualMaximum(Calendar.DAY_OF_YEAR);// 得到当年的实际天数
                    tmp.add(Calendar.YEAR, 1);
                }
                cycleNum = (int) Math.floor(days / quantity) + next; // 上一个周期
                c1.add(Calendar.DAY_OF_YEAR, cycleNum * quantity);
                startDate = c1.getTime();
                c1.add(Calendar.DAY_OF_YEAR, quantity);
                break;
            case QUARTERLY: // 季度
            case HALF_YEARLY: // 半年度
            case YEARLY: // 年度
            case MONTHLY: // 月
                switch (period) {
                    case QUARTERLY: // 季度
                        quantity *= 3;
                        break;
                    case HALF_YEARLY: // 半年度
                        quantity *= 6;
                        break;
                    case YEARLY:
                        quantity *= 12; // 年度
                        break;
                    default: // MONTHLY
                }
                // 间隔月数
                int intervalMonth = (c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR)) * 12 + c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH);
                cycleNum = (int) Math.floor(intervalMonth / quantity);
                tmp = (Calendar) c1.clone();
                // 跨月问题，当前时间仍属于该周期内，则应减一个周期数，如：(2012-01-15 ~ 2012-02-14，当前时间为2012-02-14，则当前时间属于该周期，而不是下一周期)
                tmp.add(Calendar.MONTH, cycleNum * quantity);
                if (tmp.after(c2)) {
                    cycleNum -= 1;
                }
                cycleNum += next; // 上一个周期
                c1.add(Calendar.MONTH, cycleNum * quantity);
                startDate = c1.getTime(); // 本周期开始时间
                c1.add(Calendar.MONTH, quantity); // 本周期结束时间
                break;
            default:
                throw new IllegalArgumentException("invalid period type");
        }
        c1.add(Calendar.MILLISECOND, -1);
        return new Date[] { startDate, c1.getTime() };
    }

    public Date[] calculate(int next) {
        return calculate(1, next);
    }

    public enum Periods {
        DAILY("每日"), // 
        WEEKLY("每周"), // 
        MONTHLY("每月"), // 
        QUARTERLY("每季度"), // 
        HALF_YEARLY("每半年"), // 
        YEARLY("每年");

        private final String desc;

        Periods(String desc) {
            this.desc = desc;
        }

        String desc() {
            return this.desc;
        }
    }

    private static final FastDateFormat FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss SSS");
    public static void test1() throws ParseException {
        System.out.println(FORMAT.format(new LocalDateTime(1990, 04, 15, 00, 00, 00, 000).toDate()));
        System.out.println(FORMAT.format(FORMAT.parse("1990-04-15 00:00:00 000")));

        for (int i = 0; i < 100000; i++) {
            int step = ThreadLocalRandom.current().nextInt(37) + 1;
            int next = -47 + ThreadLocalRandom.current().nextInt(94);
            Date target = Dates.random(Dates.ofMillis(0), Dates.plusSeconds(STARTING_DATE, 10));
            String except, actual;

            except = except(Periods.DAILY, target, step, next);
            actual = DatePeriods.DAILY.next(STARTING_DATE, target, step, next).toString();
            print("DAILY FAIL!", except, actual, step);

            except = except(Periods.WEEKLY, target, step, next);
            actual = DatePeriods.WEEKLY.next(STARTING_DATE, target, step, next).toString();
            print("WEEKLY FAIL!", except, actual, step);

            except = except(Periods.MONTHLY, target, step, next);
            actual = DatePeriods.MONTHLY.next(STARTING_DATE, target, step, next).toString();
            print("MONTHLY FAIL!", except, actual, step);

            except = except(Periods.QUARTERLY, target, step, next);
            actual = DatePeriods.QUARTERLY.next(STARTING_DATE, target, step, next).toString();
            print("QUARTERLY FAIL!", except, actual, step);

            except = except(Periods.HALF_YEARLY, target, step, next);
            actual = DatePeriods.SEMIANNUAL.next(STARTING_DATE, target, step, next).toString();
            print("SEMIANNUAL FAIL!", except, actual, step);

            except = except(Periods.YEARLY, target, step, next);
            actual = DatePeriods.ANNUAL.next(STARTING_DATE, target, step, next).toString();
            print("ANNUAL FAIL!", except, actual, step);
        }
    }

    private static String except(Periods p, Date target, int step, int next) {
        Date[] dates = new DatePeriodCalculator(target, p).calculate(step, next);
        return FORMAT.format(dates[0]) + " ~ " + FORMAT.format(dates[1]);
    }

    private static void print(String msg, String except, String actual, int step) {
        boolean flag = true;
        if (!flag)  {
            return;
        }
        if (!except.equals(actual)) {
            System.err.println(msg + ", step: " + step);
            System.err.println("except: " + except);
            System.err.println("actual: " + actual);
            System.err.println();
        } else if (!except.contains("00:00:00 000")) {
            System.err.println(except);
        } else {
            //System.out.println(except);
        }
    }
    
    private static void test2() {
        Date target = Dates.random(Dates.ofMillis(0), STARTING_DATE);
        System.out.println(Dates.format(target));
        for (int i = 0; i < 100; i++) {
            Interval interval = DatePeriods.HOURLY.next(STARTING_DATE, target, 2, 1);
            System.out.println(interval.toString());
            target = interval.begin();
        }
    }
    
    public static void test3() throws ParseException {
        Date original = Dates.toDate("2018-03-01 15:00:00");
        Date target = Dates.toDate("2018-05-01 14:00:00");
        for (int i = 0; i < 10; i++) {
            Interval in = DatePeriods.MONTHLY.next(original, target, 1, 1);
            target = in.begin();
            System.out.println(in.toString());
        }
    }
    
    public static void main(String[] args) throws ParseException {
        //test1();
        test2();
        //test3();

        /*for (int i = 0; i < 999999; i++) {
            Date d = Dates.random();
            if (!Dates.format(new LocalDateTime(d).toDate()).equals(Dates.format(new LocalDateTime(d).millisOfDay().getLocalDateTime().toDate())))
            System.err.println("err");
        }*/
    }
}
