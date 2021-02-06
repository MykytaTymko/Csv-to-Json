package com.mycompany.test;

/**
 *
 * @author Mykyta Tymko
 */
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Test {

    static final String CSV_FILENAME = "src\\main\\resources\\pf4x-4Q2020.csv";
    static final String JSON_FILENAME = "src\\main\\resources\\pf4x-4Q2020.json";

    public static void main(String[] args) throws IOException, ParseException, Exception {
        Map pracaCelkom = new TreeMap<Calendar, Float>(); //id 1
        Map pracaTarif1 = new TreeMap<Calendar, Float>(); //id 2
        Map pracaTarif2 = new TreeMap<Calendar, Float>(); //id 3
        Map hodnotaPruduL1 = new HashMap<Calendar, Float>(); //id 31
        Map hodnotaPruduL2 = new HashMap<Calendar, Float>(); //id 51
        Map hodnotaPruduL3 = new HashMap<Calendar, Float>(); //id 71
        Map hodnotaNapatiaL1 = new HashMap<Calendar, Float>(); //id 32
        Map hodnotaNapatiaL2 = new HashMap<Calendar, Float>(); //id 52
        Map hodnotaNapatiaL3 = new HashMap<Calendar, Float>(); //id 72

        try (ICsvBeanReader beanReader = new CsvBeanReader(new FileReader(CSV_FILENAME), CsvPreference.STANDARD_PREFERENCE)) {
            final String[] headers = beanReader.getHeader(true);
            final CellProcessor[] processors = getProcessors();
            Data data;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date;
            Calendar calendar;
            while ((data = beanReader.read(Data.class, headers, processors)) != null) {
                if (data.getId() == 0) {
                    continue;
                }
                String strDate = data.getDate();
                date = formatter.parse(strDate);
                calendar = Calendar.getInstance();
                calendar.setTime(date);
                if (data.getValidFlag() != 0) {
                    switch (data.getId()) {
                        case 1 ->
                            pracaCelkom.put(calendar, (float) data.getValue());
                        case 2 ->
                            pracaTarif1.put(calendar, (float) data.getValue());
                        case 3 ->
                            pracaTarif2.put(calendar, (float) data.getValue());
                        case 31 -> {
                            if (data.getValue() <= 30) {
                                hodnotaPruduL1.put(calendar, (float) data.getValue());
                            }
                        }
                        case 51 -> {
                            if (data.getValue() <= 30) {
                                hodnotaPruduL2.put(calendar, (float) data.getValue());
                            }
                        }
                        case 71 -> {
                            if (data.getValue() <= 30) {
                                hodnotaPruduL3.put(calendar, (float) data.getValue());
                            }
                        }
                        case 32 -> {
                            if (data.getValue() <= 280 && (float) data.getValue() >= 180) {
                                hodnotaNapatiaL1.put(calendar, (float) data.getValue());
                            }
                        }
                        case 52 -> {
                            if (data.getValue() <= 280 && data.getValue() >= 180) {
                                hodnotaNapatiaL2.put(calendar, (float) data.getValue());
                            }
                        }
                        case 72 -> {
                            if (data.getValue() <= 280 && data.getValue() >= 180) {
                                hodnotaNapatiaL3.put(calendar, (float) data.getValue());
                            }
                        }
                    }
                }
            }
        }

        //max and min value
        float maxValuePruduL1 = maxValue(hodnotaPruduL1);
        float maxValuePruduL2 = maxValue(hodnotaPruduL2);
        float maxValuePruduL3 = maxValue(hodnotaPruduL3);

        float maxValueNapatieL1 = maxValue(hodnotaNapatiaL1);
        float maxValueNapatieL2 = maxValue(hodnotaNapatiaL1);
        float maxValueNapatieL3 = maxValue(hodnotaNapatiaL1);

        float minValueNapatieL1 = minValue(hodnotaNapatiaL1);
        float minValueNapatieL2 = minValue(hodnotaNapatiaL1);
        float minValueNapatieL3 = minValue(hodnotaNapatiaL1);

        //value for every hour  
        TreeMap<Date, Float> valueForEachNewHourPracaCelkom = valueForEachNewHour(pracaCelkom);
        TreeMap<Date, Float> valueForEachNewHourTarif1 = valueForEachNewHour(pracaTarif1);
        TreeMap<Date, Float> valueForEachNewHourTarif2 = valueForEachNewHour(pracaTarif2);

        //difference in values for each hour
        TreeMap<Date, Float> differenceValueHodPracaCelkom = differenceInValuesForEachHour(valueForEachNewHourPracaCelkom, pracaCelkom);
        TreeMap<Date, Float> differenceValueHodTarif1 = differenceInValuesForEachHour(valueForEachNewHourTarif1, pracaTarif1);
        TreeMap<Date, Float> differenceValueHodTarif2 = differenceInValuesForEachHour(valueForEachNewHourTarif2, pracaTarif2);

        //difference in values for each month
        TreeMap<Date, Float> differenceValueMonthPracaCelkom = differenceInValuesForEachMonth(valueForEachNewHourPracaCelkom, pracaCelkom);
        TreeMap<Date, Float> differenceValueMonthTarif1 = differenceInValuesForEachMonth(valueForEachNewHourTarif1, pracaTarif1);
        TreeMap<Date, Float> differenceValueMonthTarif2 = differenceInValuesForEachMonth(valueForEachNewHourTarif2, pracaTarif2);

        //convert to JSON
        convertToJSON(JSON_FILENAME, differenceValueHodPracaCelkom, differenceValueMonthPracaCelkom, differenceValueHodTarif1, differenceValueMonthTarif1, differenceValueHodTarif2, differenceValueMonthTarif2,
                maxValuePruduL1, maxValuePruduL2, maxValuePruduL3, maxValueNapatieL1, maxValueNapatieL2, maxValueNapatieL3, minValueNapatieL1, minValueNapatieL2, minValueNapatieL3);

    }

    private static CellProcessor[] getProcessors() {
        final CellProcessor[] processors = new CellProcessor[]{
            new NotNull(), //date
            new NotNull(new ParseInt()), //id
            new NotNull(new ParseDouble()), //value
            new Optional(new ParseInt()), //validFlag
        };
        return processors;
    }

    public static float maxValue(Map map) {
        return (float) Collections.max(map.values());
    }

    public static float minValue(Map map) {
        return (float) Collections.min(map.values());
    }

    public static TreeMap valueForEachNewHour(Map map) {
        Map First = new TreeMap<Calendar, Float>();
        Map Second = new TreeMap<Calendar, Float>();
        Map valueForEachNewHour = new TreeMap<Calendar, Float>();

        for (int month = 9; month <= 11; month++) {
            for (int day = 1; day <= 31; day++) {
                for (int hour = 0; hour <= 23; hour++) {

                    Calendar calendarStartEachHour = new GregorianCalendar();
                    calendarStartEachHour.set(Calendar.YEAR, 2020);
                    calendarStartEachHour.set(Calendar.MONTH, month);
                    calendarStartEachHour.set(Calendar.DAY_OF_MONTH, day);
                    calendarStartEachHour.set(Calendar.HOUR_OF_DAY, hour);
                    calendarStartEachHour.set(Calendar.MINUTE, 0);
                    calendarStartEachHour.set(Calendar.SECOND, 0);
                    calendarStartEachHour.set(Calendar.MILLISECOND, 0);

                    for (int minute = 0; minute <= 59; minute++) {
                        for (int second = 0; second <= 59; second++) {

                            Calendar calendarForCycle = new GregorianCalendar();
                            calendarForCycle.set(Calendar.YEAR, 2020);
                            calendarForCycle.set(Calendar.MONTH, month);
                            calendarForCycle.set(Calendar.DAY_OF_MONTH, day);
                            calendarForCycle.set(Calendar.HOUR_OF_DAY, hour);
                            calendarForCycle.set(Calendar.MINUTE, minute);
                            calendarForCycle.set(Calendar.SECOND, second);
                            calendarForCycle.set(Calendar.MILLISECOND, 0);

                            if (map.containsKey(calendarForCycle) == true) {
                                switch (hour % 2) {
                                    case 0 -> {
                                        First.put(calendarForCycle, map.get(calendarForCycle));
                                    }
                                    case 1 -> {
                                        Second.put(calendarForCycle, map.get(calendarForCycle));
                                    }
                                }
                            }
                        }
                    }
                    if (First.isEmpty() == false && Second.isEmpty() == false) {
                        switch (hour % 2) {
                            case 1 -> {
                                float a = equationResult(First, Second, calendarStartEachHour);
                                valueForEachNewHour.put(calendarStartEachHour.getTime(), a);
                                First.clear();
                            }
                            case 0 -> {
                                float a = equationResult(Second, First, calendarStartEachHour);
                                valueForEachNewHour.put(calendarStartEachHour.getTime(), a);
                                Second.clear();
                            }
                        }
                    }
                }
            }
        }
        return (TreeMap) valueForEachNewHour;
    }

    //used only in the function above (valueForEachNewHour(Map map))
    public static float equationResult(Map lowerValue, Map greatValue, Calendar calendar) {

        //we take the last value from the map, which contains the values of the "lower" hour
        Calendar lastKey = ((TreeMap<Calendar, Float>) lowerValue).lastKey();
        Float lastRecord = ((TreeMap<Calendar, Float>) lowerValue).get(lastKey);

        //we take the first value from the map, which contains the values of the "upper" hour
        Calendar firstKey = ((TreeMap<Calendar, Float>) greatValue).firstKey();
        Float firstRecord = ((TreeMap<Calendar, Float>) greatValue).get(firstKey);

        //the difference between the lower value of the "upper" hour and he upper value of the "lower" hour 
        Float difference = (float) ((firstKey.getTimeInMillis() - lastKey.getTimeInMillis()) / 1000);
        //difference between midnight of the new hour and the last value of the previous hour
        Float difference2 = (float) ((calendar.getTimeInMillis() - lastKey.getTimeInMillis()) / 1000);

        //h(tx) = h(t0) + ( ( h(t1) - h(t0) ) / ( t1 - t0 ) ) * tx
        Float result = lastRecord + ((firstRecord - lastRecord) / difference) * difference2;
        return result;

    }

    //for example: differenceInValuesForEachHour(valueForEachNewHour, pracaCelkom);
    public static TreeMap differenceInValuesForEachHour(Map needToCalculateTheValues, Map mapWithTheLastValue) {

        Object[] ob = needToCalculateTheValues.keySet().toArray(new Object[needToCalculateTheValues.size()]);
        Map differenceOfValues = new TreeMap<String, Float>();

        for (int index = 0; index < needToCalculateTheValues.size(); index++) {

            if (index == needToCalculateTheValues.size() - 1) {
                Object keyLower = ob[index];
                Float valueLower = (float) needToCalculateTheValues.get(keyLower);
                Object keyUpper = ((TreeMap<Calendar, Float>) mapWithTheLastValue).lastKey();
                Float valueUpper = ((TreeMap<Calendar, Float>) mapWithTheLastValue).get(keyUpper);
                Float difference = valueUpper - valueLower;
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String keyLowerString = (formatter.format(keyLower));
                differenceOfValues.put(keyLowerString, difference);
            } else {
                int nextIndex = index + 1;
                Object keyLower = ob[index];
                Float valueLower = (float) needToCalculateTheValues.get(keyLower);
                Object keyUpper = ob[nextIndex];
                Float valueUpper = (float) needToCalculateTheValues.get(keyUpper);
                Float difference = valueUpper - valueLower;
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String keyLowerString = (formatter.format(keyLower));
                differenceOfValues.put(keyLowerString, difference);
            }
        }
        return (TreeMap) differenceOfValues;
    }

    //for example: differenceInValuesForEachMonth(valueForEachNewHourPracaCelkom, pracaCelkom);
    public static TreeMap differenceInValuesForEachMonth(Map needToCalculateTheValues, Map mapWithLastAndFirstValue) {
        Map differenceOfValues = new TreeMap<String, Float>();

        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, 2020);
        calendar.set(Calendar.MONTH, 10);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        float november = (float) needToCalculateTheValues.get(calendar.getTime());
        calendar.set(Calendar.MONTH, 11);
        float december = (float) needToCalculateTheValues.get(calendar.getTime());

        for (int month = 10; month <= 12; month++) {
            GregorianCalendar calendarForCycle = new GregorianCalendar();
            calendarForCycle.set(Calendar.YEAR, 2020);
            calendarForCycle.set(Calendar.MONTH, month);
            calendarForCycle.set(Calendar.DAY_OF_MONTH, 1);
            calendarForCycle.set(Calendar.HOUR_OF_DAY, 0);
            calendarForCycle.set(Calendar.MINUTE, 0);
            calendarForCycle.set(Calendar.SECOND, 0);
            calendarForCycle.set(Calendar.MILLISECOND, 0);
            if (month == 10) {
                Object firstKey = ((TreeMap<Calendar, Float>) mapWithLastAndFirstValue).firstKey();
                Float firstRecord = ((TreeMap<Calendar, Float>) mapWithLastAndFirstValue).get(firstKey);
                float result = november - firstRecord;
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String calendarForCycleString = (formatter.format(calendarForCycle.getTime()));
                differenceOfValues.put(calendarForCycleString, result);
            } else if (month == 11) {
                float result = december - november;
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String calendarForCycleString = (formatter.format(calendarForCycle.getTime()));
                differenceOfValues.put(calendarForCycleString, result);
            } else if (month == 12) {
                Object lastKey = ((TreeMap<Calendar, Float>) mapWithLastAndFirstValue).lastKey();
                Float lastRecord = ((TreeMap<Calendar, Float>) mapWithLastAndFirstValue).get(lastKey);
                float result = lastRecord - december;
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String calendarForCycleString = (formatter.format(calendarForCycle.getTime()));
                differenceOfValues.put(calendarForCycleString, result);
            }
        }
        return (TreeMap) differenceOfValues;
    }

    public static void convertToJSON(String Path, Map map1, Map map2, Map map3, Map map4, Map map5, Map map6, float map7,
            float map8, float map9, float map10, float map11, float map12, float map13, float map14, float map15) throws IOException {

        Map<String, Object> map = new LinkedHashMap<>();

        map.put("hodinovky-TARIF1+TRARIF2", map1);
        map.put("mesacne-TARIF1+TRARIF2", map2);
        map.put("hodinovky-TARIF1", map3);
        map.put("mesacne-TARIF1", map4);
        map.put("hodinovky-TARIF2", map5);
        map.put("mesacne-TARIF2", map6);
        map.put("max-prud-L1", map7);
        map.put("max-prud-L2", map8);
        map.put("max-prud-L3", map9);
        map.put("max-napatie-L1", map10);
        map.put("max-napatie-L2", map11);
        map.put("max-napatie-L3", map12);
        map.put("min-napatie-L1", map13);
        map.put("min-napatie-L2", map14);
        map.put("min-napatie-L3", map15);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(Path), map);
    }

}
