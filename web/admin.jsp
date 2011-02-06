<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="template/auth_header.jspf" %>
<%@ page import="config.Config" %>

<%
    String admin = new Config().getConfig("admin");
    if (false == admin.contains(USER)) { response.sendRedirect("evil.html"); return; }
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

<!-- jQuery-ui settings -->
        <link href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.9/themes/flick/jquery-ui.css"
              rel="stylesheet" type="text/css" />
        <script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.9/jquery-ui.min.js"
                type="text/javascript"></script>

        <style type="text/css">

            div.template {
                display: none;
            }

            div.list div {
                margin-bottom: 5px;
            }

            div.list div.date {
                float: right;
            }

            div.list div.edit {
                float: right;
                margin-left: 5px;
            }

            div.list div.summary {
                font-weight: bold;
            }

            div.list div.desc, div.list div.explain {
                border: 1px solid #cccccc;
                padding: 5px 5px 5px 5px;
            }

            div.list ul {
                margin-left: 0px;
                padding-left: 0px;
            }

            div.list ul li.thumbnail {
                list-style: none;
                border: 1px solid #8496ba;
                margin: 5px 5px 5px 5px;
                padding: 5px 5px 5px 5px;
            }

        </style>
    </head>
    <body>
        <div class="template">
            <ul>
                <li class="thumbnail">
                    <div class="preview">
                        <div class="edit"></div>
                        <div class="date"></div>
                        <div class="summary"></div>
                        <div class="keyword"></div>
                        <div><label>Description:</label></div>
                        <div class="desc"></div>
                        <div><label>Explanation of Concept:</label></div>
                        <div class="explain"></div>
                        <div class="ref"></div>
                    </div>
                </li>
            </ul>
        </div>
        <div class="container">
            <fieldset>
                <legend>Statistics</legend>
                <form action="#">
                    <div class="span-15 suffix-9 last">
                        <label for="since">Since: </label><input id="since" type="text" />
                        <label for="until">Until: </label><input id="until" type="text" />
                        <button id="range_reset" style="float: right;">Reset Time Range</button>
                    </div>
                    <div>
                        Download &gt;
                          <a id="link_object" href="stats/user/submission?">Learning Object Report</a>
                        | <a id="link_view_category" href="stats/visit/category?">Category View Report</a>
                        | <a id="link_view_topic" href="stats/visit/topic?">Topic View Report</a>
                    </div>
                </form>
            </fieldset>
            <fieldset>
                <legend>Learning Object Lookup</legend>
                <form action="#">
                    <label>Student ID: </label><input type="text" id="sid" /> <button style="float: none">Search</button>
                </form>
                <div class="list span-24">
                </div>
            </fieldset>
<%@include file="template/credit.jspf"%>
        </div>
        <script type="text/javascript">
            function href_base(href, token) {
                var token_idx = href.indexOf(token);
                return href.substring(0, token_idx);
            }

            $('document').ready(function() {
                $('#since, #until').datepicker({changeYear: true, changeMonth: true});
                $('#since, #until').change(function() {
                    $('#link_object, #link_view_category, #link_view_topic')
                    .each(function(idx, el) {
                        $(el).attr('href', href_base($(el).attr('href'), '?')
                                         + '?since=' + $('#since').val()
                                         + '&until=' + $('#until').val());
                    });
                });
                $('#range_reset').click(function (){
                    $('#link_object, #link_view_category, #link_view_topic')
                    .each(function(idx, el) {
                        $(el).attr('href', href_base($(el).attr('href'), '?') + '?');
                    });
                    $('#since, #until').val('');
                    return false;
                });
            });

            $('form').submit(function() {
                $.getJSON('restapi/learning_objects/submitby?sid=' + $('#sid').val(),
                function(objects) {
                    $('div.list').empty();
                    var ul = $('<ul/>');

                    for (var i in objects) {
                        var li = $('.template .thumbnail').clone(),
                            o = objects[i];

                        $('.date', li).html(o['create']);
                        $('.edit', li).html(
                            $('<a/>', {href: 'edit.jsp?oid=' + o._id, html: 'Edit'})
                        );
                        $('.summary', li).html(o['summary']);
                        $('.keyword', li).html(o['keyword'].join(', '));
                        $('.desc', li).html(o['desc']);
                        $('.explain', li).html(o['explain']);

                        if (o['ref'] && 0 != o['ref'].length) {
                            var r_ul = $('<ul/>');
                            for (var ref_i in o['ref']) {
                                r_ul.append(
                                    $('<li/>', {
                                        html: $('<a/>', {href: o['ref'][ref_i], html: o['ref'][ref_i]})
                                    })
                                );
                            }
                            $('.ref', li).html(r_ul);
                        } else {
                            $('.ref', li).html('<ul><li>No reference provided.</li></ul>');
                        }

                        ul.append(li);
                    }

                    $('div.list').append(ul);
                });
                return false;
            });
        </script>
    </body>
</html>
