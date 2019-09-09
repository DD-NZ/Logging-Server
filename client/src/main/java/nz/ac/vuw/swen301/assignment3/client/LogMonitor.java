package nz.ac.vuw.swen301.assignment3.client;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;

public class LogMonitor {

    private static HttpClient httpClient;
    private static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {

        httpClient = HttpClientBuilder.create().build();


        JFrame frame = new JFrame("LOGGER");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1600,900);
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.BLACK);

        final JTextArea data = new JTextArea();
        data.setFont(new Font("Consolas",1,12));
        JScrollPane scrollPane = new JScrollPane(data);

        JPanel top = new JPanel();

        JLabel levelLabel = new JLabel("Min Level:");
        levelLabel.setFont(new Font("comic sans",1,18));

        String[] levels = new String[]{"TRACE","DEBUG", "INFO", "WARN", "ERROR", "FATAL", "OFF"};
        final JComboBox<String> comboBox = new JComboBox<String >(levels);
        comboBox.setPreferredSize(new Dimension(100,35));

        JLabel limitLabel = new JLabel("Limit:");
        limitLabel.setFont(new Font("comic sans",1,18));

        final JTextField limitInput=new JTextField();
        limitInput.setPreferredSize(new Dimension(100,35));

        final JButton fetchData = new JButton("Fetch Data");
        fetchData.setPreferredSize(new Dimension(150,35));
        fetchData.addActionListener(new ActionListener() {
            //prints the logs to the text field
            public void actionPerformed(ActionEvent e) {
                data.setText("");
                LogEvent[] events = fetchLogs(comboBox.getSelectedItem().toString(),limitInput.getText());
                data.setText("Fetching "+ limitInput.getText()+" Logs\n");
               if(events==null){
                   data.setText("That was a Bad Request");
               }else {
                   data.append("ID");
                   appendTab(data,4);
                   data.append("Level");
                   appendTab(data,1);
                   data.append("Logger");
                   appendTab(data,2);
                   data.append("Message");
                   appendTab(data,5);
                   data.append("Time Stamp");
                   appendTab(data,3);
                   data.append("Thread");
                   data.append("\n\n");

                   for (LogEvent event : events) {
                       data.append(event.id);
                       appendSpaces(data,50-event.id.length());
                       data.append("\t");
                       data.append(event.level);
                       appendSpaces(data,10-event.level.length());
                       data.append("\t");
                       data.append(event.logger);
                       appendSpaces(data,20-event.logger.length());
                       data.append("\t");
                       data.append(event.message);
                       appendSpaces(data,70-event.message.length());
                       data.append("\t");
                       data.append(event.timeStamp);
                       data.append("\t");
                       data.append(event.thread);
                       data.append("\t");
                       if(event.errorDetails.equals("N/A")){
                           data.append("Error Details:"+event.errorDetails);
                       }else{
                          String s = event.errorDetails.replaceAll("\n","\n\t");
                           data.append("\n\tError Details:\n\t"+s);
                       }

                       data.append("\n");
                   }
               }
            }
        });


        JButton downloadStats = new JButton("Download Stats");
        downloadStats.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getCSV();
              }
        });
        downloadStats.setPreferredSize(new Dimension(150,35));

        top.add(levelLabel);
        top.add(comboBox);
        top.add(limitLabel);
        top.add(limitInput);
        top.add(fetchData);
        top.add(downloadStats);

        frame.getContentPane().add(BorderLayout.NORTH,top);
        frame.getContentPane().add(BorderLayout.CENTER,scrollPane);

        frame.setVisible(true);

    }

    private static void getCSV(){
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            URIBuilder builder = new URIBuilder();
            builder.setScheme("http").setHost("localhost:8080").setPath("/resthome4logs/stats");
            URI uri = builder.build();
            HttpGet request = new HttpGet(uri);
            HttpResponse response = httpClient.execute(request);
            try {
                is = response.getEntity().getContent();
                String filePath = "stats.xlsx";
                try {
                    fos = new FileOutputStream(new File(filePath));
                    if(is!=null) {
                        int inByte;
                        while ((inByte = is.read()) != -1)
                            fos.write(inByte);
                    }
                }finally {
                    if(fos!=null) {
                        fos.close();
                    }
                }
            }finally {
                if(is!=null) {
                    is.close();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //gets logs at a level and limit
    private static LogEvent[] fetchLogs(String level, String limit){
        try {
            URIBuilder builder = new URIBuilder();
            builder.setScheme("http").setHost("localhost:8080").setPath("/resthome4logs/logs").setParameter("limit",limit ).setParameter("level",level);
            URI uri = builder.build();
            HttpGet request = new HttpGet(uri);
            HttpResponse response = httpClient.execute(request);
            if(response.getStatusLine().toString().contains("400")){return null;}
            String content = EntityUtils.toString(response.getEntity());
            return mapper.readValue(content, LogEvent[].class);

        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }


    private static void appendSpaces(JTextArea t, int num){
        for(int i=0;i<num;i++){
            t.append(" ");
        }
    }
    private static void appendTab(JTextArea t, int num){
        for(int i=0;i<num;i++){
            t.append("\t");
        }
    }
}
