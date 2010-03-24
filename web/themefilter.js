jQuery.fn.themefilter = function(list)
{
	var tf = this;
	this.theme = null;
	list = jQuery(list);

	if ( list.length ) {
		var rows = list.children('li');
		var cache = rows.map(function() {
			return $('.expand',this).attr('theme');
		});

		this.children('li').each(filter);
	}

	return this;

	function filter(idx, el)
	{
		var theme = $(el).html().replace('&gt; ','');
		$(el).click(function ()
		{
			tf.theme = theme;
			cache.each(function(i, c)
			{
				if (c == theme) {
					$(rows[i]).show();
					$(rows[i]).trigger('themefilter-show', $(rows[i]));
				} else {
					$(rows[i]).hide();
					$(rows[i]).trigger('themefilter-hide', $(rows[i]));
				}
			});
		});
	}
};
