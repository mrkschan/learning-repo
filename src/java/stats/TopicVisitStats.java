/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package stats;

import config.Config;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mongo.MongoController;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.filters.SafeRequest;
import org.owasp.esapi.filters.SafeResponse;
import restapi.RestAPI;
import util.AuthHandler;

/**
 *
 * @author kschan
 */
public class TopicVisitStats extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        final String USER = AuthHandler.checkAuth(request, response);
        String admin = new Config().getConfig("admin");
        if (false == admin.contains(USER)) {
            response.sendRedirect("evil.html");
            return;
        }

        ESAPI.httpUtilities().setCurrentHTTP(request, response);
        SafeRequest req  = ESAPI.httpUtilities().getCurrentRequest();
        SafeResponse res = ESAPI.httpUtilities().getCurrentResponse();

        // http://poi.apache.org/spreadsheet/how-to.html#user_api
        Workbook wb = new HSSFWorkbook();
        Sheet s = wb.createSheet();

        wb.setSheetName(0, "stats_by_topic");

        Row r = null;
        Cell c = null;

        // user id, object[, object, ...]
        r = s.createRow(0);

        c = r.createCell(0);
        c.setCellValue("Topic");

        c = r.createCell(1);
        c.setCellValue("View Count");

        byte[] out = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {

            MongoController m = MongoController.getInstance();
            List<Map<String, Object>> views = m.getTopicViews();

            HashMap<String, Integer> counts = new HashMap<String, Integer>();

            for (Map<String, Object> v: views) {
                String topic = v.get("topic").toString();
                if (!counts.containsKey(topic)) {
                    counts.put(topic, 0);
                }
                counts.put(topic, counts.get(topic) + 1);
            }

            int i = 0;
            for (Entry<String, Integer> e : counts.entrySet()) {
                r = s.createRow(i + 1);

                c = r.createCell(0);
                c.setCellValue(e.getKey());

                c = r.createCell(1);
                c.setCellValue(e.getValue());

                ++i;
            }

            wb.write(os);
            out = os.toByteArray();
            os.close();
        } catch (IOException ex) {
            Logger.getLogger(RestAPI.class.getName()).log(Level.SEVERE, null, ex);
        }

        res.setContentType("application/xls");
        res.addHeader("Content-Disposition", "attachment; filename=stats.xls");

        res.getOutputStream().write(out);
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
