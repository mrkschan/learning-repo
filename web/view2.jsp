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

        div.browser div.exit {
            float: right;
        }

        div.browser div.exit a {
            text-decoration: none;
            color: #3366cc;
        }
        div.browser div.exit a:hover {
            color: #c33;
        }

        div.browser div.abstract {
            display: block;
        }

        div.browser div.abstract div.category {
            float: left;
            width: 30%;
            height: 590px;
        }

        div.browser div.abstract div.list {
            float: left;
            width: 67%;
            height: 590px;
            padding: 5px 5px 5px 5px;
        }

        div.browser div.abstract div.list div.content {
            overflow-y: auto;
            height: 100%;
        }

        div.browser div.abstract div.list div.content div.summary {
            font-weight: bold;
        }

        div.browser div.abstract div.list div.content ul {
            margin-left: 0px;
            padding-left: 0px;
        }

        div.browser div.abstract div.list div.content ul li {
            list-style: none;
            border: 1px solid #cccccc;
            margin: 5px 5px 5px 5px;
            padding: 5px 5px 5px 5px;
            cursor: pointer;
        }

        div.browser div.detail {
            display: none;
        }

        div.browser div.detail div.to_abstract {
            float: left;
            height: 590px;
            border: 1px solid #cccccc;
            margin-right: 3px;
            padding-left: 2px;
            padding-right: 2px;
            cursor: pointer;
        }

        div.browser div.detail div.to_abstract div {
            position: relative;
            top: 300px;
        }

        div.browser div.detail div.external {
            float: left;
            height: 590px;
            width: 70%;
        }

        div.external iframe {
            width: 100%;
            height: 100%;
            border: 0px;
        }

        div.browser div.detail div.metadata {
            float: left;
            height: 590px;
            width: 20%;
            padding: 5px 5px 5px 5px;
        }

        div.template {
            display: none;
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

        div.topics {
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
            <div class="abstract">
                <div class="category"></div>
                <div class="list">
                    <div class="loading">Loading ...</div>
                    <div class="content"></div>
                </div>
            </div>
            <div class="detail">
                <div class="to_abstract"><div> <a href="#">&lt;&lt;</a> </div></div>
                <div class="external"><iframe src="data:text/html;charset=utf-8,Loading..."></iframe></div>
                <div class="metadata">
                        <div class="summary"></div>
                        <div class="keyword"></div>
                        <div class="desc"></div>
                        <div class="explain"></div>
                        <div class="ref"></div>
                </div>
            </div>
        </div>
        <div class="template">
            <ul>
                <li class="thumbnail">
                    <div class="to_detail" style="float:right"> <a href="#">&gt;&gt;</a> </div>
                    <div class="preview">
                        <div class="summary"></div>
                        <div class="keyword"></div>
                    </div>
                </li>
            </ul>
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
            (function ($) {

                /*
                 * TODO:
                 * - resize from big to small, overlay not covering much
                 */

                // learning object browser
                Browser = function() {
                    var _this = this;
                    _this.state = null;

                    this.setState = function(s) {
                        _this.state = s;
                        _this.state.enter(_this);
                    }

                    this.roll = function() {
                        _this.state.roll(_this);
                    }

                    this.paint = function() {
                        _this.state.paint(_this);
                    }

                    this.resize = function() {
                        _this.state.resize(_this);
                    }

                    this.loaded = function() {
                        _this.state.loaded(_this);
                    }
                    
                    //--- init browser ---//
                    $(window).resize(function() {
                        // browser resize when window resize
                        _this.resize();
                    });

                    $('div.overlay, div.exit a').click(function () {
                        // exit browser event
                        // repaint browser
                        _this.paint();
                    });

                    $('div.to_abstract').click(function () {
                        // to abstract browser button click event
                        // $('div.to_abstract') can only be clicked in detail state
                        // toggle from detail state to abstract state
                        _this.roll();
                    });

                    $('li.category').click(function() {
                        // click event of category box, show browser
                        // browser goes from hidden state to abstract state
                        // set content of abstract browser
                        var el = $('div.topics', this).first(),
                            ary = $('a', el).toArray();

                        // clone category header to abstract browser
                        $('div.category').append($('div.header', this).clone());

                        // create topic listing of category in abstract browser
                        var ul = $('<ul/>', {'class': 'listing'});
                        for (var i in ary) {
                            $(ul).append(
                                $('<li/>', {html: $(ary[i]).clone()})
                            );
                        }
                        $('div.category').append($('<div/>', {html: $(ul)}));

                        // create learning object listing in abstract browser
                        $.getJSON(
                            'restapi/learning_objects/category/' + $('h4', this).html(),
                            function(topics) {
                                _this.loaded();

                                var list = $('<ul/>'), unique = [];
                                for (var i in topics) {
                                    var objects = $.makeArray(topics[i]);

                                    for (var j in objects) {
                                        var o = objects[j];

                                        if (-1 < $.inArray(o['_id'], unique)) continue;
                                        unique.push(o['_id']);

                                        var li = $('.template .thumbnail').clone();
                                        $('.preview .summary', li).html(o['summary']);
                                        $('.preview .keyword', li).html(o['keyword'].join(', '));
                                        $(li).attr('oid', o['_id']);

                                        list.append(li);
                                    }
                                }

                                if (0 == unique.length) {
                                    $('div.list div.content').html('No Learning Object found.');
                                } else {
                                    $('div.list div.content').append(list);
                                }
                            }
                        );

                        _this.paint(); // repaint browser, enter abstract state
                    });

                    $('div.list li.thumbnail').live('click', function() {
                        // to learning object detail button click event
                        // $('li.thumbnail') can only be clicked in abstrate state
                        // toggle from abstract state to detail state
                        _this.roll();

                        // retrieve learning object data
                        // fill data to detail browser
                        $.getJSON(
                            'restapi/learning_objects/object/' + $(this).attr('oid'),
                            function(r) {
                                $('div.external iframe').attr('src', r['ref'][0]);
                            }
                        );
                    });

                    this.setState(new Browser.hidden_state()); // default hidden state

                    return this;
                };

                // learning object browser - hidden state
                Browser.hidden_state = function() {
                    this.enter = function(browser) {
                        $('div.overlay, div.browser').hide();
                    }
                    this.roll = function(browser) {
                        // do nothing
                    }
                    this.paint = function(browser) {
                        // when repaint in hidden state
                        // browser is shown
                        $('div.overlay').css({
                            width: $(window).width(),
                            height: $(window).height()
                        });
                        $('div.overlay, div.browser').show();
                        
                        browser.setState(new Browser.abstract_state());
                    }
                    this.resize = function(browser) {
                        // do nothing
                    }
                    this.loaded = function(browser) {
                        // do nothing
                    }
                    return this;
                };

                // learning object browser - abstract state
                Browser.abstract_state = function() {
                    this.enter = function(browser) {
                        $('div.browser').css({
                            top: 50,
                            width: $(window).width() - 300,
                            left: 150
                        });
                        $('div.browser div.abstract').show();
                    }
                    this.roll = function(browser) {
                        $('div.browser div.abstract').hide();

                        browser.setState(new Browser.detail_state());
                    }
                    this.paint = function(browser) {
                        // when repaint in abstrate state
                        // browser is hidden indeed
                        $('div.category, div.list div.content', $('div.browser')).empty();
                        $('div.list div.loading').show();

                        browser.setState(new Browser.hidden_state());
                    }
                    this.resize = function(browser) {
                        $('div.overlay').css({
                            width: $(window).width(),
                            height: $(window).height()
                        });
                        $('div.browser').css({
                            width: $(window).width() - 300
                        });
                    }
                    this.loaded = function(browser) {
                        $('div.list div.loading').hide();
                    }
                    return this;
                };

                // learning object browser - detail state
                Browser.detail_state = function() {
                    this.enter = function(browser) {
                        $('div.browser').css({
                            width: $(window).width() - 20,
                            left: 5
                        });
                        $('div.browser div.detail').show();
                    }
                    this.roll = function(browser) {
                        $('div.external iframe').attr('src', 'data:text/html;charset=utf-8,Loading...');
                        $('div.browser div.detail').hide();

                        browser.setState(new Browser.abstract_state());
                    }
                    this.paint = function(browser) {
                        // when repaint in detail state
                        // browser is hidden indeed
                        $('div.external iframe').attr('src', 'data:text/html;charset=utf-8,Loading...');

                        $('div.category, div.list div.content', $('div.browser')).empty();
                        $('div.list div.loading').show();
                        $('div.browser div.detail').hide();
                        
                        browser.setState(new Browser.hidden_state());
                    }
                    this.resize = function(browser) {
                        $('div.overlay').css({
                            width: $(window).width(),
                            height: $(window).height()
                        });
                        $('div.browser').css({
                            width: $(window).width() - 20
                        });
                    }
                    this.loaded = function(browser) {
                        // do nothing
                    }
                    return this;
                };


                // onready:
                // init category box, create browser
                $(document).ready(function () {
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
                    });

                    new Browser();
                });
            })(jQuery);



            // obsolete
/*
            $(document).ready(function () {

                var browser_style;

                function stylize_overlay() {
                    $('div.overlay').css('width', $(window).width());
                    $('div.overlay').css('height', $(window).height());
                }

                function stylize_browser(s) {
                    if (null != s && undefined != s) browser_style = s;
                    
                    if ('max' == browser_style) {
                        $('div.browser').css({
                            width: $(window).width() - 20,
                            left: 5
                        });
                    } else {
                        $('div.browser').css({
                            top: 50,
                            width: $(window).width() - 300,
                            left: 150
                        });
                    }
                }

                function clearup_overlay() {
                    stylize_browser('');

                    $('div.browser').hide();
                    $('div.browser div.detail').hide();
                    $('div.overlay').hide();

                    $('div.category, div.list div.content', $('div.browser')).empty();
                    $('div.external iframe').unbind();
                    $('div.external iframe').attr('src', '');
                    $('div.external iframe').hide();

                    
                    $('div.browser div.abstract').show();
                }

                $(window).resize(function() {
                    if ($('div.overlay').is(":visible")) {
                        stylize_overlay();
                        stylize_browser();
                    }
                });

                // exit overlay
                $('div.overlay').click(function () {
                    clearup_overlay();
                });
                $('div.exit a').click(function() {
                    clearup_overlay();
                    return false;
                });

                // go back category from learning object
                $('div.to_abstract').click(function () {
                    $('div.browser div.abstract').show();
                    $('div.browser div.detail').hide();

                    $('div.external iframe').attr('src', 'data:text/html;charset=utf-8,Loading...');
                    stylize_browser('');
                });

                // show learning object
                $('li.thumbnail').live('click', function() {
                    $('div.browser div.abstract').hide();
                    $('div.browser div.detail').show();

                    $.getJSON(
                        'restapi/learning_objects/object/' + $(this).attr('oid'),
                        function(r) {                            
                            $('div.external iframe').attr('src', r['ref'][0]);
                            stylize_browser('max');
                        }
                    );
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

                    // click event, show category
                    $(li).click(function() {
                        $('div.category').append($('div.header', this).clone());

                        var ul = $('<ul/>', {class: 'listing'});
                        for (var i in ary) {
                            $(ul).append(
                                $('<li/>', {html: $(ary[i]).clone()})
                            );
                        }
                        $('div.category').append($('<div/>', {html: $(ul)}));

                        $('div.list div.loading').show();
                        $.getJSON(
                            'restapi/learning_objects/category/' + $('h4', this).html(),
                            function(topics) {
                                $('div.list div.loading').hide();

                                var list = $('<ul/>'), unique = [];
                                for (var i in topics) {
                                    var objects = $.makeArray(topics[i]);

                                    for (var j in objects) {
                                        var o = objects[j];
                                        
                                        if (-1 < $.inArray(o['_id'], unique)) continue;
                                        unique.push(o['_id']);

                                        var li = $('.template .thumbnail').clone();
                                        $('.preview .summary', li).html(o['summary']);
                                        $('.preview .keyword', li).html(o['keyword'].join(', '));
                                        $(li).attr('oid', o['_id']);

                                        list.append(li);
                                    }
                                }

                                if (0 == unique.length) {
                                    $('div.list div.content').html('No Learning Object found.');
                                } else {
                                    $('div.list div.content').append(list);
                                }
                            }
                        );

                        stylize_overlay();
                        stylize_browser('');
                        $('div.overlay, div.browser').show();
                    });
                });
            });
*/
        </script>

    </body>
</html>
