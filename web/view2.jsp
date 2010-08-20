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

<%@ page import="config.Config" %>

<%
    String admin = new Config().getConfig("admin");
%>

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

<!-- jQuery-plugin: timesink settings -->
<!-- http://github.com/mrkschan/jquery-timesink-plugin -->
        <script type="text/javascript" src="timesink/jquery.timesink.js"></script>
        <link rel="stylesheet" href="timesink/timesink.css" type="text/css" />

<!-- jQuery-plugin: jTags settings -->
<!-- http://www.benoitvidis.com/2009/09/jtags-plugin/ -->
        <script type="text/javascript" src="jtags/jquery.jtags.js"></script>
        <link rel="stylesheet" href="jtags/jtags.css" type="text/css" />

<!-- theme filter settings-->
        <script type="text/javascript" src="themefilter.js"></script>
        <link rel="stylesheet" href="themefilter.css" type="text/css" />

<!-- Custom settings -->
    <style type="text/css">
        window, body {
            overflow: scroll;
        }

        div.overlay {
            z-index: 10;
            display: none;
            position: absolute; top: 0; left: 0;
            cursor: pointer;
            opacity:0.4;
            filter:alpha(opacity=40);
            background-color: #cccccc;
        }

        div.browser {
            z-index: 20;
            display: none;
            position: absolute;
            border: 2px solid #2982C6;
            background-color: #FFF;
            padding: 5px 5px 5px 5px;
            height: 600px;
        }

        div.exit {
            float: right;
        }

        div.exit a {
            text-decoration: none;
            color: #3366cc;
        }
        div.exit a:hover {
            color: #c33;
        }

        div.category {
            float: left;
            width: 300px;
            height: 100%;
        }

        div.list {
            float: left;
            width: 600px;
            height: 100%;
        }

        ul.listing {
            margin: 0 0 0 0;
            padding: 0 0 0 5px;
        }

        fieldset {
            background-color: #eeeeee;
        }
        legend {
            padding-bottom: 10px;
        }

        ul.boxes {
            margin-left: 0px;
            margin-right: 0px;
        }

        ul.boxes li {
            border: 1px solid #2982C6;
            background-color: #FFFFFE;
            float: left;
            list-style: none;
            padding: 0px 5px 5px 5px;
            margin: 10px 10px 10px 10px;
            width: 250px;
            height: 150px;
        }

        li.category {
            cursor: pointer;
        }

        div.header {
            height: 40px;
            margin-bottom: 5px;
            border-bottom: 1px solid #eeeeee;
        }

        div.topics{
            height: 80px;
            overflow: hidden;
            padding-top: 5px;
            border-bottom: 0px;
        }

        a.topic {
            margin: 2px 2px 2px 2px;
        }


    </style>
    </head>
    <body>
        <div class="overlay"></div>
        <div class="container browser">
            <div class="exit"><a href="#">[x]</a></div>
            <div class="category"></div>
            <div class="list"></div>
        </div>
        <div class="container">
            <fieldset>
                <legend>Learning Object Repository</legend>
                <ul class="boxes">
                    <li>
                        <div><h4>Quick Search</h4></div>
                        <div><label>Keyword:</label> <input type="text" /></div>
                    </li>
<%
                List<Map<String, Object>> topics = new Config().getTopics();
                List<String> ts;

                for (Map<String, Object> c : topics) {
                    ts = (List<String>) c.get("topic");
%>
                    <li class="category">
                        <div class="header"><h4><% out.print(c.get("category")); %></h4></div>
                        <div class="topics">
                            <% for (String t : ts) out.println("<a href=\"#\" class=\"topic\">" + t + "</a>"); %>
                        </div>
                    </li>
<%
                }
%>
                </ul>
            </fieldset>
<%@include file="template/credit.jspf" %>
        </div>

        <script type="text/javascript">
            $(document).ready(function () {

                function stylize_overlay() {
                    $('div.overlay').css('width', $(window).width());
                    $('div.overlay').css('height', $(window).height());
                }

                function stylize_browser() {
                    $('div.browser').css('top', 50);
                    $('div.browser').css('left', ($(window).width() - $('div.browser').width()) / 2);
                }

                function clearup_overlay() {
                    $('div.overlay').hide();
                    $('div.category, div.list', $('div.browser')).empty();
                    $('div.browser').hide();
                }

                $('div.overlay').click(function () {
                    clearup_overlay();
                });
                $('div.exit a').click(function() {
                    clearup_overlay();
                    return false;
                });

                $('li.category').each(function(idx, li) {
                    var el = $('div.topics', this).first(),
                        ary = $('a', el).toArray();

                    if (0 == ary.length) return; // skip search box

                    // random sort
                    ary.sort(function() {return (Math.round(Math.random())-0.5);});
                    $(el).html($(ary));

                    // prevent topic list too long
                    while (el.clientHeight != el.scrollHeight) {
                        ary.pop();
                        $(el).html($(ary));
                    }

                    // click event
                    $(li).click(function() {
                        $('div.category').append($('div.header', this).clone());

                        var ul = $('<ul/>', {class: 'listing'});
                        for (var i in ary) {
                            $(ul).append(
                                $('<li/>', {html: $(ary[i]).clone()})
                            );
                        }
                        $('div.category').append($('<div/>', {html: $(ul)}));

                        stylize_overlay();
                        stylize_browser();
                        $('div.overlay, div.browser').show();
                    });
                });

                $(window).resize(function() {
                    if ($('div.overlay').is(":visible")) {
                        stylize_overlay();
                        stylize_browser();
                    }
                });
            });
        </script>

    </body>
</html>
