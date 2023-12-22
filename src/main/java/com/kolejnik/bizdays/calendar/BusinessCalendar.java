package com.kolejnik.bizdays.calendar;

import com.kolejnik.bizdays.BusinessDayCalculator;
import com.kolejnik.bizdays.InvalidHolidayException;
import com.kolejnik.bizdays.holiday.CronHoliday;
import com.kolejnik.bizdays.holiday.Holiday;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class BusinessCalendar implements BusinessDayCalculator {

    // TODO: parametrize it
    /*
     * 4 years. If there is no holiday in MAX_BUSINESS_DAYS_BLOCK days
     * there must be something wrong with the calendar.
     */
    private final static int MAX_BUSINESS_DAYS_BLOCK = 1461;

    private Set<Holiday> holidays;

    @Override
    public boolean isBusinessDay(LocalDate date) {
        return !isHoliday(date);
    }

    @Override
    public boolean isHoliday(LocalDate date) {
        for (Holiday holiday : holidays) {
            if (holiday.isHoliday(date)) {
                return true;
            }

            LocalDate oneDayBefore = date.minusDays(1);
            if ((oneDayBefore.getDayOfWeek()).toString().equalsIgnoreCase("SUNDAY") && isHolidayFallsOnWeekEnd(oneDayBefore)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public LocalDate nextBusinessDay() {
        return businessDayAfter(LocalDate.now());
    }

    @Override
    public LocalDate businessDayAfter(LocalDate date) {
        return nextBusinessDay(date, 1);
    }

    @Override
    public LocalDate businessDayBefore(LocalDate date) {
        return nextBusinessDay(date, -1);
    }

    private LocalDate nextBusinessDay(LocalDate date, int step) {
        int i = 0;
        do {
            date = date.plusDays(step);
            if (++i > MAX_BUSINESS_DAYS_BLOCK) {
                throw new InvalidHolidayException("No holiday found in "
                        + MAX_BUSINESS_DAYS_BLOCK + " days");
            }
        } while (!isBusinessDay(date));
        return date;
    }

    @Override
    public int businessDaysBetween(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            return businessDaysBetween(to, from);
        }
        LocalDate date = from, endDate = to.plusDays(1);
        int businessDaysCount = 0;
        while (date.isBefore(endDate)) {
            if (isBusinessDay(date)) {
                businessDaysCount++;
            }
            date = date.plusDays(1);
        }
        return businessDaysCount;
    }

    @Override
    public LocalDate plus(LocalDate date, int businessDaysCount) {
        LocalDate endDate = date;
        int step = businessDaysCount >= 0 ? 1 : -1;
        while (businessDaysCount != 0) {
            endDate = nextBusinessDay(endDate, step);
            businessDaysCount -= step;
        }
        return endDate;
    }

    @Override
    public LocalDate minus(LocalDate date, int businessDaysCount) {
        return plus(date, -businessDaysCount);
    }

    public boolean addHoliday(Holiday holiday) {
        if (holidays == null) {
            holidays = new HashSet<>();
        }
        return holidays.add(holiday);
    }

    public boolean removeHoliday(Holiday holiday) {
        if (holidays == null) {
            return false;
        }
        return holidays.remove(holiday);
    }

    public Set<Holiday> getHolidays() {
        return holidays;
    }

    public void setHolidays(Set<Holiday> holidays) {
        this.holidays = holidays;
    }

    public boolean isHolidayFallsOnWeekEnd(LocalDate date){
        Set<Holiday> holidaysCopy=  holidays.stream().filter(holiday -> !checkIsCronHoliday(holiday)).collect(Collectors.toSet());
       for(Holiday holiday: holidaysCopy){
          if(holiday.isHoliday(date))
              return Boolean.TRUE;
       }
       return Boolean.FALSE;
    }

    //cron holiday like saturday,sunday
    private boolean checkIsCronHoliday(Holiday holiday){
        return ((holiday instanceof CronHoliday) && (((CronHoliday) holiday).getCronExpression().toString().equalsIgnoreCase("* * * ? * SAT *") ||
                ((CronHoliday) holiday).getCronExpression().toString().equalsIgnoreCase("* * * ? * SUN *")));
    }

    public String getConfigureHolidayName(LocalDate date, String filePath) {
        Iterable<CSVRecord> csvRecords = null;
        try {
            File file = new File(filePath);
            if(file.exists()) {
                BufferedReader fileReader = new BufferedReader(new FileReader(filePath));
                csvRecords = CSVFormat.EXCEL.withHeader().withIgnoreEmptyLines(true).parse(fileReader);
                String month = String.valueOf(date.getMonth());
                int dayOfMonth = date.getDayOfMonth();
                for (CSVRecord csvRecord : csvRecords) {
                    String day = csvRecord.get("Day Of Month");
                    if (day == null || day.isEmpty())
                        continue;
                    if (month.equalsIgnoreCase(csvRecord.get("Month")) && dayOfMonth == Integer.parseInt(day))
                        return csvRecord.get("Name of the Holiday");
                }
            }
        } catch (IOException e) {
            System.out.println("unable to add configured Holidays....");
            e.printStackTrace();
        }

        return null;
    }

    public String getConfigureHolidayName(LocalDate date) {
        return getConfigureHolidayName(date, "./src/main/resources/HolidaysList.csv");
    }
}
