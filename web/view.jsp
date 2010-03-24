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


<!-- theme filter -->
        <script type="text/javascript" src="themefilter.js"></script>
        <link rel="stylesheet" href="themefilter.css" type="text/css" />

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

        #toTop {
            width:100px;
            border:1px solid #ccc;
            background:#f7f7f7;
            text-align:center;
            padding:5px;
            position:fixed; /* this is the magic */
            top:10px; /* together with this to put the div at the bottom*/
            left:10px;
            cursor:pointer;
            display:none;
            color:#333;
            font-family:verdana;
            font-size:11px;
        }

        h2 { margin-top: 2px; }
        table, td { 
            border-top: 0px;
            border-left: 0px;
            border-bottom: 0px;
            border-right: 0px;
        }
        table { margin-bottom: .5em; }
    </style>
    </head>
    <body>
<!-- back-to-top tricks http://agyuku.net/2009/05/back-to-top-link-using-jquery/ -->
        <div id="toTop">^ Back to Top</div>
        <script type="text/javascript">
            $(document).ready(function() {
                $(window).scroll(function () {
                    if ($(this).scrollTop() != 0) {
                        $('#toTop').fadeIn();
                    } else {
                        $('#toTop').fadeOut();
                    }
                });
                $('#toTop').click(function () {
                    $('body,html').scrollTop(0);
                });
            });
        </script>
        <div class="container">
            <fieldset>
                <legend>Learning Object Repository</legend>

                <div style="float: right">
                    &gt; Make your submission <a href="index.jsp">Here</a>
                </div>
                <div style="float: none">
                <table>
                    <tr>
                        <td>
                            <label for="filter">Keyword Filter:</label>
                            <input type="text" id="filter" name="filter" value="" />
                        </td>
                        <td>
                            <a href="#" class="button" style="float: none" onclick="reset_filter()">Reset</a>
                            <a href="#" class="button" style="float: none" onclick="collapse_all()">Collapse All</a>
                        </td>
                    </tr>
<%
    MongoController m = new MongoController();

    Map<String, Object> qt = new LinkedHashMap();
    qt.put("show", true);
    List<Map<String, Object>> lt  = m.queryTheme(qt);

    if (null != lt) {
%>
                    <tr>
                        <td colspan="2" style="padding-bottom: 0px;">
                            <label>Filter by Theme:</label>
                            <ul id="themefilter" class="theme_filter">
<%
        for (Map _m : lt) out.print("<li>&gt; " + _m.get("name") + "</li>");
%>
                            </ul>
                        </td>
                    </tr>
<%
    }
%>
                    <tr>
                        <td colspan="2">
                            <label>Filter by Time:</label>
                            <ul id="timesink" class="ts_filter">
                                <li>&gt; Within a Quarter</li>
                                <li>&gt; Within a Year</li>
                            </ul>
                        </td>
                    </tr>
                </table>
                </div>
                <div>
                    <ul id="listing">
<%
    SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyy");

    Map<String, Object> qo;
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
            // avg  = [1, 6] view = [1, inf]

            double va = Double.valueOf(a.get("view_count").toString()) + 1,
                   vb = Double.valueOf(b.get("view_count").toString()) + 1;

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
        Double user_rating, average;
        Map<String, Object> vote = null;
        Map<String, Object> qv = new LinkedHashMap<String, Object>();
        qv.put("voter", USER);

        for (Map<String, Object> o : lo) {

            _id = o.get("_id").toString();
            qv.put("oid", _id);

            keyword = (String[]) o.get("keyword");
            ref     = (String[]) o.get("ref");
            vote    = m.getVote(qv);
            average = Double.valueOf(o.get("rating").toString());

            rating = ""; user_rating = null;
            if (null != vote)        user_rating = Double.valueOf(vote.get("rating").toString());
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

    <div class="expand" id="head_<% out.print(_id); %>" theme="<% out.print(o.get("theme")); %>">
    <h2 class="bigcap"><% out.println(o.get("summary").toString()); %></h2>
    <i>&gt; <% out.print(k); %></i>
</div>

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
<!-- view: <% out.print(o.get("view_count")); %> -->

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

    function reset_filter() {
        $('#listing').children('li').show();

        $('#filter').val('');
        timesink.sieve    = null;
        themefilter.theme = null;
    }

    var toggler, liveupdate, timesink, themefilter;
    $(document).ready(function() {

        // keyword filter
        liveupdate = $('#filter').liveUpdate('#listing', function() {
            return $('.expand',this).html().toLowerCase()
        });
        liveupdate.focus();

        // expand toggler
        toggler = $('div.expand').toggler({
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

        // theme filter
        themefilter = $('#themefilter').themefilter('#listing');

        // filter registry -
        // only process el which is going to be show
        // hide those el that "should-not-show"
        var filter_registry = [
            function(evt, el) {
                // themefilter filter
                if (null != themefilter.theme) {
                    if ($('.expand', el).attr('theme') != themefilter.theme) {
                        $(el).hide();
                    }
                }
            },
            function(evt, el) {
                // timesink filter
                if (null != timesink.sieve) {
                    if (new Date($('.timestamp', el).html()) < timesink.sieve) {
                        $(el).hide();
                    }
                }
            },
            function(evt, el) {
                // keyword filter
                var term = $.trim( $('#filter').val().toLowerCase() );
                var line = $('.expand a',el).html().toLowerCase();
                if (line.score(term) == 0) {
                    $(el).hide();
                }
            }
        ];
        var _filter = function(evt, el) {
            for (var f in filter_registry) {
                if ($(el).is(":visible")) filter_registry[f](evt, el);
            }
        }

        // bind show listener for filtering
        $('#listing').bind('liveupdate-show',  _filter);
        $('#listing').bind('timesink-show',    _filter);
        $('#listing').bind('themefilter-show', _filter);
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
