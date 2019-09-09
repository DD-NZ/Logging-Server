package nz.ac.vuw.swen301.assignment3.server;


import com.fasterxml.jackson.databind.ObjectMapper;
import nz.ac.vuw.swen301.assignment3.server.LogEvent;
import nz.ac.vuw.swen301.assignment3.server.LogServlet;
import nz.ac.vuw.swen301.assignment3.server.LogStorage;
import nz.ac.vuw.swen301.assignment3.server.RandomLogCreator;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


import java.util.ArrayList;

import static org.junit.Assert.*;

public class WhiteBoxTests {
    private RandomLogCreator creator = new RandomLogCreator();
    private ObjectMapper m = new ObjectMapper();
    private LogServlet logServlet = new LogServlet();

    @Test
    public void doGetCorrectCode() {
        LogStorage.clear();
        addLogs(100,logServlet);
        //Tests all correct LEVEL inputs
        String[] levels = new String[]{"ALL","TRACE","DEBUG","INFO","WARN","FATAL","OFF"};
        for(String s: levels){
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setParameter("limit", "1");
            request.setParameter("level", s);
            logServlet.doGet(request, response);
            assertEquals(200, response.getStatus());
        }

        //test OK upper bound of limit parameter
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setParameter("limit", "50");
        request.setParameter("level", "ALL");
        logServlet.doGet(request, response);
        assertEquals(200, response.getStatus());

        //test lower bound of limit parameter
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        request.setParameter("limit", "0");
        request.setParameter("level", "ALL");
        logServlet.doGet(request, response);
        assertEquals(200, response.getStatus());




    }

    @Test
    public void doGetbadLimitCode() {
        LogStorage.clear();
        addLogs(50,logServlet);

        //test lower out of bound for limit parameter
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setParameter("limit", "-1");
        request.setParameter("level", "ALL");
        logServlet.doGet(request, response);
        assertEquals(400, response.getStatus());

        //test upper out bound for limit parameter
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        request.setParameter("limit", "51");
        request.setParameter("level", "ALL");
        logServlet.doGet(request, response);
        assertEquals(400, response.getStatus());

        //test somthing that is not a number
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        request.setParameter("limit", "banana");
        request.setParameter("level", "ALL");
        logServlet.doGet(request, response);
        assertEquals(400, response.getStatus());
    }

    @Test
    public void doGetBadLevelCode() {
        LogStorage.clear();

        //Test Level that is not  correct
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setParameter("limit", "1");
        request.setParameter("level", "Banana");
        logServlet.doGet(request, response);
        assertEquals(400, response.getStatus());

    }


    @Test
    public void doGetContentType() {
        LogStorage.clear();
        addLogs(5,logServlet);

        //test doGet has correct content type
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setParameter("limit", "1");
        request.setParameter("level", "ALL");
        logServlet.doGet(request, response);
        assertEquals("application/json", response.getContentType());

    }

    //tests that the values of the Level filter work.
    @Test
    public void doGetContentValues() {
        LogStorage.clear();
        addLogs(50, logServlet);
        ArrayList<Integer> sizes = new ArrayList<Integer>();
        String[] levels = new String[]{"ALL","TRACE","DEBUG","INFO","WARN","FATAL","OFF"};
        //tests all level values to see that the doGet filters the level correctly
        for(String s: levels){
            LogEvent[] events = getLogs(50, s, logServlet);
            int allSize = events.length;
            if (events == null) {
                fail();
            }
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

    //Test that we get back the same loggings event that we sent
    @Test
    public void doGetContentValues2() throws Exception {

        LogStorage.clear();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        LogEvent[] sendEvents = creator.createLoggingEvents(10);

        String JSON = m.writerWithDefaultPrettyPrinter().writeValueAsString(sendEvents);
        request.setContent(JSON.getBytes());
        logServlet.doPost(request, response);
        assertEquals(200, response.getStatus());

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        request.setParameter("limit", "10");
        request.setParameter("level", "ALL");
        logServlet.doGet(request, response);
        assertEquals(200, response.getStatus());

        LogEvent[] getEvents = m.readValue(response.getContentAsString(), LogEvent[].class);

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
    public void doPostCorrectCode() throws Exception {
        LogStorage.clear();

        //test send one log
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        LogEvent[] events = creator.createLoggingEvents(1);

        String JSON = m.writerWithDefaultPrettyPrinter().writeValueAsString(events);
        request.setContent(JSON.getBytes());
        logServlet.doPost(request, response);
        assertEquals(200, response.getStatus());

        //test sending 15 logs
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        events = creator.createLoggingEvents(15);

        JSON = m.writerWithDefaultPrettyPrinter().writeValueAsString(events);
        request.setContent(JSON.getBytes());
        logServlet.doPost(request, response);
        assertEquals(200, response.getStatus());

    }

    //test sending non Log event JSON
    @Test
    public void doPostBadInputCode() {
        LogStorage.clear();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        try {
            String JSON = m.writerWithDefaultPrettyPrinter().writeValueAsString("THIS is not a logging event");
            request.setContent(JSON.getBytes());
            logServlet.doPost(request, response);
            assertEquals(400, response.getStatus());

        } catch (Exception e) {
            fail();
        }
    }


    //Test sending two of the same ID Codes together
    @Test
    public void doPostDoubleIDCode() throws Exception{
        LogStorage.clear();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        LogEvent[] events = creator.createLoggingEvents(1);
        LogEvent[] doubleUp = new LogEvent[]{events[0], events[0]};

        String JSON = m.writerWithDefaultPrettyPrinter().writeValueAsString(doubleUp);
        request.setContent(JSON.getBytes());
        logServlet.doPost(request, response);
        assertEquals(409, response.getStatus());

    }

    //Test sending two of the same ID Codes seperatly
    @Test
    public void doPostDoubleIDCode2() throws Exception {
        LogStorage.clear();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        LogEvent[] events = creator.createLoggingEvents(1);
        String JSON = m.writerWithDefaultPrettyPrinter().writeValueAsString(events);

        //First one should be added okay
        request.setContent(JSON.getBytes());
        logServlet.doPost(request, response);
        assertEquals(200, response.getStatus());

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        //second time should be declined
        request.setContent(JSON.getBytes());
        logServlet.doPost(request, response);
        assertEquals(409, response.getStatus());

    }



    //Helper method for getting logs from server
    public LogEvent[] getLogs(Integer num,String level, LogServlet logServlet) {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setParameter("limit", num.toString());
        request.setParameter("level", level);
        logServlet.doGet(request, response);
        try {
            return m.readValue(response.getContentAsString(), LogEvent[].class);
        }catch (Exception e){
            return null;
        }
    }


    //helper method for posting logs to server
    public void addLogs(int num, LogServlet logServlet) {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        LogEvent[] events = creator.createLoggingEvents(num);

        try {
            String JSON = m.writerWithDefaultPrettyPrinter().writeValueAsString(events);
            request.setContent(JSON.getBytes());
            logServlet.doPost(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
