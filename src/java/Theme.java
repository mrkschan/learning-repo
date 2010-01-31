import java.util.logging.Level;
import java.util.logging.Logger;
import mongo.MongoController;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.owasp.esapi.ESAPI;
import util.ErrorHandler;

public class Theme extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        MongoController m = new MongoController();
        if (!m.alive()) throw new IOException("mongo connection is dead");

        if (request.getMethod().equals("GET")) {
            // auto-complete
            String qp = null;
            try {
                qp = ESAPI.validator().getValidInput(
                    "Querying theme", request.getParameter("q"),
                    "ThemeQuery", Integer.MAX_VALUE, false
                );

            } catch (Exception ex) {
                Logger.getLogger(Theme.class.getName()).log(Level.SEVERE, null, ex);
                ErrorHandler.reportError(response, "Bad theme query");
            }
            
            Pattern p = Pattern.compile(
                ".*" + qp + ".*", Pattern.CASE_INSENSITIVE
            );
            Map<String, Object> q = new LinkedHashMap();
            q.put("name", p);

            List<Map<String, Object>> themes = m.queryTheme(q);

            response.setContentType("text/plain;charset=UTF-8");
            PrintWriter out = response.getWriter();
            try {
                if (null != themes) {
                    for (Map<String, Object> t : themes) {
                        out.println(t.get("name"));
                    }
                }
            } finally {
                out.close();
            }

        } else if (request.getMethod().equals("POST")) {

            String n  = request.getParameter("name"),
                   k  = request.getParameter("keyword"),
                   sh = request.getParameter("show_hide");

            boolean okay = true;
            okay &= ESAPI.validator().isValidSafeHTML(
                "Saving theme", n, Integer.MAX_VALUE, false
            );
            okay &= ESAPI.validator().isValidSafeHTML(
                "Saving theme", k, Integer.MAX_VALUE, false
            );
            okay &= ESAPI.validator().isValidSafeHTML(
                "Saving theme", sh, 5, false
            );

            if (!okay) {
                ErrorHandler.reportError(response, "Invalid theme content");
            }

            boolean _sh = false;
            try {
                _sh = Boolean.valueOf(sh);
            } catch (Exception ex) {
                Logger.getLogger(Theme.class.getName()).log(Level.SEVERE, null, ex);
                ErrorHandler.reportError(response, "Invalid theme content");
            }

            m.saveTheme(n, k.replace(", ", ",").split(","), _sh);
            response.sendRedirect("admin.jsp" + 
                                  "?name=" + n + 
                                  "&keyword=" + k + 
                                  "&show=" + _sh
            );
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
