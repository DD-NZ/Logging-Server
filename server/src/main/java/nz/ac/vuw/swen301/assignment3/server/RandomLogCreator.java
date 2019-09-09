package nz.ac.vuw.swen301.assignment3.server;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;


// HELPER CLASS FOR CREATING MULTIPLE LOGS AT ONCE
public class RandomLogCreator {
    private String[] words = {"the","of","and","a","to","in","is","you","that","it","he","was","for","on","are","as","with","his","they","I","at","be","this","have","from","or","one","had","by","word","but","not","what","all","were","we","when","your","can","said","there","use","an","each","which","she","do","how","their","if","will","up","other","about","out","many","then","them","these","so","some","her","would","make","like","him","into","time","has","look","two","more","write","go","see","number","no","way","could","people","my","than","first","water","been","call","who","oil","its","now","find","long","down","day","did","get","come","made","may","part"};
    private String[] loggers = {"One","Two","Three"};
    private String[] threads = {"Four","Five","Six"};
    private Random random = new Random();
    private SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public LogEvent[] createLoggingEvents(int num){
        LogEvent[] events = new LogEvent[num];
        for(int i=0;i<num;i++){
            events[i]=createLog();
        }
        return events;
    }

    private LogEvent createLog(){
        String id = UUID.randomUUID().toString();
        String message =randomString();
        String timeStamp = dateFormat.format(new Date());
        String logger = loggers[random.nextInt(3)];
        String thread = threads[random.nextInt(3)];
        String level = LogStorage.Level.values()[random.nextInt(6)+1].toString();
        if(level.equals("ERROR")){
            StackTraceElement[] e = new Exception().getStackTrace();
            StringBuilder b = new StringBuilder();
            for(StackTraceElement s: e) {
                b.append(s.toString() + "\n");
            }
            return new LogEvent(id,message,timeStamp,thread,logger, level,b.toString());
        }else{

            return new LogEvent(id,message,timeStamp,thread,logger, level);
        }

    }

    private String randomString(){
        StringBuilder b = new StringBuilder();
        for(int i=0;i<10;i++){
            b.append(words[random.nextInt(100)]+" ");
        }
        return b.toString();
    }

}
