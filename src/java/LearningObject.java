import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mongo.MongoController;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.owasp.esapi.ESAPI;
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
        MongoController m = new MongoController();
        if (!m.alive()) throw new IOException("mongo connection is dead");

        if (request.getMethod().equals("GET")) {
            // GET download learning object
            String id = request.getParameter("o");

            Map<String, Object> q = new LinkedHashMap();
            q.put("_id", id);

            Map<String, Object> f   = m.getFile(q);
            if (null == f) {
                ErrorHandler.reportError(response, "Learning Object not found");
            }

            ServletOutputStream out = response.getOutputStream();
            try {
                response.setContentType(f.get("contentType").toString());
                response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=" + f.get("filename").toString()
                );
                out.write((byte[]) f.get("data"));
            } finally {
                out.close();
            }

        } else {
            // POST
            // handle multipart/form-data request
            if (false == ServletFileUpload.isMultipartContent(request)) {
                ErrorHandler.reportError(response, "Non multipart/form-data");
            }

            String sid = null, pid = null, theme = null,
                   desc_txt = null, keyword = null, 
                   title = null, desc_type = null;
            List<String> ref = new LinkedList<String>();
            byte[] desc_file = null;
            String fname = null, ftype = null;

            ServletFileUpload upload = new ServletFileUpload(
                new DiskFileItemFactory()
            );

            boolean okay = true;
            try{
                // Parse the request
                Iterator<FileItem> iter = upload.parseRequest(request)
                                          .iterator();
                FileItem i;
                String f, t;
                while (okay && iter.hasNext()) {
                    i = iter.next();
                    f = i.getFieldName();

                    if (i.isFormField()) {

                        t = i.getString();
                        if (f.equals("desc_type")) {
                            desc_type = t;
                        } else if (f.equals("sid")) {
                            sid = t;
                            okay &= ESAPI.validator().isValidInput(
                                "Learning Object Submission", 
                                sid, "StudentID", 8, false
                            );
                        } else if (f.equals("pid")) {
                            pid = t;
                            okay &= ESAPI.validator().isValidInput(
                                "Learning Object Submission", 
                                pid, "StudentID", 8, false
                            );
                        } else if (f.equals("theme")) {
                            theme = t;
                            okay &= ESAPI.validator().isValidSafeHTML(
                                "Learning Object Submission", 
                                theme, Integer.MAX_VALUE, false
                            );
                        } else if (f.equals("desc_txt")) {
                            desc_txt = t;
                            okay &= ESAPI.validator().isValidSafeHTML(
                                "Learning Object Submission", 
                                desc_txt, Integer.MAX_VALUE, false
                            );
                        } else if (f.equals("keyword")) {
                            keyword = t;
                            okay &= ESAPI.validator().isValidSafeHTML(
                                "Learning Object Submission", 
                                keyword, Integer.MAX_VALUE, false
                            );
                        } else if (f.equals("title")) {
                            title = t;
                            okay &= ESAPI.validator().isValidSafeHTML(
                                "Learning Object Submission", 
                                title, 100, false
                            );
                        } else if (f.equals("ref")) {
                            if (t.length() > 0) {
                                okay &= ESAPI.validator().isValidInput(
                                    "Learning Object Submission", 
                                    t, "ReferenceLink", Integer.MAX_VALUE, false
                                );
                                if (okay) ref.add(t);
                            }
                        }

                    } else {

                        if (desc_type.equals("file")) {
                            ftype = i.getContentType();
                            fname = i.getName();
                            desc_file = i.get();
                            int upload_size = ESAPI.securityConfiguration()
                                              .getAllowedFileUploadSize();

                            okay &= ESAPI.validator().isValidFileUpload(
                                "Learning Object Submission", 
                                System.getProperty("java.io.tmpdir"),
                                fname, desc_file, upload_size, false
                            );
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(LearningObject.class.getName()).log(Level.SEVERE, null, ex);
                ErrorHandler.reportError(
                    response, "Invalid learning object content"
                );
            }

            if (!okay) {
                ErrorHandler.reportError(
                    response, "Invalid learning object content"
                );
            }

            String[] _ref = new String[ref.size()];
            ref.toArray(_ref);
            
            String[] _keyword = keyword.replace(", ",",").split(",");

            if (desc_type.equals("file")) {
                m.saveObject(
                    sid, pid, theme, title, fname, ftype, 
                    desc_file, _keyword, _ref
                );
            } else {
                m.saveObject(
                    sid, pid, theme, title, desc_txt, _keyword, _ref
                );
            }

            response.sendRedirect("?sid=" + sid + "&pid=" + pid);
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
