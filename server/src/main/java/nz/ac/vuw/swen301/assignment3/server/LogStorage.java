package nz.ac.vuw.swen301.assignment3.server;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class LogStorage {
    public enum Level {ALL, TRACE, DEBUG, INFO, WARN, ERROR, FATAL,OFF}


    private static  ArrayList<LogEvent> logEvents = new ArrayList<>();


    public static void addLog(LogEvent e){
        logEvents.add(e);
    }

    public static ArrayList<LogEvent> getLogs(int limit, String level){

        int levelVal = Level.valueOf(level).ordinal();
        ArrayList<LogEvent> le = new ArrayList<>();

        int count =0;
        for(int i=logEvents.size()-1;i>=0;i--) {
            LogEvent e = logEvents.get(i);
            if(e.level.ordinal()>=levelVal){
                le.add(e);
                count++;
            }
            if(count==limit){
                return le;
            }
        }
        return le;
    }

    public static void clear(){
        logEvents.clear();
    }

    public static boolean hasID(String id){
        for(LogEvent e: logEvents){
            if(e.id.equals(id)){
                return true;
            }
        }
        return false;
    }


    // FOLLOWING METHODS ARE FOR CREATING THE EXCEL FILE

    public static XSSFWorkbook  getCSV(){
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("logStats");

        ArrayList<Date> dates = getDates();
        //Create header Array -> Date   DATE1   DATE2   ...
        String[] header = new String[dates.size()+1];
        header[0] = "Date";

        //formats the Date nicer DD/M/YYYY
        for(int i=1;i<=dates.size();i++){
            header[i]=getYMD(dates.get(i-1));
        }

        int rowCount =0;
        int colCount =0;
        //adds the header to the sheet
        Row row = sheet.createRow(rowCount++);
        for(String head : header){
            Cell cell = row.createCell(colCount++);
            cell.setCellValue(head);
        }

        //adds the body to the sheet
        String[][] body = getBody();
        for(String[] x : body){
            row = sheet.createRow(rowCount++);
            colCount=0;
            for(String y : x){
                Cell cell = row.createCell(colCount++);
                cell.setCellValue(y);
            }
        }
        return workbook;
    }

    private static String[][]  getBody(){
        ArrayList<Date> dates = getDates();
        ArrayList<String> loggers = getLoggers();
        ArrayList<String> threads = getThreads();

        int size = Level.values().length;
        int[][] levelCount = new int[size][dates.size()];
        int[][] loggerCount = new int[loggers.size()][dates.size()];
        int[][] threadCount = new int[threads.size()][dates.size()];

        for(LogEvent e: logEvents){
            for(int i=0; i<dates.size();i++) {
                if (getDate(e.timeStamp).equals(dates.get(i))) {
                    levelCount[0][i]++;
                    levelCount[e.level.ordinal()][i]++;
                    loggerCount[loggers.indexOf(e.logger)][i]++;
                    threadCount[threads.indexOf(e.thread)][i]++;
                }

            }

        }
        String[][] body = new String[size+loggers.size()+threads.size()][dates.size()+1];

        int rowCount = 0;
        for(int i=0;i<levelCount.length;i++){
            body[i][0] = Level.values()[i].toString();
            for(int j=0;j<levelCount[0].length;j++){
            body[i][j+1] = String.valueOf(levelCount[i][j]);
            }
            rowCount++;
        }

        for(int i=0;i<loggerCount.length;i++){
            body[rowCount][0] = "Logger: "+loggers.get(i);
            for(int j=0;j<loggerCount[0].length;j++){
                body[rowCount][j+1] = String.valueOf(loggerCount[i][j]);
            }
            rowCount++;
        }

        for(int i=0;i<threadCount.length;i++){
            body[rowCount][0] = "Thread: "+threads.get(i);
            for(int j=0;j<threadCount[0].length;j++){
                body[rowCount][j+1] = String.valueOf(threadCount[i][j]);
            }
            rowCount++;
        }

        return body;
    }


    private static ArrayList<String> getLoggers() {
        ArrayList<String> loggers = new ArrayList<>();
        for (LogEvent e : logEvents) {
            if(!loggers.contains(e.logger)){
                loggers.add(e.logger);
            }
        }
        return loggers;
    }

    private static ArrayList<String> getThreads() {
        ArrayList<String> threads = new ArrayList<>();
        for (LogEvent e : logEvents) {
            if(!threads.contains(e.thread)){
                threads.add(e.thread);
            }
        }
        return threads;
    }

    private static ArrayList<Date> getDates() {
        //Filter Dates
        HashSet<Date> allDates = new HashSet<>();
        for (LogEvent e : logEvents) {
            allDates.add(getDate(e.timeStamp));
        }
        //Order the dates
        ArrayList<Date> orderedDates = new ArrayList<>(allDates);
        Collections.sort(orderedDates, new Comparator<Date>() {
            public int compare(Date o1, Date o2) {
                return o1.compareTo(o2);
            }
        });

        return orderedDates;
    }


    private static Date getDate(String d){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String[] split = d.split("T")[0].split("-");
            Date day = sdf.parse(split[0] + "-" + split[1] + "-" + split[2]);
            return day;
        }catch(ParseException exception){
            exception.printStackTrace();
            return null;
        }

    }

    //GETS Year Month and Day of DATE
    private static String getYMD(Date d){
        String[] split= d.toString().split(" ");
        return split[2]+" "+split[1]+" "+split[5];
    }





}
