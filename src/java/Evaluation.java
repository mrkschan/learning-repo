import java.io.IOException;
import java.io.PrintWriter;
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

        Map<String, Object> qo = new LinkedHashMap();
        qo.put("_id", oid);

        Map<String, Object> o = m.getObject(qo);
        if (null == o) ErrorHandler.reportError(res, "Learning Object not found");


        // update vote of user
        Map<String, Object> qv = new LinkedHashMap();
        qv.put("voter", user);
        qv.put("oid", oid);
        Map<String, Object> vote = m.getVote(qv);

        if (null == vote) {
            m.saveVote(oid, user_rating, user);
        } else {
            vote.put("rating", user_rating);
            m.updateVote(vote.get("_id").toString(), vote);
        }


        // get all votes for object
        Map<String, Object> qvo = new LinkedHashMap();
        qvo.put("oid", oid);
        List<Map<String, Object>> votes = m.queryVote(qvo);

        double average, sum = .0;
        for (Map<String, Object> v : votes) {
            sum += Double.valueOf(v.get("rating").toString());
        }
        average = sum / votes.size();

        o.put("rating", average);
        m.updateObject(oid, o);
    }

    void view(HttpServletRequest req, HttpServletResponse res, String user)
    throws ServletException, IOException {

        String oid = req.getParameter("oid");

        MongoController m = new MongoController();

        Map<String, Object> q = new LinkedHashMap();
        q.put("_id", oid);

        Map<String, Object> o = m.getObject(q);
        if (null == o) ErrorHandler.reportError(res, "Learning Object not found");


        // get view of user
        Map<String, Object> qv = new LinkedHashMap();
        qv.put("viewer", user);
        qv.put("oid", oid);
        Map<String, Object> view = m.getView(qv);

        if (null == view) m.saveView(oid, user);


        // update view count of object
        Map<String, Object> qvc = new LinkedHashMap();
        qvc.put("oid", oid);
        Long view_count = m.getViewCount(qvc);

        o.put("view_count", Double.valueOf(view_count.toString()));
        m.updateObject(oid, o);
    }

    void annotate(HttpServletRequest req, HttpServletResponse res, String user)
    throws ServletException, IOException {

        String oid = req.getParameter("oid");

        MongoController m = new MongoController();

        Map<String, Object> q = new LinkedHashMap();
        q.put("_id", oid);

        Map<String, Object> o = m.getObject(q);
        if (null == o) ErrorHandler.reportError(res, "Learning Object not found");


        String _keyword = ESAPI.encoder().encodeForHTML(req.getParameter("keyword"));
        String keyword[] = _keyword.split(",");
        for (int i = 0; i < keyword.length; ++i) keyword[i] = keyword[i].trim();

        // get annotation of user
        Map<String, Object> qv = new LinkedHashMap();
        qv.put("whom", user);
        qv.put("oid", oid);
        Map<String, Object> annotation = m.getAnnotation(qv);

        if (null == annotation) {
            m.saveAnnotation(oid, keyword, user);
        } else {
            annotation.put("keyword", keyword);
            m.updateAnnotation(annotation.get("_id").toString(), annotation);
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
