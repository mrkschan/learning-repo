import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mongo.MongoController;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;
import org.owasp.esapi.filters.SafeRequest;
import org.owasp.esapi.filters.SafeResponse;
import util.AuthHandler;
import util.ErrorHandler;

// TODO: create a LearningObject model, validation & persistence inside the model
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

        final String USER = AuthHandler.checkAuth(request, response);
        if (null == USER) return;

        ESAPI.httpUtilities().setCurrentHTTP(request, response);
        SafeRequest req  = ESAPI.httpUtilities().getCurrentRequest();
        SafeResponse res = ESAPI.httpUtilities().getCurrentResponse();

        req.setCharacterEncoding("UTF-8");
        Encoder e = ESAPI.encoder();

        Map pm = req.getParameterMap();
        String oid      = req.getParameter("oid"),
               sid      = req.getParameter("sid"),
               pid      = req.getParameter("pid"),
               theme    = req.getParameter("theme"),
               type     = req.getParameter("type"),
               summary  = e.encodeForHTML(req.getParameter("summary")),
               desc     = e.encodeForHTML(req.getParameter("desc")),
               explain  = e.encodeForHTML(req.getParameter("explain")),
               _keyword = e.encodeForHTML(req.getParameter("keyword")),
               comment  = e.encodeForHTML(req.getParameter("comment")),
               ref[]    = (String[]) pm.get("ref");

        // backward compatibility
        if (null == theme) theme = "";

        // replace \r\n to \n, .replace may cause overflow
        {
            String buf = "";
            int i, j = 0;
            for (i = 0; i < desc.length() - 2;) {
                if (desc.substring(i, i+2).equals("\r\n")) {
                    buf += desc.substring(j, i) + "\n";
                    j = i + 2;
                    i = j;
                } else {
                    ++i;
                }
            }
            buf += desc.substring(j);
            desc = buf;
        }
        {
            String buf = "";
            int i, j = 0;
            for (i = 0; i < explain.length() - 2;) {
                if (explain.substring(i, i+2).equals("\r\n")) {
                    buf += explain.substring(j, i) + "\n";
                    j = i + 2;
                    i = j;
                } else {
                    ++i;
                }
            }
            buf += explain.substring(j);
            explain = buf;
        }

        String errlog = "";
        try {
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
        } catch (Exception ex) {
            errlog = "Error occured when type-casting reference list to an array.\n";
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            ErrorHandler.reportError(response, "Invalid learning object content\n" + errlog);
        }

        // validation
        {
            boolean okay = true;

            if (false == ESAPI.validator()
                .isValidInput("Learning Object Submission", sid, "StudentID", 8, false))
            {
                okay = false;
                errlog += "Invalid StudenrID - sid\n";
            }
            if (false == ESAPI.validator()
                .isValidInput("Learning Object Submission", pid, "StudentID", 8, false))
            {
                okay = false;
                errlog += "Invalid StudenrID - pid\n";
            }
            if (false == ESAPI.validator()
                .isValidInput("Learning Object Submission", type, "MediaType", 8, false))
            {
                okay = false;
                errlog += "Invalid MediaType\n";
            }
            if (summary.length() > 100)
            {
                okay = false;
                errlog += "Invalid Summary Length\n";
            }
            if (desc.length() > 1024)
            { // 1kB description limit (include \n)
                okay = false;
                errlog += "Invalid Description Length\n";
            }
            if (explain.length() > 1024)
            { // 1kB explain limit (include \n)
                okay = false;
                errlog += "Invalid Explanation of Concept Length\n";
            }
            if (comment.length() > 1024)
            { // 1kB comment limit (include \n)
                okay = false;
                errlog += "Invalid Comment Length\n";
            }

            if (null != ref) {
                for (String _r : ref) {
                    if (false == ESAPI.validator()
                        .isValidInput("Learning Object Submission", _r, "ReferenceLink", Integer.MAX_VALUE, false))
                    {
                        okay = false;
                        errlog += "Invalid ReferenctLink Format\n";
                    }
                }
            }

            if (!okay) {
                ErrorHandler.reportError(
                    response, "Invalid learning object content\n" + errlog
                );
            }
        }

        LinkedList<String> keywordlist = new LinkedList<String>();
        String k, keyword[] = _keyword.split(",");
        for (int i = 0; i < keyword.length; ++i) {
            k = keyword[i].trim();
            if (false == k.isEmpty()) keywordlist.add(k);
        }
        keyword = new String[keywordlist.size()];
        keywordlist.toArray(keyword);


//        MongoController m = new MongoController();
        MongoController m = MongoController.getInstance();
        if (null == oid) {
            // new learning object
            m.saveObject(
                sid, pid, theme, type, summary, desc, explain, keyword, ref, USER
            );

            res.sendRedirect("submit.jsp?sid=" + sid + "&pid=" + pid);
        } else {
            // update learning object
            Map<String, Object> qo = new LinkedHashMap();
            qo.put("_id", oid);

            Map<String, Object> o = m.getObject(qo);
            if (null == o) ErrorHandler.reportError(res, "Learning Object not found");

            o.put("sid", sid);
            o.put("pid", pid);
            o.put("theme", theme);
            o.put("type", type);
            o.put("summary", summary);
            o.put("desc", desc);
            o.put("explain", explain);
            o.put("keyword", keyword);
            o.put("ref", ref);
            o.put("comment", comment);
            m.updateObject(oid, o);

            res.sendRedirect("edit.jsp?oid=" + oid + "&sid=" + sid + "&pid=" + pid);
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
