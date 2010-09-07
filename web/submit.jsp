<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@page import="mongo.MongoController" %>
<%@page import="java.util.List" %>
<%@page import="java.util.Map" %>
<%@page import="java.util.Map.Entry" %>
<%@page import="java.io.IOException" %>
<%@page import="org.owasp.esapi.ESAPI" %>
<%@include file="template/auth_header.jspf" %>

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
                display: none;
            }
        </style>

    </head>
    <body>
        <div class="container">
            <div class="success">
                <p>Learning Object Submitted!</p>
            </div>
            <form id="f" action="submit_object" method="post">
                <fieldset>
                    <legend>Learning Object Submission</legend>

                    <div style="float: right">
                        &gt; View all submission <a href="index.jsp">Here</a>
                    </div>
                    <div>
                        <p>
                            <label for="sid">Student ID:</label>
                            <input type="text" id="sid" name="sid" maxlength="8" value="<% out.print(USER); %>" />
                            &nbsp;&nbsp;&nbsp;
                            <label for="pid">Partner's Student ID:</label>
                            <input type="text" id="pid" name="pid" maxlength="8" />
                        </p>
                    </div>
                    <div>
                        <label>One-line Short Summary: (less than 100 characters)</label>
                        <br />
                        <input type="text" id="summary" name="summary" size="80" maxlength="100" />
                    </div>
                    <div>
                        <label for="keyword">Keywords:</label>
                        <br />
                        <input type="text" id="keyword" name="keyword" size="70" />
                    </div>
                    <div>
                        <label>Media Type:</label>
                        <input type="radio" name="type" value="Article" />
                        <label>Article</label>
                        <input type="radio" name="type" value="Slide" />
                        <label>Slide</label>
                        <input type="radio" name="type" value="Video" />
                        <label>Video</label>
                        <input type="radio" name="type" value="Audio" />
                        <label>Audio</label>
                        <input type="radio" name="type" value="Other" />
                        <label>Other</label>
                    </div>
                    <div>
                        <label>Content Description:</label>
                        <br />
                        <textarea id="desc" name="desc" cols="50" rows="10"></textarea>
                    </div>
                    <div>
                        <label>Explanation of Concepts:</label>
                        <br />
                        <textarea id="explain" name="explain" cols="50" rows="10"></textarea>
                    </div>
                    <div>
                        <label for="ref">Any Reference of the Object: (http/https, ftp/ftps)</label>
                        <a href="#" class="trigger">(Add reference line)</a>
                        <br />
                        <div id="ref_container">
                            <input type="text" name="ref" size="80" style="margin-bottom: 2px" /><br/>
                        </div>
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
        if (null != _pid) $("#pid").val(unescape(_pid));

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
            }
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
