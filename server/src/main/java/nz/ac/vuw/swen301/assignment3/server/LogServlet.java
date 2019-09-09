package nz.ac.vuw.swen301.assignment3.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;

public class LogServlet extends HttpServlet {

    private int num = 0;
    private static ObjectMapper mapper = new ObjectMapper();
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response){
        //Checks limits are a number and is within the bounds
        try{
            Integer limit = Integer.parseInt(request.getParameter("limit"));
            if(limit<0||limit>50){
                response.setStatus(400);
                return;
            }
        }catch (NumberFormatException e){
            response.setStatus(400);
            return;
        }

        Integer limit = Integer.parseInt(request.getParameter("limit"));
        String level = request.getParameter("level");
        //Checks level is correct
        try {
            LogStorage.Level.valueOf(level);
        }catch (IllegalArgumentException e){
            response.setStatus(400);
            return;
        }

        //fetches logs and sends them back
        ArrayList<LogEvent> eventsList = LogStorage.getLogs(limit,level);
        LogEvent[] events = eventsList.toArray(new LogEvent[eventsList.size()]);
        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            String JSON = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(events);
            PrintWriter out = response.getWriter();
            out.print(JSON);

        }catch (Exception e){
            e.printStackTrace();
           response.setStatus(400);
           return;
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        //trys to convert sent string to LogEvent if fails returns 400
        try {
            LogEvent[] logEvents = mapper.readValue(request.getInputStream(), LogEvent[].class);
            //ch
            for(LogEvent e: logEvents) {
                if(LogStorage.hasID(e.id)){
                    response.setStatus(409);
                }else {
                    LogStorage.addLog(e);
                }
            }
        }catch (IOException e){

            response.setStatus(400);
        }

    }
}
