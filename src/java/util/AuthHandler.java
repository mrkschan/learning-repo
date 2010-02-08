package util;

import config.Config;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AuthHandler {

    public static String checkAuth(HttpServletRequest request, HttpServletResponse response)
    throws IOException {

        HttpSession s = request.getSession(true);

        if (null == s.getAttribute("user")) {
            String auth_svr = new Config().getConfig("auth_server_url");

            response.sendRedirect(auth_svr +
                "?app_auth=" + request.getContextPath() + "/auth" +
                "&app_ip=" + request.getLocalAddr() +
                "&app_port=" + request.getLocalPort() +
                "&s=" + s.getId() +
                "&referer=" + request.getRequestURL()
            );
            return null;
        }
        return s.getAttribute("user").toString();
    }
}
