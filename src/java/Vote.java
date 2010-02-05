import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
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
import util.ErrorHandler;

public class Vote extends HttpServlet {

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
        SafeRequest  req = ESAPI.httpUtilities().getCurrentRequest();
        SafeResponse res = ESAPI.httpUtilities().getCurrentResponse();

        AuthHandler.checkAuth(req, res);

        String user   = req.getSession().getAttribute("user").toString();
        String oid    = req.getParameter("oid");
        Double rating = Double.valueOf(req.getParameter("rating"));

        if (-1 == rating.compareTo(0d) || 1 == rating.compareTo(5d)) {
            // less than 0 or larger than 5
            ErrorHandler.reportError(response,
                "Vote Rating less than 0 or larger than 5");
        }

        Map<String, Object> q = new LinkedHashMap();
        q.put("_id", oid);

        MongoController m = new MongoController();
        if (!m.alive()) throw new IOException("mongo connection is dead");
        Map<String, Object> o = m.getObject(q);

        if (null == o) {
            // learning object not found
            ErrorHandler.reportError(response, "Learning Object not found");
        }

        Map<String, Object>[] v = (Map[]) o.get("vote");

        Map<String, Object> _vote = null;
        double sum = 0.0d;
        if (null != v) {
            for (Map<String, Object>_v : v) {
                if (_v.get("voter").equals(user)) {
                    // found vote by session[user]
                    _vote = _v;
                } else {
                    sum += Double.valueOf(_v.get("rating").toString());
                }
            }
        }

        if (null == _vote) {
            // no vote yet
            Map<String, Object>[] nv = null;
            if (v != null) {
                nv = new Map[v.length + 1];
                System.arraycopy(v, 0, nv, 0, v.length);
            } else {
                nv = new Map[1];
            }
            v = nv;

            // create vote
            _vote = new LinkedHashMap<String, Object>();
            v[v.length - 1] = _vote;
            _vote.put("voter", user);
        }
        _vote.put("rating", rating);
        sum += rating;

        o.put("rating", sum / v.length);
        o.put("vote", v);

        m.updateObject(oid, o);

        res.setContentType("application/json;charset=UTF-8");
        PrintWriter out = res.getWriter();
        try {
            out.println(sum / v.length);
        } finally {
            out.close();
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
