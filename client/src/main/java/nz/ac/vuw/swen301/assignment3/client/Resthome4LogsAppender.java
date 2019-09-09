package nz.ac.vuw.swen301.assignment3.client;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Resthome4LogsAppender extends AppenderSkeleton {

    private HttpClient httpClient;

    public Resthome4LogsAppender() {
        httpClient = HttpClientBuilder.create().build();

    }



    protected void append(LoggingEvent le) {
        //generates ID
        UUID uuid = UUID.randomUUID();
        //gemerates Date
        SimpleDateFormat dateFormater = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String date = dateFormater.format(new Date());

        // creates a new LOG event
        LogEvent[] logEvent = new LogEvent[1];
        //Checks if the LogEvent has throwable information, if so creates LogEvent with stacktrace
        if(le.getThrowableInformation()!=null){
            String[] stacktrace = le.getThrowableStrRep();
            StringBuilder b = new StringBuilder();
            for(String s: stacktrace){
                b.append(s+"\n");
            }
            logEvent[0] = new LogEvent(uuid.toString(),le.getRenderedMessage(),date,le.getThreadName(),le.getLoggerName(),le.getLevel().toString(),b.toString());
        }
        //else cretes LogEvent without stacktrace
        else{
            logEvent[0] = new LogEvent(uuid.toString(),le.getRenderedMessage(),date,le.getThreadName(),le.getLoggerName(),le.getLevel().toString());
        }


        //Sends logEvent off to the server
        try {
            ObjectMapper m = new ObjectMapper();
            StringEntity JSON =  new StringEntity(m.writerWithDefaultPrettyPrinter().writeValueAsString(logEvent));
            URIBuilder builder = new URIBuilder();
            builder.setScheme("http").setHost("localhost:8080").setPath("/resthome4logs/logs");
            try {
                URI uri = builder.build();
                HttpPost request = new HttpPost(uri);
                request.addHeader("content-type", "application/json");
                request.setEntity(JSON);
                httpClient.execute(request);
            }catch (URISyntaxException e){
                e.printStackTrace();
            }
        }catch( IOException  e){
            System.out.println("Could not send log to Server");
        }
    }

    public void close() {

    }

    public boolean requiresLayout() {
        return false;
    }
}
