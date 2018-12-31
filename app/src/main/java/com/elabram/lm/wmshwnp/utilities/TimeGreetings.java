package com.elabram.lm.wmshwnp.utilities;

import java.util.Calendar;
import java.util.Date;

public class TimeGreetings {

    public static Date dateNow() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    // Morning
    private static Date startMorning()  {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 5);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    private static Date endMorning()  {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    // Day
    private static Date startDay()  {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 1);
        return calendar.getTime();
    }

    private static Date endDay()  {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 16);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    // Afternoon
    private static Date startAfternoon()  {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 16);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 1);
        return calendar.getTime();
    }

    private static Date endAfternoon()  {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    // Evening
    private static Date startEvening()  {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 1);
        return calendar.getTime();
    }

    private static Date endEvening()  {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    // Evening 1
    private static Date startEvening1()  {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 1);
        return calendar.getTime();
    }

    private static Date endEvening1()  {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 4);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }

    public static boolean isMorning(Date date) {
        return !date.before(startMorning()) && !date.after(endMorning());
    }

    public static boolean isDay(Date date) {
        return !date.before(startDay()) && !date.after(endDay());
    }

    public static boolean isAfternoon(Date date) {
        return !date.before(startAfternoon()) && !date.after(endAfternoon());
    }

    public static boolean isEvening(Date date) {
        return !date.before(startEvening()) && !date.after(endEvening());
    }

    public static boolean isEvening1(Date date) {
        return !date.before(startEvening1()) && !date.after(endEvening1());
    }

    // Early
    private static Date startEarly()  {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 5);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    private static Date endEarly()  {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 20);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    public static boolean isEarly(Date date) {
        return !date.before(startEarly()) && !date.after(endEarly());
    }

    // Ontime
    private static Date startOntime()  {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 20);
        calendar.set(Calendar.SECOND, 1);
        return calendar.getTime();
    }

    private static Date endOntime()  {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    public static boolean isOntime(Date date) {
        return !date.before(startOntime()) && !date.after(endOntime());
    }

    // Late 1
    private static Date startLate()  {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 1);
        return calendar.getTime();
    }

    private static Date endLate()  {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }

    public static boolean isLate1(Date date) {
        return !date.before(startLate()) && !date.after(endLate());
    }

    // Late 2
    private static Date startLate1()  {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 1);
        return calendar.getTime();
    }

    private static Date endLate1()  {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 4);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }

    static boolean isLate2(Date date) {
        return !date.before(startLate1()) && !date.after(endLate1());
    }


//    static boolean isBirthday(Date date) {
//        return date.equals(birthDate);
//    }
}
