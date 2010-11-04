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
import org.owasp.esapi.Encoder;
import org.owasp.esapi.filters.SafeRequest;
import org.owasp.esapi.filters.SafeResponse;
import util.AuthHandler;
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

        final String USER = AuthHandler.checkAuth(request, response);
        if (null == USER) return;

        ESAPI.httpUtilities().setCurrentHTTP(request, response);
        SafeRequest req  = ESAPI.httpUtilities().getCurrentRequest();
        SafeResponse res = ESAPI.httpUtilities().getCurrentResponse();

        req.setCharacterEncoding("UTF-8");
        Encoder e = ESAPI.encoder();

        if (req.getMethod().equals("GET")) {
            // auto-complete
            String qp = e.encodeForHTML(req.getParameter("q"));

            boolean okay = ESAPI.validator().isValidInput(
                "Querying theme", qp,
                "ThemeQuery", Integer.MAX_VALUE, false
            );
            if (!okay) {
                ErrorHandler.reportError(response, "Bad theme query");
            }

            Pattern p = Pattern.compile(
                ".*" + qp + ".*", Pattern.CASE_INSENSITIVE
            );
            Map<String, Object> q = new LinkedHashMap();
            q.put("name", p);

//            MongoController m = new MongoController();
            MongoController m = MongoController.getInstance();

            List<Map<String, Object>> themes = m.queryTheme(q);

            res.setContentType("text/plain;charset=UTF-8");
            PrintWriter out = res.getWriter();
            try {
                if (null != themes) {
                    for (Map<String, Object> t : themes) {
                        out.println(t.get("name"));
                    }
                }
            } finally {
                out.close();
            }

        } else if (req.getMethod().equals("POST")) {

            String n  = e.encodeForHTML(req.getParameter("name")),
                   k  = e.encodeForHTML(req.getParameter("keyword")),
                   sh = req.getParameter("show_hide");

            boolean _sh = false;
            try {
                _sh = Boolean.valueOf(sh);
            } catch (Exception ex) {
                Logger.getLogger(Theme.class.getName()).log(Level.SEVERE, null, ex);
                ErrorHandler.reportError(response, "Invalid theme content");
            }

            String keyword[] = null;
            if (null != k) keyword = k.replace(", ", ",").split(",");

//            MongoController m = new MongoController();
            MongoController m = MongoController.getInstance();
            m.saveTheme(n, keyword, _sh);

            if (null == k) k = "";

            res.sendRedirect("admin.jsp" +
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
