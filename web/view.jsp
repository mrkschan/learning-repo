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
<%@page import="java.util.Comparator" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="template/header.jspf" %>

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

<!--jQuery-plugin: timesink settings -->
<!-- http://github.com/mrkschan/jquery-timesink-plugin -->
        <script type="text/javascript" src="timesink/jquery.timesink.js"></script>
        <link rel="stylesheet" href="timesink/timesink.css" type="text/css" />

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

        .ts_filter {
            margin-top: 0px;
            margin-left: 0px;
            margin-bottom: 0px;
            padding-left: 0px;
        }

        .ts_filter li {
            float: left;
            padding-right: 5px;
        }

        .ts_filter :first-child {
            margin-left: 0px;
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
                    <p style="margin-bottom: 0px;">
                        <label for="filter">Keyword Filter:</label>
                        <input type="text" id="filter" name="filter" value="" />
                        <a href="#" class="button" style="float: none" onclick="reset_filter()">Reset</a>
                        <a href="#" class="button" style="float: none" onclick="collapse_all()">Collapse All</a>
                    </p>
                    <ul id="timesink" class="ts_filter">
                        <li>&gt; Show item within a Quarter</li>
                        <li>&gt; Show item within a Year</li>
                    </ul>
<script type="text/javascript">
    function reset_filter() {
        $('#filter').val('');
        $('#listing').children('li').show();
        timesink.sieve = null;
    }
</script>
                </div>
                <div>
                    <ul id="listing">
<%
    MongoController m = new MongoController();

    SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyy");

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

    Collections.sort(lo, new Comparator<Map>() {
        public int compare(Map a, Map b) {
            // sort (DESC) by view * avg
            // avg  = [1, 6] view = [2, inf]

            Map views_a = (Map) a.get("views");
            Map views_b = (Map) b.get("views");

            int va = (null != views_a)? views_a.size() : 2,
                vb = (null != views_b)? views_b.size() : 2;

            Double ra = Double.valueOf(a.get("rating").toString()),
                   rb = Double.valueOf(b.get("rating").toString());

            double _a = va * (ra + 1),
                   _b = vb * (rb + 1);

            if (_a > _b) return -1;
            if (_a < _b) return 1;
            return 0;
        }
    });
    
    if (false == lo.isEmpty()) {
        String[] keyword, ref;
        String _id, k, r, desc, explain, rating;
        Map<String, Double> votes;
        Double user_rating, average;

        for (Map<String, Object> o : lo) {

            _id = o.get("_id").toString();

            keyword = (String[]) o.get("keyword");
            ref     = (String[]) o.get("ref");
            votes   = (Map<String, Double>) o.get("votes");
            average = Double.valueOf(o.get("rating").toString());

            rating = ""; user_rating = null;
            if (null != votes)       user_rating = votes.get(USER);
            if (null != user_rating) rating = ", curvalue:" + user_rating;

            k = "";
            for (int i = 0; i < keyword.length; ++i) {
                if (0 == i) k += keyword[i];
                else        k += ", " + keyword[i];
            }

            r = "";
            if (null != ref) {
                for (String _r : ref) {
                    r += "<li><a href=\"" + _r + "\">" + _r + "</a></li>\n";
                }
            }

            desc    = "<pre>" + o.get("desc").toString() + "</pre>";
            explain = "<pre>" + o.get("explain").toString() + "</pre>";
%>
<li>

<h2 class="expand" id="head_<% out.print(_id); %>">
    <%
        out.println(o.get("summary").toString() + " [<i>Keyword - " + k + "</i>]");
    %>
</h2>

<div class="collapse">
    <label>Media Type:</label>
    <span><% out.println(o.get("type").toString()); %></span>
    <br /><br />

    <label>Content Description:</label>
    <% out.println(desc); %>

    <label>Explanation of Concept:</label>
    <% out.println(explain); %>

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
<!--  avg: <% out.print(average); %> -->
<!-- view: <% Map _views = (Map) o.get("views"); if (null != _views) out.print(_views.size()); %> -->

    <div style="float: right">
        - <span class="timestamp"><% out.print(df.format(o.get("create"))); %></span>
    </div>

<script type="text/javascript">
    $(document).ready(function() {
        $('#vote_<% out.print(_id); %>').rating(
            'evaluate?action=vote&oid=<% out.print(_id); %>',
            {maxvalue:5, increment:.5<% out.print(rating); %>}
        );
    });
</script>
</div>

</li>
<%
        }
    }
%>
                    </ul>
<script type="text/javascript">
    var toggler, liveupdate, timesink;
    $(document).ready(function() {

        // keyword filter
        liveupdate = $('#filter').liveUpdate('#listing', function() {
            return $('.expand',this).html().toLowerCase()
        });
        liveupdate.focus();

        // expand toggler
        toggler = $('h2.expand').toggler({
            method: 'toggle', speed: 0,
            expandCallback: function(o) {
                var oid = o.id.replace('head_','');
                $.ajax({ url: 'evaluate?action=view&oid=' + oid});
            }
        });

        // time range filter
	var anchor  = new Date();
	var quarter = new Date(anchor); quarter.setMonth(anchor.getMonth() - 4);
	var year    = new Date(anchor); year.setFullYear(anchor.getFullYear() - 1);

	timesink = $('#timesink').timeSink(
            '#listing',
            [quarter, year],
            function () {
                return new Date($('.timestamp',this).html());
            }
	);

        $('#listing').bind('liveupdate-show', function(evt, el) {
            if (null != timesink.sieve) {
                if (new Date($('.timestamp', el).html()) < timesink.sieve) {
                    $(el).hide();
                }
            }
        });
        $('#listing').bind('timesink-show', function(evt, el) {
            var term = $.trim( $('#filter').val().toLowerCase() );
            var line = $('.expand a',el).html().toLowerCase();
            if (line.score(term) == 0) {
                $(el).hide();
            }
        });
    });

    function collapse_all() {
        toggler.each(function() {
            if ($(this).hasClass('open')) {
                $(this).removeClass('open').next('div.collapse').hide();
            }
        });
    }
</script>
                </div>
            </fieldset>
<%@include file="template/credit.jspf" %>
        </div>
    </body>
</html>
