package util;

import config.Config;
import java.io.IOException;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AuthHandler {

    public static String checkAuth(HttpServletRequest request, HttpServletResponse response)
    throws IOException {

        HttpSession s = request.getSession(true);

        if (null == s.getAttribute("user")) {
            Config c = new Config();

            if (Boolean.valueOf(c.getConfig("dev_mode"))) {
                String user = c.getConfig("dev_admin");
                s.setAttribute("user", user);
                return user;
            }

            String auth_svr = c.getConfig("auth_server_url"),
                   nonce    = UUID.randomUUID().toString();

            s.setAttribute("nonce", nonce);

            response.sendRedirect(auth_svr +
                "?app_auth=" + request.getContextPath() + "/auth" +
                "&app_ip=" + request.getLocalAddr() +
                "&app_port=" + request.getLocalPort() +
                "&s=" + s.getId() +
                "&nonce=" + nonce +
                "&referer=" + request.getRequestURL()
            );
            return null;
        }
        return s.getAttribute("user").toString();
    }
}
