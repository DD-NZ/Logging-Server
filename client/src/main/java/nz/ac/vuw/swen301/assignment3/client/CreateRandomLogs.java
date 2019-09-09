package nz.ac.vuw.swen301.assignment3.client;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class CreateRandomLogs {
    private static String[] words = {"the","of","and","a","to","in","is","you","that","it","he","was","for","on","are","as","with","his","they","I","at","be","this","have","from","or","one","had","by","word","but","not","what","all","were","we","when","your","can","said","there","use","an","each","which","she","do","how","their","if","will","up","other","about","out","many","then","them","these","so","some","her","would","make","like","him","into","time","has","look","two","more","write","go","see","number","no","way","could","people","my","than","first","water","been","call","who","oil","its","now","find","long","down","day","did","get","come","made","may","part"};
    private static Random random = new Random();
    private static Logger l = Logger.getLogger("createLogger");

    public static void main(String [] args){
        Resthome4LogsAppender a = new Resthome4LogsAppender();
        l.addAppender(a);
        l.setLevel(Level.ALL);
        createLogs();
    }
    public static void createLogs(){
        while(true){
            //generates a random string and message to send
            String s =randomString();
            switch(randomLevel()) {
                case 0:
                    l.debug(s);
                    break;
                case 1:
                    l.info(s);
                    break;
                case 2:
                    l.warn(s);
                    break;
                case 3:
                    l.error(s, new Exception());
                    break;
                case 4:
                    l.fatal(s);
                    break;
                case 5:
                    l.trace(s);
                    break;
                default:

            }

            try {
                Thread.sleep(1000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }

        }
    }

    private static String randomString(){
        StringBuilder b = new StringBuilder();
        for(int i=0;i<10;i++){
            b.append(words[random.nextInt(100)]+" ");
        }
        return b.toString();
    }

    private static int randomLevel(){
        return random.nextInt(6);
    }


}
