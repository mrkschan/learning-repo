<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@page import="mongo.MongoController" %>
<%@page import="java.util.List" %>
<%@page import="java.util.Map" %>
<%@page import="java.util.Map.Entry" %>
<%@page import="java.util.LinkedHashMap" %>
<%@page import="java.io.IOException" %>
<%@page import="org.owasp.esapi.ESAPI" %>
<%@page import="org.owasp.esapi.filters.SafeRequest" %>
<%@include file="template/auth_header.jspf" %>

<%@ page import="config.Config" %>

<%
    String admin = new Config().getConfig("admin");
    if (false == admin.contains(USER)) { response.sendRedirect("evil.html"); return; }
%>

<% 
    ESAPI.httpUtilities().setCurrentHTTP(request, response);
    SafeRequest req = ESAPI.httpUtilities().getCurrentRequest();

    req.setCharacterEncoding("UTF-8");
    
    String oid = req.getParameter("oid");
    if (null == oid) { response.sendRedirect("evil.html"); return; }

    MongoController m = new MongoController();

    Map<String, Object> qo = new LinkedHashMap();
    qo.put("_id", oid);

    Map<String, Object> o = m.getObject(qo);
    if (null == o) { response.sendRedirect("evil.html"); return; }
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="template/header.jspf" %>

<!-- jQuery-plugin: validate v1.6 settings -->
<!-- http://bassistance.de/jquery-plugins/jquery-plugin-validation/ -->
        <script type="text/javascript" src="validate/jquery.validate.js"></script>
    </head>
    <body>
        <div class="container">
            <div>
<script type="text/javascript">

    var param = window.location.search.replace('?', '').split('&');
    var _sid = null, _pid = null, p;
    for (var idx in param) {
        p = param[idx];
        if (-1 != p.indexOf("sid")) _sid = p.split('=').pop();
        if (-1 != p.indexOf("pid")) _pid = p.split('=').pop();
    }

    if (null != _sid && null != _pid) {
        document.write('<p class="success">Learning Object Updated!</p>');
    }
</script>
            </div>
            <form id="f" action="update_object" method="post">
                <input type="hidden" id="oid" name="oid" value="<% out.print(oid); %>" />
                <fieldset>
                    <legend>Learning Object Editor</legend>

                    <div style="float: right">
                        &gt; View all submission <a href="view.jsp">Here</a>
                    </div>
                    <div>
                        <p>
                            <label for="sid">Student ID:</label>
                            <input type="text" id="sid" name="sid" maxlength="8" value="<% out.print(o.get("sid")); %>" />
                            &nbsp;&nbsp;&nbsp;
                            <label for="pid">Partner's Student ID:</label>
                            <input type="text" id="pid" name="pid" maxlength="8" value="<% out.print(o.get("pid")); %>" />
                        </p>
<script type="text/javascript">
    if (null != _sid) $("#sid").val(unescape(_sid));
    if (null != _pid) $("#pid").val(unescape(_pid));
</script>
                    </div>
                    <div>
                        <label for="theme">Theme:</label>
                        <select id="theme" name="theme" onchange="refresh_pool()">
<%
    List<Map<String, Object>> dump = m.dumpTheme();

    if (null != dump) {
        String theme_name;
        for (Map<String, Object> _m : dump) {
            theme_name = _m.get("name").toString();

            if (theme_name.equals(o.get("theme"))) {
                out.println("<option value=\"" + _m.get("name") + "\" selected=\"selected\">" + _m.get("name") + "</option>");
            } else {
                out.println("<option value=\"" + _m.get("name") + "\">" + _m.get("name") + "</option>");
            }
        }
    }
%>
                        </select>
<script type="text/javascript">
    var _pool = {};
<%
    if (null != dump) {
        String ary, name;
        String[] keyword;
        for (Map<String, Object> _m : dump) {
            keyword = (String[]) _m.get("keyword");
            name    = (String) _m.get("name");
            name    = name.replaceAll(" ", "_");

            ary = "[";
            for (int i = 0; null != keyword && i < keyword.length; ++i) {
                if (0 == i) ary += '"' + keyword[i] + '"';
                else        ary += "," + '"' + keyword[i] + '"';
            }
            ary += ']';
            out.println("_pool." + name + '=' + ary + ';');
        }
    }
%>
    function refresh_pool() {
        var _tv = $("#theme").val().replace(/ /g, '_');
        if (_tv.length > 0) {
            $("#pool").html(_pool[_tv].join(', '));
        }
    }
</script>
                    </div>
                    <div>
                        <label>One-line Short Summary: (less than 100 characters)</label>
                        <br />
                        <input type="text" id="summary" name="summary" size="80" maxlength="100" value="<% out.print(o.get("summary")); %>" />
                    </div>
                    <div>
                        <label>Media Type:</label>
                        <input type="radio" name="type" value="Article" <% if (o.get("type").equals("Article")) out.print("checked=\"checked\""); %> />
                        <label>Article</label>
                        <input type="radio" name="type" value="Slide" <% if (o.get("type").equals("Slide")) out.print("checked=\"checked\""); %> />
                        <label>Slide</label>
                        <input type="radio" name="type" value="Video" <% if (o.get("type").equals("Video")) out.print("checked=\"checked\""); %> />
                        <label>Video</label>
                        <input type="radio" name="type" value="Audio" <% if (o.get("type").equals("Audio")) out.print("checked=\"checked\""); %> />
                        <label>Audio</label>
                        <input type="radio" name="type" value="Other" <% if (o.get("type").equals("Other")) out.print("checked=\"checked\""); %> />
                        <label>Other</label>
                    </div>
                    <div>
                        <label>Content Description:</label>
                        <br />
                        <textarea id="desc" name="desc" cols="60" rows="10"><% out.print(o.get("desc")); %></textarea>
                    </div>
                    <div>
                        <label>Explanation of Concepts:</label>
                        <br />
                        <textarea id="explain" name="explain" cols="60" rows="10"><% out.print(o.get("explain")); %></textarea>
                    </div>
                    <div>
                        <label for="keyword">Keywords (Comma Separated Value):</label>
                        <br />
                        [<i>Suggested: <span id="pool"></span></i>]
                        <br />
                        <input type="text" id="keyword" name="keyword" size="70" value="<%
                            String[] keyword = (String []) o.get("keyword");
                            String k = "";
                            for (int i = 0; i < keyword.length; ++i) {
                                if (0 == i) k += keyword[i];
                                else        k += ", " + keyword[i];
                            }
                            out.print(k);
                        %>"/>
<script type="text/javascript">refresh_pool();</script>
                    </div>
                    <div>
                        <label for="ref">Any Reference of the Object: (http/https, ftp/ftps)</label>
                        <a href="#" class="trigger" onclick="add_ref()">(Add reference line)</a>
                        <br />
                        <div id="ref_container">
                            <%
                                String[] ref = (String []) o.get("ref");
                                if (null != ref) {
                                    for (String r: ref) {
                            %>
                                    <input type="text" name="ref" size="80" style="margin-bottom: 2px" value="<% out.print(r); %>" /><br/>
                            <%
                                    }
                                } else {
                            %>
                                    <input type="text" name="ref" size="80" style="margin-bottom: 2px" /><br/>
                            <%
                                }
                            %>
                        </div>
<script type="text/javascript">
    function add_ref() {
        var input = document.createElement("input");
        input.type = "text";
        input.name = "ref";
        input.size = 80;
        input.style.margin_bottom = '2px';
        var br = document.createElement("br");

        var c = document.getElementById("ref_container");
        c.appendChild(input);
        c.appendChild(br);
    }
</script>
                    </div>
                    <div>
                        <button class="button positive">Submit</button>
                        <a href="#" class="button" onclick="document.forms[0].reset()">Reset</a>
                    </div>
                </fieldset>
            </form>
<script type="text/javascript">
    $().ready(function() {
        $('#f').validate({
            rules: {
                sid: {required: true, digits: true, minlength: 8, maxlength: 8},
                pid: {required: true, digits: true, minlength: 8, maxlength: 8},
                summary: {required: true, maxlength: 100},
                keyword: {required: true},
                desc: {required: true, maxlength: 1024},
                explain: {required: true, maxlength: 1024},
                type: {required: true}
            },
            submitHandler: function(form) { form.submit(); }
        });
    });
</script>
<%@include file="template/credit.jspf"%>
        </div>
    </body>
</html>
