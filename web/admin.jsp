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
            <form action="#">
                <label>Student ID: </label><input type="text" id="sid" /> <button style="float: none">Search</button>
            </form>
            <div class="list">
            </div>
<%@include file="template/credit.jspf"%>
        </div>
        <script type="text/javascript">
            $('form').submit(function() {
                $.getJSON('restapi/learning_objects/submitby?sid=' + $('#sid').val(),
                function(objects) {
                    $('div.list').empty();
                    var ul = $('<ul/>');

                    for (var i in objects) {
                        var li = $('.template .thumbnail').clone(),
                            o = objects[i];

                        $('.date', li).html(o['create']);
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
