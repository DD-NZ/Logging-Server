package nz.ac.vuw.swen301.assignment3.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.*;



import java.net.URI;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.*;

public class BlackBoxTests {

    private static URI logURI;
    private static URI statURI;

    private ObjectMapper m = new ObjectMapper();
    private RandomLogCreator creator = new RandomLogCreator();

    @BeforeClass
    public static void startServer() throws Exception {
        Runtime.getRuntime().exec("mvn jetty:run");
        //Runtime.getRuntime().exec(new String[]{"cmd.exe", "/C", "mvn", "jetty:run"} );
        Thread.sleep(10000);


        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost("localhost").setPort(8080).setPath("/resthome4logs/logs");
        logURI = builder.build();

        URIBuilder builder2 = new URIBuilder();
        builder2.setScheme("http").setHost("localhost").setPort(8080).setPath("/resthome4logs/stats");
        statURI = builder2.build();
    }

    @AfterClass
    public static void stopServer() throws Exception {
        Runtime.getRuntime().exec("mvn jetty:stop");
        //Runtime.getRuntime().exec(new String[]{"cmd.exe", "/C", "mvn", "jetty:stop"} );
        Thread.sleep(10000);
    }

    //before each test, test if the server is running
    @Before
    public  void testServerIsRunning() throws Exception {
        Assume.assumeTrue(isServerRunning());
    }

    @Test
    public void logDoGetCorrectCode() throws Exception {
        LogEvent[] event = creator.createLoggingEvents(100);
        String JSON = m.writerWithDefaultPrettyPrinter().writeValueAsString(event);
        HttpResponse postResponse = post(logURI,JSON);
        assertEquals(postResponse.getStatusLine().getStatusCode(),200);
        //Tests all correct LEVEL inputs
        String[] levels = new String[]{"ALL","TRACE","DEBUG","INFO","WARN","FATAL","OFF"};
        for(String s: levels){
            HttpResponse getResponse = get(getURI(s,"1"));
            assertEquals(getResponse.getStatusLine().getStatusCode(),200);
        }
        //test OK upper bound of limit parameter
        HttpResponse getResponse = get(getURI("ALL","50"));
        assertEquals(getResponse.getStatusLine().getStatusCode(),200);

        //test OK lower bound of limit parameter
        getResponse = get(getURI("ALL","0"));
        assertEquals(getResponse.getStatusLine().getStatusCode(),200);

    }

    @Test
    public void logDoGetbadLimitCode() throws Exception {
        //test out of bound lower limit
        HttpResponse getResponse = get(getURI("ALL","-1"));
        assertEquals(getResponse.getStatusLine().getStatusCode(),400);

        //test out of bound upper
        getResponse = get(getURI("ALL","51"));
        assertEquals(getResponse.getStatusLine().getStatusCode(),400);
    }

    //test non correct level
    @Test
    public void logDoGetbadLevelCode() throws Exception {
        HttpResponse getResponse = get(getURI("Banana","1"));
        assertEquals(getResponse.getStatusLine().getStatusCode(),400);
    }

    @Test
    public void logDoGetContentType()throws Exception {
        LogEvent[] event = creator.createLoggingEvents(1);
        String JSON = m.writerWithDefaultPrettyPrinter().writeValueAsString(event);
        HttpResponse postResponse = post(logURI,JSON);
        assertEquals(postResponse.getStatusLine().getStatusCode(),200);

        HttpResponse getResponse = get(getURI("ALL","1"));
        assertTrue(getResponse.getFirstHeader("Content-Type").toString().contains("application/json;"));

    }
    //tests that the values of the Level filter work.
    @Test
    public void logDoGetContentValues()throws Exception {
        //send 50  logs to server
        LogEvent[] sendEvents = creator.createLoggingEvents(50);
        String JSON = m.writerWithDefaultPrettyPrinter().writeValueAsString(sendEvents);
        HttpResponse postResponse = post(logURI,JSON);
        assertEquals(postResponse.getStatusLine().getStatusCode(),200);

        ArrayList<Integer> sizes = new ArrayList<Integer>();
        String[] levels = new String[]{"ALL","TRACE","DEBUG","INFO","WARN","FATAL","OFF"};
        //tests all level values to see that the doGet filters the level correctly
        for(String s: levels){
            HttpResponse getResponse = get(getURI(s,"1"));
            assertEquals(getResponse.getStatusLine().getStatusCode(),200);
            String entity = EntityUtils.toString(getResponse.getEntity());
            LogEvent[] events = m.readValue(entity, LogEvent[].class);

            int allSize = events.length;

            //Checks that all logevents are greater than or equal to the set level
            for (LogEvent e : events) {
                assertTrue(e.level.compareTo(LogStorage.Level.valueOf(s)) >= 0);
            }
            sizes.add(events.length);
        }
        //number of logs returned should be ALL >= TRACE >= DEBUG >= INFO ...
        Integer last = Integer.MAX_VALUE;
        for(Integer i: sizes){
            assertTrue(last>=i);
            last = i;
        }

    }

    @Test
    public void logDoGetContentValues2()throws Exception {
        LogEvent[] sendEvents = creator.createLoggingEvents(10);
        String JSON = m.writerWithDefaultPrettyPrinter().writeValueAsString(sendEvents);
        HttpResponse postResponse = post(logURI,JSON);
        assertEquals(postResponse.getStatusLine().getStatusCode(),200);

        HttpResponse getResponse = get(getURI("ALL","10"));
        String entity = EntityUtils.toString(getResponse.getEntity());
        LogEvent[] getEvents = m.readValue(entity, LogEvent[].class);
        //The storage works as a stack, first in last out, so the get Event will be on the opposite index to the send event
        for(int i=0;i<10;i++){
            assertEquals(sendEvents[i].id,getEvents[getEvents.length-(i+1)].id);
            assertEquals(sendEvents[i].id,getEvents[getEvents.length-(i+1)].id);
            assertEquals(sendEvents[i].message,getEvents[getEvents.length-(i+1)].message);
            assertEquals(sendEvents[i].logger,getEvents[getEvents.length-(i+1)].logger);
            assertEquals(sendEvents[i].thread,getEvents[getEvents.length-(i+1)].thread);
            assertEquals(sendEvents[i].timeStamp,getEvents[getEvents.length-(i+1)].timeStamp);
            assertEquals(sendEvents[i].errorDetails,getEvents[getEvents.length-(i+1)].errorDetails);
            assertEquals(sendEvents[i].level,getEvents[getEvents.length-(i+1)].level);

        }
    }

    @Test
    public void statsDoGetContentType()throws Exception {
        LogEvent[] event = creator.createLoggingEvents(1);
        String JSON = m.writerWithDefaultPrettyPrinter().writeValueAsString(event);
        HttpResponse postResponse = post(logURI,JSON);
        assertEquals(postResponse.getStatusLine().getStatusCode(),200);

        HttpResponse getResponse = get(statURI);
        assertEquals(getResponse.getStatusLine().getStatusCode(),200);
        assertTrue(getResponse.getFirstHeader("Content-Type").toString().contains("application/vnd.ms-excel"));

    }

    @Test
    public void statsDoGetContentValues()throws Exception {
        //reset server to clear logs for accurate stats
        resetServer();
        String date1 = "2019-06-01T00:00:00.000+1200";
        String date2 = "2019-06-02T000:00:00.000+1200";
        String date3 = "2019-06-03T00:00:00.000+1200";
        String date4 = "2019-06-04T00:00:00.000+1200";
        String[] dates = new String[]{date1,date2,date3,date4};
        LogEvent[] events = new LogEvent[24];
        int count =0;
        //adds 6 logs per different date
        for(String d: dates){
            events[count++] = new LogEvent(UUID.randomUUID().toString(),"fill",d,"thread3", "logger1","TRACE","none");
            events[count++] = new LogEvent(UUID.randomUUID().toString(),"fill",d,"thread3", "logger1","DEBUG","none");
            events[count++] = new LogEvent(UUID.randomUUID().toString(),"fill",d,"thread2", "logger2","INFO","none");
            events[count++] = new LogEvent(UUID.randomUUID().toString(),"fill",d,"thread2", "logger2","WARN","none");
            events[count++] = new LogEvent(UUID.randomUUID().toString(),"fill",d,"thread1", "logger3","ERROR","none");
            events[count++] = new LogEvent(UUID.randomUUID().toString(),"fill",d,"thread1", "logger3","FATAL","none");

        }

        //sends logs over to the server
        String JSON = m.writerWithDefaultPrettyPrinter().writeValueAsString(events);
        HttpResponse postResponse = post(logURI,JSON);
        assertEquals(postResponse.getStatusLine().getStatusCode(),200);

        //get the stats of the server back
        HttpResponse getResponse = get(statURI);
        assertEquals(getResponse.getStatusLine().getStatusCode(),200);

        XSSFWorkbook workbook = new XSSFWorkbook(getResponse.getEntity().getContent());
        XSSFSheet sheet = workbook.getSheetAt(0);

        //These should be the correct order of the first column of the rows
        String[]  rows = new String[]{"Date","ALL","TRACE","DEBUG","INFO","WARN","ERROR","FATAL","OFF","Logger: logger1","Logger: logger2","Logger: logger3","Thread: thread3","Thread: thread2","Thread: thread1"};

        //iterates through each row of the sheet
        for(int i=0;i<sheet.getLastRowNum();i++){
            Row row = sheet.getRow(i);
            //Checks row heading is in the correct order as above.
            assertEquals(row.getCell(0).toString(), rows[i]);
            //first row is the dates
            if(i==0){
                assertEquals(row.getCell(1).toString(), "01 Jun 2019");
                assertEquals(row.getCell(2).toString(), "02 Jun 2019");
                assertEquals(row.getCell(3).toString(), "03 Jun 2019");
                assertEquals(row.getCell(4).toString(), "04 Jun 2019");
            }
            //Next rows are all the counts of the logs
            else {
                String expectedNum = "";
                //This is the ALL level log, there should be a total of 6 logs per date
                if(i==1){
                    expectedNum = "6";
                }
                //rows 2-7 should be all the other loging levels, there should be 1 log, per level, per date
                else if(i<8){
                    expectedNum = "1";
                }
                //rows 8 should be the OFF level, and no logs were logged at this level
                else if(i==8){
                    expectedNum = "0";
                }
                //rows 9-14 are the logger names, and threads, each logger AND thread, got two logs per day.
                else{
                    expectedNum = "2";
                }
                //assert that each day got the expected number of logs :)
                for (int j = 1; j <= 4; j++) {
                    assertEquals(row.getCell(1).toString(), expectedNum);
                }
            }
        }
    }

    @Test
    public void doPostCorrectCode() throws Exception {
        //test 1 log
        LogEvent[] event = creator.createLoggingEvents(1);
        String JSON = m.writerWithDefaultPrettyPrinter().writeValueAsString(event);
        HttpResponse response = post(logURI,JSON);
        assertEquals(response.getStatusLine().getStatusCode(),200);

        //test 15 logs
        event = creator.createLoggingEvents(15);
        JSON = m.writerWithDefaultPrettyPrinter().writeValueAsString(event);
        response = post(logURI,JSON);
        assertEquals(response.getStatusLine().getStatusCode(),200);
    }

    @Test
    public void doPostBadInputCodes() throws Exception {
        HttpResponse response = post(logURI,"THIS is not a logging event");
        assertEquals(response.getStatusLine().getStatusCode(),400);

    }

    //Test sending two of the same ID Codes together
    @Test
    public void doPostDoubleIDCode() throws Exception {
        LogEvent[] event = creator.createLoggingEvents(1);
        LogEvent[] doubleUp = new LogEvent[]{event[0],event[0]};
        String JSON = m.writerWithDefaultPrettyPrinter().writeValueAsString(doubleUp);

        HttpResponse response2 = post(logURI,JSON);
        assertEquals(response2.getStatusLine().getStatusCode(),409);

    }

    //Test sending two of the same ID Codes seperatly
    @Test
    public void doPostDoubleIDCode2() throws Exception {
        LogEvent[] event = creator.createLoggingEvents(1);
        String JSON = m.writerWithDefaultPrettyPrinter().writeValueAsString(event);

        HttpResponse response = post(logURI,JSON);
        assertEquals(response.getStatusLine().getStatusCode(),200);

        HttpResponse response2 = post(logURI,JSON);
        assertEquals(response2.getStatusLine().getStatusCode(),409);
    }








    private URI getURI(String level, String limit) throws Exception{
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost("localhost").setPort(8080).setPath("/resthome4logs/logs")
                .setParameter("level",level).setParameter("limit",limit);
        return builder.build();
    }


    private HttpResponse get(URI uri) throws Exception {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(uri);
        return httpClient.execute(request);
    }

    private HttpResponse post(URI uri, String JSON) throws Exception {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(uri);
        request.setEntity(new StringEntity(JSON));
        return httpClient.execute(request);
    }

    private void resetServer() throws Exception{
        Runtime.getRuntime().exec("mvn jetty:stop");
        //Runtime.getRuntime().exec(new String[]{"cmd.exe", "/C", "mvn", "jetty:stop"} );
        Thread.sleep(10000);
        Runtime.getRuntime().exec("mvn jetty:run");
        //Runtime.getRuntime().exec(new String[]{"cmd.exe", "/C", "mvn", "jetty:run"} );
        Thread.sleep(10000);
    }


    private static boolean isServerRunning() throws Exception {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost("localhost").setPort(8080).setPath("/resthome4logs");
        URI uri = builder.build();
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(uri);
            HttpResponse response =  httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                return true;
            }
            return false;
        }
        catch (Exception e) {
            return false;
        }
    }
}
