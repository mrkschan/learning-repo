/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package stats;

import config.Config;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mongo.MongoController;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
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
public class UserSubmissionStats extends HttpServlet {
   
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

        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        Date since = null;
        Date until = null;
        try {
            String _since = req.getParameter("since");
            if (null != _since) since = df.parse(_since);

            String _until = req.getParameter("until");
            if (null != _until) until = df.parse(_until);
        } catch (Exception e) {
        }

        // http://poi.apache.org/spreadsheet/how-to.html#user_api
        Workbook wb = new HSSFWorkbook();
        Sheet s = wb.createSheet();

        wb.setSheetName(0, "stats_by_students");

        Row r = null;
        Cell c = null;

        // user id, object[, object, ...]
        r = s.createRow(0);

        c = r.createCell(0);
        c.setCellValue("User ID");

        c = r.createCell(1);
        c.setCellValue("Objects");

        byte[] out = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {

            MongoController m = MongoController.getInstance();
            List<String> distinct_submit = m.distinctSubmit();

            int len = distinct_submit.size();
            for (int i = 0; i < len; ++i) {
                String whom = distinct_submit.get(i);

                r = s.createRow(i + 1);

                c = r.createCell(0);
                c.setCellValue(whom);

                HashMap<String, Object> q = new HashMap<String, Object>();
                q.put("submit", whom);

                //TODO: use saferequest instead, config esapi for each input
                String scheme = request.getScheme();
                String server_name = request.getServerName();
                int server_port = request.getServerPort();
                String context_path = request.getContextPath();

                String permlink_base = null;
                if (server_port != 80) {
                    permlink_base = scheme + "://" + server_name +
                                    ":" + server_port + context_path;
                } else {
                    permlink_base = scheme + "://" + server_name + context_path;
                }


                List<Map<String, Object>> objs = m.queryObject(q, since, until);
                for (int j = 0; null != objs && j < objs.size(); ++j) {
                    Map<String, Object> obj = objs.get(j);

                    String permlink = permlink_base + "/edit.jsp" +
                                        "?oid=" + obj.get("_id").toString();

                    HSSFHyperlink l = new HSSFHyperlink(HSSFHyperlink.LINK_URL);
                    l.setAddress(permlink);

                    c = r.createCell(j + 1);
                    c.setCellValue(permlink);
                    c.setHyperlink(l);
                }
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
