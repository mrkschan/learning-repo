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

public class Evaluation extends HttpServlet {
   
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
        SafeRequest  req = ESAPI.httpUtilities().getCurrentRequest();
        SafeResponse res = ESAPI.httpUtilities().getCurrentResponse();

        String action = req.getParameter("action");
        
             if (action.equals("vote"))     vote(req, res, USER);
        else if (action.equals("view"))     view(req, res, USER);
        else if (action.equals("annotate")) annotate(req, res, USER);
    }

    void vote(HttpServletRequest req, HttpServletResponse res, String user) 
    throws ServletException, IOException {
        
        String oid = req.getParameter("oid");
        Double user_rating = Double.valueOf(req.getParameter("rating"));

        if (-1 == user_rating.compareTo(0d) || 1 == user_rating.compareTo(5d))
            ErrorHandler.reportError(res, "Vote Rating less than 0 or larger than 5");

        MongoController m = new MongoController();

        Map<String, Object> q = new LinkedHashMap();
        q.put("_id", oid);

        Map<String, Object> o = m.getObject(q);
        if (null == o) ErrorHandler.reportError(res, "Learning Object not found");

        
        // object.votes
        Map<String, Double> votes = (Map) o.get("votes");

        if (null == votes) votes = new LinkedHashMap<String, Double>();
        votes.put(user, user_rating);

        Double average, sum = .0;
        for (Double r : votes.values()) sum += r;
        average = sum / votes.size();

        o.put("rating", average);
        o.put("votes",  votes);
        
        m.updateObject(oid, o);

        
        res.setContentType("application/json;charset=UTF-8");
        PrintWriter out = res.getWriter();
        try {
            out.println(average);
        } finally {
            out.close();
        }
    }

    void view(HttpServletRequest req, HttpServletResponse res, String user)
    throws ServletException, IOException {

        String oid = req.getParameter("oid");

        MongoController m = new MongoController();

        Map<String, Object> q = new LinkedHashMap();
        q.put("_id", oid);

        Map<String, Object> o = m.getObject(q);
        if (null == o) ErrorHandler.reportError(res, "Learning Object not found");


        // object.views
        Map<String, Boolean> views = (Map) o.get("views");

        if (null == views) views = new LinkedHashMap<String, Boolean>();
        views.put(user, true);

        o.put("views", views);

        m.updateObject(oid, o);
    }

    void annotate(HttpServletRequest req, HttpServletResponse res, String user)
    throws ServletException, IOException {
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
