package nz.ac.vuw.swen301.assignment3.server;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import javax.servlet.http.*;

public class StatsServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response){
        XSSFWorkbook  workbook = LogStorage.getCSV();
        try {
            OutputStream out = response.getOutputStream();
            response.setContentType("application/vnd.ms-excel");
            workbook.write(out);


        }catch (Exception e){
            e.printStackTrace();
            response.setStatus(400);
            return;
        }
    }

}
