/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tracker;

import config.Config;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mongo.MongoController;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.filters.SafeRequest;
import org.owasp.esapi.filters.SafeResponse;
import util.AuthHandler;

/**
 *
 * @author kschan
 */
public class TrackCategory extends HttpServlet {
   
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
        if (null == USER) return;
        
        ESAPI.httpUtilities().setCurrentHTTP(request, response);
        SafeRequest req  = ESAPI.httpUtilities().getCurrentRequest();
        SafeResponse res = ESAPI.httpUtilities().getCurrentResponse();

        String uri = req.getRequestURI();
        String[] uri_segments = uri.split("/");

        // only accepts uri - /tracker/category/{category}
        try {
            // align uri
            int idx = 0;
            if (!uri_segments[idx].equals("tracker")) ++idx;

            String category = null;
            int category_idx = idx + 3;
            if (category_idx < uri_segments.length) {
                category = "";
                while (category_idx < uri_segments.length) {
                    category += uri_segments[category_idx++];
                    if (category_idx < uri_segments.length) {
                        category += "/";
                    }
                }
            } else {
                category = uri_segments[uri_segments.length - 1];
            }

            List<Map<String, Object>> categories = new Config().getTopics();
            for (Map<String, Object> c: categories) {
                if (c.get("category").equals(category)) {
                    MongoController m = MongoController.getInstance();
                    Map<String, Object> qv = new LinkedHashMap<String, Object>();
                    qv.put("category", category);
                    qv.put("user", USER);

                    if (null == m.getCategoryView(qv)) {
                        m.saveCategoryView(category, USER);
                    }
                    res.setStatus(HttpServletResponse.SC_OK);
                    return;
                }
            }
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch(Exception ex) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
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
