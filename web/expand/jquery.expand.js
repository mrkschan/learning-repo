/* ---------------------------------------------
expandAll v.1.3
http://adipalaz.awardspace.com/experiments/jquery/expand.html
Requires: jQuery v1.3+
Copyright (c) 2009 Adriana Palazova
Dual licensed under the MIT (http://adipalaz.awardspace.com/docs/mit-license.txt) and GPL (http://adipalaz.awardspace.com/docs/gpl-license.txt) licenses.
------------------------------------------------ */
(function($) {
$.fn.expandAll = function(options) {
    var defaults = {
         expTxt : '[Expand All]',
         cllpsTxt : '[Collapse All]',
         cllpsEl : '.collapse', // the collapsible element
         trigger : '.expand', // the elements that trigger the toggle action on the individual collapsible sections
         ref : '.expand', // the switch 'Expand All/Collapse All' is inserted before the 'ref'
         showMethod : 'show',
         hideMethod : 'hide',
         state : 'hidden', // the collapsible elements are hidden by default
         speed : 0
    };
    var o = $.extend({}, defaults, options);   
    
    var container;
    if ($(this).attr('id').length) {
      container = '#' + $(this).attr('id');
    } else if ($(this).attr('class').length){
      container = '.' + $(this).attr('class');
    } else {container = '';}
    
    var toggleTxt = o.expTxt;
    if (o.state == 'hidden') {$(this).find(o.cllpsEl).hide();} else {toggleTxt = o.cllpsTxt;}
    
    return this.each(function(index) {
        $(this).find(o.ref + ':first').before('<p class="switch"><a href="#">' + toggleTxt + '</a></p>');
        $(this).find('p.switch a').click(function() {
        var $cllps = $(this).closest(container).find(o.cllpsEl),
            $tr = $(this).closest(container).find(o.trigger);
        if ($(this).text() == o.expTxt) {
          $(this).text(o.cllpsTxt);
          $tr.addClass('open');
          $cllps[o.showMethod](o.speed);
        } else {
          $(this).text(o.expTxt);
          $tr.removeClass('open');
          $cllps[o.hideMethod](o.speed);
        }
        return false;
    });
});};
/* ---------------------------------------------
Toggler
http://adipalaz.awardspace.com/experiments/jquery/expand.html
When using this script, please keep the above url intact.
------------------------------------------------ */
$.fn.toggler = function(options) {
    var defaults = {
         cllpsEl : 'div.collapse',
         method : 'slideToggle',
         speed : 'slow',
         container : '', //the common container of all groups with collapsible content (optional)
         initShow : '.shown' //the initially expanded sections (optional)
    };
    var o = $.extend({}, defaults, options);
    
    $(this).wrapInner('<a style="display:block" href="#" title="Expand/Collapse" />');
    return this.each(function() {
      var container;
      (o.container) ? container = o.container : container = 'div';
      if (o.initShow) {
        $(this).closest(container).find(o.initShow).show().prev().addClass('open');
      }
      $(this).click(function() {
          $(this).toggleClass('open')
          .next(o.cllpsEl)[o.method](o.speed);
          return false;
    });
});};
//http://www.learningjquery.com/2008/02/simple-effects-plugins:
$.fn.fadeToggle = function(speed, easing, callback) {
    return this.animate({opacity: 'toggle'}, speed, easing, callback);
};
$.fn.slideFadeToggle = function(speed, easing, callback) {
    return this.animate({opacity: 'toggle', height: 'toggle'}, speed, easing, callback);
};
})(jQuery);
////////////////////////////
/* ---
Sample usage:
$(function() {
    $("#container").expandAll({showMethod: "slideDown", hideMethod: "slideUp", speed: 400});
    //Call toggler() with the default options. If we add class="shown" to some of the collapsible elements, they will be expanded on page load:
    $("#container  h4.expand").toggler();
});
Sample markup:
<div id="container>
  <h4 class="expand">Title 1</h4>
  <div class="collapse shown">...</div>
  <h4 class="expand">Title 2</h4>
  <div class="collapse">...</div>
</div>

--- */
