package util;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

public class ErrorHandler {
    public static void reportError(HttpServletResponse response, String message)
    throws IOException, ServletException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.println("Don't be Evil!");
        } finally {
            out.close();
        }
        throw new ServletException(message);
    }
}
