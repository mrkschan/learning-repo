import mongo.MongoController;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import util.ErrorHandler;

public class Keyword extends HttpServlet {
   
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

        Map<String, Object> r, q = new LinkedHashMap();
        q.put("name", request.getParameter("theme"));

        r = m.getTheme(q);

        if (null == r) {
            ErrorHandler.reportError(response, "Theme not found");
        }
        String[] keyword = (String[]) r.get("keyword");

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            if (null != keyword) {
                String json = "{\"keyword\":[";
                for (int i = 0; i < keyword.length; ++i) {
                    if (0 == i) json += '"' + keyword[i] + '"';
                    else        json += "," + '"' + keyword[i] + '"';
                }
                json += "], \"show\":"+ r.get("show") + "}";
                out.println(json);
            }
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
