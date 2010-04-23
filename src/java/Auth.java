import config.Config;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import sun.misc.BASE64Encoder;
import util.ErrorHandler;

public class Auth extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        if (request.getMethod().equals("POST")) {
            
            String user = request.getParameter("user"),
                   r    = request.getParameter("r").replace(' ', '+'),
                   sr   = null;

            if (null == user) {
                ErrorHandler.reportError(response, "Invalid Auth attempt");
                return;
            }

            HttpSession s = request.getSession();
            if (null != s) {
                String snonce = (String) s.getAttribute("nonce");
                if (null == snonce) {
                    ErrorHandler.reportError(response, "Invalid Auth attempt");
                    return;
                }

                
                try {
                    BASE64Encoder b64 = new BASE64Encoder();
                    String secret = new Config().getConfig("secret"),
                           plain  = user+snonce;

                    Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
                    SecretKeySpec k = new SecretKeySpec(
                        secret.getBytes(), "AES"
                    );
                    c.init(Cipher.ENCRYPT_MODE, k);
                    sr = b64.encode(
                        c.doFinal(b64.encode((user+snonce).getBytes()).getBytes())
                    ).replace("\n","");

                } catch (Exception ex) {
                    Logger.getLogger(Auth.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (sr.equals(r)) {
                    s.removeAttribute("nonce");
                    s.setAttribute("user", user);
                } else {
                    System.err.println("sr: " + sr);
                    System.err.println("r : " + r);
                    ErrorHandler.reportError(response, "Invalid Auth attempt");
                }
            }
            
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
