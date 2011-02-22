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
<%@ page import="org.bson.types.ObjectId" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>

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

//    MongoController m = new MongoController();
    MongoController m = MongoController.getInstance();

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

        <!-- jQuery-plugin: tokeninput v1.1 settings -->
<!-- http://loopj.com/2009/04/25/jquery-plugin-tokenizing-autocomplete-text-entry/ -->
        <script type="text/javascript" src="tokeninput/jquery.tokeninput.js"></script>
        <link rel="stylesheet" href="tokeninput/token-input-facebook.css" type="text/css">

        <style type="text/css">
            div.success {
                display: none
            }
        </style>

    </head>
    <body>
        <div class="container">
            <div class="success">
                <p>Learning Object Updated!</p>
            </div>
            <form id="f" action="update_object" method="post">
                <input type="hidden" id="oid" name="oid" value="<% out.print(oid); %>" />
                <fieldset>
                    <legend>Learning Object Editor</legend>
<!-- TODO: provide link to view object on index.jsp -->
                    <div style="float: right">
                        &gt; View all submission <a href="index.jsp">Here</a>
                    </div>
                    <div>
                        <p>
                            <label for="sid">Student ID:</label>
                            <input type="text" id="sid" name="sid" maxlength="8" value="<% out.print(o.get("sid")); %>" />
                            &nbsp;&nbsp;&nbsp;
                            <label for="pid">Partner's Student ID:</label>
                            <input type="text" id="pid" name="pid" maxlength="8" value="<% out.print(o.get("pid")); %>" />
                        </p>
                    </div>
                    <div>
                        <label>One-line Short Summary: (less than 100 characters)</label>
                        <br />
                        <input type="text" id="summary" name="summary" size="80" maxlength="100" value="<% out.print(o.get("summary")); %>" />
                    </div>
                    <div>
                        <label for="keyword">Keywords (Comma Separated Value):</label>
                        <br />
                        <input type="text" id="keyword" name="keyword" size="70" />
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
                        <label>Comment:</label>
                        <br />
                        <textarea id="comment" name="comment" cols="60" rows="10"><%
                            String _comment = (String) o.get("comment");
                            if (null != _comment) out.print(_comment);
                        %></textarea>
                    </div>
                    <div>
                        <label for="ref">Any Reference of the Object: (http/https, ftp/ftps)</label>
                        <a href="#" class="trigger">(Add reference line)</a>
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
                    </div>
                    <div>
                        Submit time: <%
                            long timestamp = new ObjectId(oid).getTime();
                            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

                            out.println(df.format(new Date(timestamp)));
                        %>
                    </div>
                    <div>
                        <button class="button positive">Submit</button>
                        <a href="#" class="button" onclick="document.forms[0].reset()">Reset</a>
                    </div>
                </fieldset>
            </form>
<script type="text/javascript">
    $().ready(function() {

        var param = window.location.search.replace('?', '').split('&');
        var _sid = null, _pid = null, p;
        for (var idx in param) {
            p = param[idx];
            if (-1 != p.indexOf("sid")) _sid = p.split('=').pop();
            if (-1 != p.indexOf("pid")) _pid = p.split('=').pop();
        }
        if (null != _sid && null != _pid) $('.success').show();
        if (null != _sid) $("#sid").val(unescape(_sid));
        if (null != _pid) $("#pid").val(unescape(_pid));

        $('input[name=type]').attr('autocomplete', 'off');

        $('.trigger').click(function() {
            $('#ref_container')
                .append($('<input />', {
                    type: 'text',
                    name: 'ref',
                    size: 80,
                    style: 'margin-bottom: 2px'
                }))
                .append($('<br />'));
            return false;
        });

<%
        String[] keyword = (String []) o.get("keyword");
        String k = "";
        for (int i = 0; i < keyword.length; ++i) {
            if (0 == i) k += keyword[i];
            else        k += ", " + keyword[i];
        }
%>
        var pk = [], k = '<%=k %>'.split(', ');
        for (var i in k) {
            pk.push({
                id: k[i],
                name: k[i]
            });
        }
        $('#keyword').tokenInput('restapi/topics', {
            classes: {
                tokenList: "token-input-list-facebook",
                token: "token-input-token-facebook",
                tokenDelete: "token-input-delete-token-facebook",
                selectedToken: "token-input-selected-token-facebook",
                highlightedToken: "token-input-highlighted-token-facebook",
                dropdown: "token-input-dropdown-facebook",
                dropdownItem: "token-input-dropdown-item-facebook",
                dropdownItem2: "token-input-dropdown-item2-facebook",
                selectedDropdownItem: "token-input-selected-dropdown-item-facebook",
                inputToken: "token-input-input-token-facebook"
            },
            prePopulate: pk
        });

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
