package de.avanux.smartapplianceenabler;

import de.avanux.smartapplianceenabler.appliance.Appliance;
import de.avanux.smartapplianceenabler.schedule.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract public class TestBase {

    protected LocalDateTime toYesterday(Integer hour, Integer minute, Integer second) {
        return toDay(-1, hour, minute, second);
    }

    protected LocalDateTime toToday(Integer hour, Integer minute) {
        return toDay(0, hour, minute, 0);
    }

    protected LocalDateTime toToday(Integer hour, Integer minute, Integer second) {
        return toDay(0, hour, minute, second);
    }

    protected LocalDateTime toDay(Integer dayOffset, Integer hour, Integer minute, Integer second) {
        return LocalDate.now().atTime(new TimeOfDay(hour, minute, second).toLocalTime()).plusDays(dayOffset);
    }

    protected LocalDateTime toTomorrow(Integer hour, Integer minute, Integer second) {
        return toDay(1, hour, minute, second);
    }

    protected LocalDateTime toDayAfterTomorrow(Integer hour, Integer minute, Integer second) {
        return toDay(2, hour, minute, second);
    }

    protected LocalDateTime toDayOfWeek(int dayOfWeek, Integer hour, Integer minute) {
        return toDayOfWeek(LocalDateTime.now(), dayOfWeek, hour, minute, 0);
    }

    protected LocalDateTime toDayOfWeek(int dayOfWeek, Integer hour, Integer minute, Integer second) {
        return toDayOfWeek(LocalDateTime.now(), dayOfWeek, hour, minute, second);
    }

    protected LocalDateTime toDayOfWeek(LocalDateTime now, int dayOfWeek, Integer hour, Integer minute, Integer second) {
        TimeOfDayOfWeek timeOfDayOfWeek = new TimeOfDayOfWeek(dayOfWeek, hour, minute, second);
        return timeOfDayOfWeek.toNextOccurrence(now);
    }

    protected Interval toIntervalToday(Integer startHour, Integer startMinutes, Integer endHour, Integer endMinutes) {
        return toIntervalToday(startHour, startMinutes, 0, endHour, endMinutes, 0);
    }

    protected Interval toIntervalToday(Integer startHour, Integer startMinutes, Integer startSeconds,
                                       Integer endHour, Integer endMinutes, Integer endSeconds) {
        return new Interval(toToday(startHour, startMinutes, startSeconds),
                toToday(endHour, endMinutes, endSeconds));
    }

    protected Interval toIntervalTomorrow(Integer startHour, Integer startMinutes, Integer startSeconds,
                                       Integer endHour, Integer endMinutes, Integer endSeconds) {
        return new Interval(toTomorrow(startHour, startMinutes, startSeconds),
                toTomorrow(endHour, endMinutes, endSeconds));
    }

    protected Interval toIntervalDayAfterTomorrow(Integer startHour, Integer startMinutes, Integer startSeconds,
                                          Integer endHour, Integer endMinutes, Integer endSeconds) {
        return new Interval(toDayAfterTomorrow(startHour, startMinutes, startSeconds),
                toDayAfterTomorrow(endHour, endMinutes, endSeconds));
    }

    protected Interval toInterval(Integer startDayOffset, Integer startHour, Integer startMinutes,
                                  Integer endDayOffset, Integer endHour, Integer endMinutes) {
        return toInterval(startDayOffset, startHour, startMinutes, 0,
                endDayOffset, endHour, endMinutes, 0);
    }

    protected Interval toInterval(Integer startDayOffset, Integer startHour, Integer startMinutes, Integer startSeconds,
                                  Integer endDayOffset, Integer endHour, Integer endMinutes, Integer endSeconds) {
        return new Interval(toDay(startDayOffset, startHour, startMinutes, startSeconds),
                toDay(endDayOffset, endHour, endMinutes, endSeconds));
    }

    protected Interval toIntervalByDow(LocalDateTime now, Integer startDow, Integer startHour, Integer startMinutes,
                                       Integer endDow, Integer endHour, Integer endMinutes) {
        return toIntervalByDow(now, startDow, startHour, startMinutes, 0, endDow, endHour, endMinutes, 0);
    }

    protected Interval toIntervalByDow(LocalDateTime now, Integer startDow, Integer startHour, Integer startMinutes, Integer startSeconds,
                                  Integer endDow, Integer endHour, Integer endMinutes, Integer endSeconds) {
        return new Interval(toDayOfWeek(now, startDow, startHour, startMinutes, startSeconds),
                toDayOfWeek(now, endDow, endHour, endMinutes, endSeconds));
    }

    protected int toSecondsFromNow(LocalDateTime now, int dayOffset, Integer hour, Integer minutes, Integer seconds) {
        return Long.valueOf(Duration.between(now, toDay(dayOffset, hour, minutes, seconds)).toSeconds()).intValue();
    }

    protected void assertDateTime(LocalDateTime dt1, LocalDateTime dt2) {
        assertEquals(dt1, dt2);
    }

    protected void assertTimeframeIntervalRuntime(Interval interval,
                                                         TimeframeIntervalState state,
                                                         Integer min,
                                                         int max,
                                                         boolean enabled,
                                                         TimeframeInterval actual) {
        RuntimeRequest request = new RuntimeRequest(min, max);
        request.setEnabled(enabled);
        TimeframeInterval expected = new TimeframeInterval(interval, request);
        expected.initState(state);
        assertEquals(expected, actual);
    }

    protected void assertTimeframeIntervalOptionalEnergy(Interval interval,
                                                         TimeframeIntervalState state,
                                                         Integer socInitial,
                                                         Integer socRequested,
                                                         Integer evId,
                                                         Integer energy,
                                                         boolean enabled,
                                                         TimeframeInterval actual) {
        OptionalEnergySocRequest request = new OptionalEnergySocRequest(evId);
        request.setEnabled(enabled);
        request.setSocInitial(socInitial);
        request.setSoc(socRequested);
        TimeframeInterval expected = new TimeframeInterval(interval, request);
        expected.initState(state);
        assertEquals(expected, actual);
    }

    protected void assertTimeframeIntervalSocRequest(TimeframeIntervalState state,
                                                     Interval interval,
                                                     Integer socInitial,
                                                     Integer soc,
                                                     Integer evId,
                                                     Integer energy,
                                                     boolean enabled,
                                                     TimeframeInterval actual) {
        SocRequest request = new SocRequest(soc, evId);
        request.setSocInitial(socInitial);
        request.setEnabled(enabled);
        assertEquals(new TimeframeInterval(state, interval, request), actual);
    }

    protected void tick(Appliance appliance, LocalDateTime now) {
        TimeframeIntervalHandler timeframeIntervalHandler = appliance.getTimeframeIntervalHandler();
        timeframeIntervalHandler.updateQueue(now, false);
    }

    protected Schedule buildScheduleWithRequest() {
        Schedule schedule = new Schedule();
        schedule.setRequest(new RuntimeRequest());
        return schedule;
    }
}
