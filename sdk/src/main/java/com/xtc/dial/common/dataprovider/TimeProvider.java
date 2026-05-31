package com.xtc.dial.common.dataprovider;

import com.xtc.dial.common.RenderContext;

import java.util.Calendar;

/**
 * Provides decomposed current time fields (hour, minute, day, month, week).
 *
 * Call {@link #refresh()} before accessing any getter to ensure the values
 * reflect the current system time.
 */
public class TimeProvider {

    private final RenderContext mRenderContext;

    private int hour1, hour2;
    private int minute1, minute2;
    private int month1, month2;
    private int day1, day2;
    private int week;

    public TimeProvider(RenderContext renderContext) {
        this.mRenderContext = renderContext;
    }

    /** Refresh all fields from the current system time. */
    public void refresh() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        hour1 = hour / 10;
        hour2 = hour % 10;
        minute1 = minute / 10;
        minute2 = minute % 10;
        month1 = month / 10;
        month2 = month % 10;
        day1 = day / 10;
        day2 = day % 10;
        week = calendar.get(Calendar.DAY_OF_WEEK);
        // Convert from Calendar.SUNDAY=1..SATURDAY=7 to Mon=1..Sun=7
        // Calendar: 1=Sun, 2=Mon, 3=Tue, 4=Wed, 5=Thu, 6=Fri, 7=Sat
        // We want:  1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
        week = ((week + 5) % 7) + 1;
    }

    // ── Time ──
    public int getNumHour1()    { return hour1; }
    public int getNumHour2()    { return hour2; }
    public int getMinute1()     { return minute1; }
    public int getMinute2()     { return minute2; }

    // ── Date ──
    public int getMonth1()      { return month1; }
    public int getMonth2()      { return month2; }
    public int getDay1()        { return day1; }
    public int getDay2()        { return day2; }

    // ── Week (1=Mon .. 7=Sun) ──
    public int getWeek()        { return week; }

    public void listen()        { /* no-op — no data source to subscribe to */ }
    public void cancelListen()  { /* no-op */ }
    public void release()       { /* no-op */ }
}
