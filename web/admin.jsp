<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="template/auth_header.jspf" %>
<%@ page import="config.Config" %>

<%
    String admin = new Config().getConfig("admin");
    if (false == admin.contains(USER)) response.sendRedirect("evil.html");
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="template/header.jspf" %>

<!-- jQuery-plugin: autocomplete v1.1 settings -->
<!-- http://bassistance.de/jquery-plugins/jquery-plugin-autocomplete/ -->
        <link rel="stylesheet" href="autocomplete/jquery.autocomplete.css" type="text/css" />
        <script type="text/javascript" src="autocomplete/lib/jquery.bgiframe.min.js"></script>
        <script type="text/javascript" src="autocomplete/jquery.autocomplete.js"></script>
    </head>
    <body>
        <div class="container">
            <div>
<script type="text/javascript">

    var param = window.location.search.replace('?', '').split('&');
    var _n = null, _k = null, _sh = null, p;
    for (var idx in param) {
        p = param[idx];
        if (-1 != p.indexOf("name"))    _n  = p.split('=').pop();
        if (-1 != p.indexOf("keyword")) _k  = p.split('=').pop();
        if (-1 != p.indexOf("show"))    _sh = p.split('=').pop();
    }

    if (null != _n && null != _k) {
        document.write('<p class="success">Theme Saved!</p>');
    }
</script>
            </div>
            <form action="define_theme" method="post">
                <fieldset>
                    <legend>Define Submission Theme</legend>

                    <div style="float: right">
                        &gt; View all submission <a href="view.jsp">Here</a>
                    </div>
                    <div>
                        <p>
                            <label for="name">Theme:</label>
                            <input type="text" id="name" name="name" />
<script type="text/javascript">
    $(document).ready(function () {
        $("#name").autocomplete("fetch_theme", {delay: 200}).result(
            function() { fetch_keyword(); }
        )
    });
    if (null != _n) $("#name").val(unescape(_n));
</script>
                        </p>
                    </div>
                    <div>
                        <label for="keyword">Keywords (Comma Separated Value):</label>
                        <br />
                        <input type="text" id="keyword" name="keyword" size="80" />
<script type="text/javascript">
    if (null != _k) $("#keyword").val(unescape(_k));
</script>
                    </div>
                    <div>
                        <input type="radio" id="show" name="show_hide" value="true" /> <label for="show">Show</label>
                        <input type="radio" id="hide" name="show_hide" value="false" /> <label for="hide">Hide</label>
<script type="text/javascript">
    if (null != _sh) {
        if ('true' == _sh) $("#show").attr('checked', true);
        else               $("#hide").attr('checked', true);
    }
</script>
                    </div>
                    <div>
                        <button class="button positive">Assign Keywords to Theme</button>
                        <a href="#" class="button" onclick="clear_keyword()">Clear Keywords</a>
<script type="text/javascript">
    function clear_keyword() {
        $("#keyword").val('');
    }
    function fetch_keyword() {
        $.getJSON("fetch_keyword?theme=" + $("#name").val(),
        function (data) {
            $("#keyword").val(data.keyword.join(', '));

            if (data.show) {
                $("#show").attr('checked', true);
                $("#hide").attr('checked', false);
            } else {
                $("#show").attr('checked', false);
                $("#hide").attr('checked', true);
            }
        });
    }
</script>
                    </div>
                </fieldset>
            </form>
<%@include file="template/credit.jspf"%>
        </div>
    </body>
</html>
