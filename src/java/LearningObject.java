import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
import util.ErrorHandler;

public class LearningObject extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        ESAPI.httpUtilities().setCurrentHTTP(request, response);
        SafeRequest req  = ESAPI.httpUtilities().getCurrentRequest();
        SafeResponse res = ESAPI.httpUtilities().getCurrentResponse();
        
        Map pm = req.getParameterMap();        
        String sid      = req.getParameter("sid"),
               pid      = req.getParameter("pid"),
               theme    = req.getParameter("theme"),
               type     = req.getParameter("type"),
               summary  = req.getParameter("summary"),
               desc     = req.getParameter("desc"),
               explain  = req.getParameter("explain"),
               _keyword = req.getParameter("keyword"),
               ref[]    = (String[]) pm.get("ref");

        {
            // filter empty ref
            List<String> _ref = new LinkedList<String>();
            List<String> l = Arrays.asList(ref);

            for (String i : l) {
                if (null != i && i.length() > 0) _ref.add(i);
            }
            if (_ref.size() > 0) {
                ref = new String[_ref.size()];
                _ref.toArray(ref);
            } else {
                ref = null;
            }
        }

        MongoController m = new MongoController();
        if (!m.alive()) throw new IOException("mongo connection is dead");

        // validation
        {
            boolean okay = true;

            okay &= ESAPI.validator().isValidInput(
                "Learning Object Submission",
                sid, "StudentID", 8, false
            );
            okay &= ESAPI.validator().isValidInput(
                "Learning Object Submission",
                pid, "StudentID", 8, false
            );
            okay &= ESAPI.validator().isValidInput(
                "Learning Object Submission",
                type, "MediaType", 8, false
            );
            okay &= ESAPI.validator().isValidSafeHTML(
                "Learning Object Submission",
                summary, 100, false
            );
            okay &= ESAPI.validator().isValidSafeHTML(
                "Learning Object Submission",
                desc, Integer.MAX_VALUE, false
            );
            okay &= ESAPI.validator().isValidSafeHTML(
                "Learning Object Submission",
                explain, Integer.MAX_VALUE, false
            );
            okay &= ESAPI.validator().isValidSafeHTML(
                "Learning Object Submission",
                _keyword, Integer.MAX_VALUE, false
            );
            if (null != ref) {
                for (String _r : ref) {
                    okay &= ESAPI.validator().isValidInput(
                        "Learning Object Submission",
                        _r, "ReferenceLink", Integer.MAX_VALUE, false
                    );
                }
            }

            Map<String, Object> q = new LinkedHashMap<String, Object>();
            q.put("name", theme);
            okay &= (null != m.queryTheme(q));

            if (!okay) {
                ErrorHandler.reportError(
                    response, "Invalid learning object content"
                );
            }
        }

        String keyword[] = _keyword.replace(", ",",").split(",");
        m.saveObject(
            sid, pid, theme, type, summary, desc, explain, keyword, ref
        );

        res.sendRedirect("?sid=" + sid + "&pid=" + pid);
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
