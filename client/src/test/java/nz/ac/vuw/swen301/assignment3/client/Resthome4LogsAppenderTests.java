package nz.ac.vuw.swen301.assignment3.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assume;
import org.junit.Test;
import java.net.URI;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

public class Resthome4LogsAppenderTests {

    private static ObjectMapper mapper = new ObjectMapper();

    //Tests the appender with different loggers at each level.
    @Test
    public void testCombos() throws Exception{
        Assume.assumeTrue(isServerRunning());
        Logger[] loggers = getLoggers();
        try{
            for(Logger logger: loggers) {
                logger.trace("thisistrace");
                LogEvent e = getLastLog();
                assertEquals("thisistrace", e.message);
                assertEquals("TRACE", e.level);
                assertEquals(logger.getName(),e.logger);

                logger.debug("thisisdebug");
                e = getLastLog();
                assertEquals("thisisdebug", e.message);
                assertEquals("DEBUG", e.level);
                assertEquals(logger.getName(),e.logger);

                logger.info("thisisinfo");
                e = getLastLog();
                assertEquals("thisisinfo", e.message);
                assertEquals("INFO", e.level);
                assertEquals(logger.getName(),e.logger);

                logger.warn("thisiswarn");
                e = getLastLog();
                assertEquals("thisiswarn", e.message);
                assertEquals("WARN", e.level);
                assertEquals(logger.getName(),e.logger);

                logger.error("thisiserror");
                e = getLastLog();
                assertEquals("thisiserror", e.message);
                assertEquals("ERROR", e.level);
                assertEquals(logger.getName(),e.logger);

                logger.fatal("thisisfatal");
                e = getLastLog();
                assertEquals("thisisfatal", e.message);
                assertEquals("FATAL", e.level);
                assertEquals(logger.getName(),e.logger);
            }
        }catch (Exception e){
            fail();
        }
    }

    //tests that error details get sent to the server and can be retrieved.
    @Test
    public void testErrorDetails() throws Exception{
        Assume.assumeTrue(isServerRunning());
        Logger logger = Logger.getLogger("testLogger1");
        Resthome4LogsAppender a = new Resthome4LogsAppender();
        logger.addAppender(a);
        logger.setLevel(Level.ALL);


        logger.error("yeeet");
        LogEvent logEvent = getLastLog();
        assertEquals(logEvent.errorDetails,"N/A");

        logger.error("asd", new Exception());
        logEvent = getLastLog();
        assertTrue(logEvent.errorDetails.contains("java.lang.Exception"));

        logger.info("yeeet");
         logEvent = getLastLog();
        assertEquals(logEvent.errorDetails,"N/A");

        logger.info("asd", new Exception());
        logEvent = getLastLog();
        assertTrue(logEvent.errorDetails.contains("java.lang.Exception"));

    }

    //generates three different loggers
    private Logger[] getLoggers(){
        Logger logger1 = Logger.getLogger("testLogger1");
        Logger logger2 = Logger.getLogger("testLogger2");
        Logger logger3 = Logger.getLogger("testLogger3");
        Resthome4LogsAppender a = new Resthome4LogsAppender();
        logger1.addAppender(a);
        logger1.setLevel(Level.ALL);
        logger2.addAppender(a);
        logger2.setLevel(Level.ALL);
        logger3.addAppender(a);
        logger3.setLevel(Level.ALL);
        return new Logger[]{logger1,logger2,logger3};
    }

    //gets the most recent log sent to the server
    private LogEvent getLastLog()throws Exception{
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost("localhost").setPort(8080).setPath("/resthome4logs/logs").setParameter("level","ALL").setParameter("limit","1");
        URI uri = builder.build();
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(uri);
        HttpResponse response = httpClient.execute(request);
        String entity = EntityUtils.toString(response.getEntity());
        return mapper.readValue(entity, LogEvent[].class)[0];
    }

    //checks the server is running
    private boolean isServerRunning() throws Exception {
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