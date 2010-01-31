<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="template/auth_header.jspf" %>
<%@page import="mongo.MongoController" %>
<%@page import="java.io.IOException" %>
<%@page import="java.util.Collections" %>
<%@page import="java.util.Map" %>
<%@page import="java.util.LinkedHashMap" %>
<%@page import="java.util.List" %>
<%@page import="java.util.LinkedList" %>
<%@page import="java.util.Calendar" %>
<%@page import="java.text.SimpleDateFormat" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="template/header.jspf" %>
        <title><% out.print(new Config().getConfig("repo_title")); %></title>

<!-- jQuery-plugin: livesearch settings -->
<!-- http://ejohn.org/blog/jquery-livesearch/ -->
	<script type="text/javascript" src="livesearch/quicksilver.js"></script>
	<script type="text/javascript" src="livesearch/jquery.livesearch.js"></script>

<!-- jQuery-plugin: expand settings -->
<!-- http://adipalaz.awardspace.com/experiments/jquery/expand.html -->
        <script type="text/javascript" src="expand/jquery.expand.js"></script>
        <link rel="stylesheet" href="expand/expand.css" type="text/css" />

<!-- jQuery-plugin: half star rating settings -->
<!-- http://plugins.learningjquery.com/half-star-rating/ -->
        <script type="text/javascript" src="rating/jquery.rating.js"></script>
        <link rel="stylesheet" href="rating/rating.css" type="text/css" />

<!-- Custom settings -->
    <style type="text/css">
        #listing {
            list-style-type: none;
            padding-left: 0px;
        }

        .reference {
            margin-left: 0px;
        }

        pre {
            color: #000000;
        }
    </style>
    </head>
    <body>
        <div class="container">
            <fieldset>
                <legend>Learning Object Repository</legend>
                
                <div style="float: right">
                    &gt; Make your submission <a href="index.jsp">Here</a>
                </div>
                <div style="float: none">
                    <p>
                        <label for="filter">Keyword Filter:</label>
                        <input type="text" id="filter" name="filter" value="" />
                        <a href="#" class="button" style="float: none" onclick="reset_filter()">Reset</a>
                    </p>
<script type="text/javascript">
    function reset_filter() {
        $('#filter').val('');
        $('#listing').children('li').show();
    }
</script>
                </div>
                <div>
                    <ul id="listing">
<%
    MongoController m = new MongoController();
    if (!m.alive()) throw new IOException("mongo connection is dead");

    Map<String, Object> qo, qt = new LinkedHashMap();
    qt.put("show", true);

    List<Map<String, Object>> lt  = m.queryTheme(qt);
    List<Map<String, Object>> lo  = new LinkedList();
    List<Map<String, Object>> buf = null;

    if (null != lt) {
        for (Map<String, Object> i : lt) {
            qo = new LinkedHashMap();
            qo.put("theme", (String) i.get("name"));

            buf = m.queryObject(qo);
            if (null != buf) lo.addAll(buf);
        }
    }

    Collections.shuffle(lo);
    if (false == lo.isEmpty()) {
        String[] keyword, ref;
        String _id, k, r, desc_type, desc, rating;
        Map<String, Object>[] vote;

        for (Map<String, Object> o : lo) {

            _id = o.get("_id").toString();

            keyword = (String[]) o.get("keyword");
            ref     = (String[]) o.get("ref");
            vote    = (Map<String, Object>[]) o.get("vote");

            rating = "";
            if (null != vote) {
                for (Map<String, Object> v : vote) {
                    if (v.get("voter").equals(USER)) {
                        rating = ", curvalue:" + v.get("rating"); break;
                    }
                }
            }

            k = "";
            for (int i = 0; i < keyword.length; ++i) {
                if (0 == i) k += keyword[i];
                else        k += ", " + keyword[i];
            }

            r = "";
            for (String _r : ref) {
                r += "<li><a href=\"" + _r + "\">" + _r + "</a></li>\n";
            }

            desc_type = o.get("desc_type").toString();
            if (desc_type.equals("file")) {
                desc = "<p><a href=\"/repo/download?o=" + _id + "\">Download here</a></p>";
            } else {
                desc = "<pre>" + o.get("desc").toString() + "</pre>";
            }
%>
<li>

<h2 class="expand">
    <% 
        out.println(o.get("title").toString() + " [<i>Keyword - " + k + "</i>]"); 
    %>
</h2>

<div class="collapse">
    <label>Description:</label>
        <% out.println(desc); %>

    <label>Reference:</label>
        <%
            if (r.length() > 0) {
        %>
            <ol class="reference">
                <% out.println(r); %>
            </ol>
        <%
            } else {
                out.println("<p>None.</p>");
            }
        %>

    <label>Your Rating:</label>
        <%
            out.println("<div id=\"vote_" + _id + "\" class=\"rating\"></div>");
        %>

<script type="text/javascript">
    <% 
        out.println("$('#vote_" + _id + "').rating('vote?oid=" + _id + "', {maxvalue:5, increment:.5" + rating + "});"); 
    %>
</script>
</div>

</li>
<%
        }
    }
%>
                    </ul>
<script type="text/javascript">
    $(document).ready(function() {
        $('#filter').liveUpdate('#listing', function() {
            return $('.expand',this).html().toLowerCase()
        }).focus();
        $("h2.expand").toggler({method: "toggle", speed: 0});
    });
</script>
                </div>
            </fieldset>
<%@include file="template/credit.jspf" %>
        </div>
    </body>
</html>
