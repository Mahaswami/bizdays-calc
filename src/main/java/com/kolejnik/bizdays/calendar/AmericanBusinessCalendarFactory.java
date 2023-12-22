package com.kolejnik.bizdays.calendar;

import com.kolejnik.bizdays.holiday.CronHoliday;
import com.kolejnik.bizdays.holiday.FixedYearlyHoliday;
import com.kolejnik.bizdays.holiday.Holiday;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

public class AmericanBusinessCalendarFactory implements BusinessCalendarFactory {

    private static BusinessCalendar businessCalendar;

    public static final Holiday LUTER_KING_BDAY = new CronHoliday("* * * ? JAN MON#3 *");
    public static final Holiday WASHINGTON_BDAY = new CronHoliday("* * * ? FEB MON#3 *");
    public static final Holiday MEMORIAL_DAY = new CronHoliday("* * * ? MAY 2L *");
    public static final Holiday LABOR_DAY = new CronHoliday("* * * ? SEP MON#1 *");
    public static final Holiday COLUMBUS_DAY = new CronHoliday("* * * ? OCT MON#2 *");
    public static final Holiday THANKSGIVING = new CronHoliday("* * * ? NOV THU#4 *");

    private static Map<String, Month> map = new HashMap<>();

    @Override
    public BusinessCalendar getInstance() {
        if (businessCalendar == null) {
            businessCalendar = new BusinessCalendar();

            businessCalendar.addHoliday(CronHoliday.SATURDAY);
            businessCalendar.addHoliday(CronHoliday.SUNDAY);

            businessCalendar.addHoliday(FixedYearlyHoliday.VETERANS_DAY);
            businessCalendar.addHoliday(FixedYearlyHoliday.INDEPENDENCE_DAY);
            businessCalendar.addHoliday(FixedYearlyHoliday.NEW_YEAR);
            businessCalendar.addHoliday(FixedYearlyHoliday.CHRISTMAS);
            businessCalendar.addHoliday(FixedYearlyHoliday.JUNETEENTH_NATIONAL_INDEPENDENCE_DAY);

            businessCalendar.addHoliday(LUTER_KING_BDAY);
            businessCalendar.addHoliday(WASHINGTON_BDAY);
            businessCalendar.addHoliday(MEMORIAL_DAY);
            businessCalendar.addHoliday(LABOR_DAY);
            businessCalendar.addHoliday(COLUMBUS_DAY);
            businessCalendar.addHoliday(THANKSGIVING);
            //NOTE: client as asked to remove this holiday on 20/11/2020
            //businessCalendar.addHoliday(FRIDAY_AFTER_THANKSGIVING);
            addConfigureHolidays(businessCalendar);
        }
        return businessCalendar;
    }

    private void addConfigureHolidays(BusinessCalendar businessCalendar) {
        Reader fileReader = null;
        try {
            File file = new File("./src/main/resources/HolidaysList.csv");
            if(file.exists()) {
                fileReader = new BufferedReader(new FileReader(file.getAbsolutePath()));
                Iterable<CSVRecord> csvRecords = CSVFormat.EXCEL.withHeader().withIgnoreEmptyLines(true).parse(fileReader);
                for (CSVRecord csvRecord : csvRecords) {
                    String monthName = csvRecord.get("Month");
                    String day = csvRecord.get("Day Of Month");
                    int dayOfMonth = 0;
                    if (day != null) {
                        dayOfMonth = Integer.parseInt(day);
                    }
                    if (!(monthName == null || monthName.isEmpty())) {
                        Month month = map.get(monthName.toLowerCase());
                        if (month != null && dayOfMonth > 0)
                            businessCalendar.addHoliday(new FixedYearlyHoliday(month, dayOfMonth));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("unable to add configured Holidays....");
            e.printStackTrace();
        }
    }


    static {
        map.put("january", Month.JANUARY);map.put("february", Month.FEBRUARY);
        map.put("march", Month.MARCH);map.put("april", Month.APRIL);
        map.put("may", Month.MAY);map.put("june", Month.JUNE);
        map.put("july", Month.JULY);map.put("august", Month.AUGUST);
        map.put("september", Month.SEPTEMBER);map.put("october", Month.OCTOBER);
        map.put("november", Month.NOVEMBER);map.put("december", Month.DECEMBER);

    }
}
