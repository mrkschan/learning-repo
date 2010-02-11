jQuery.fn.timeSink = function(list, range, mapper)
{
	var ts = this;
	this.sieve = null;
	list = jQuery(list);

	if ( list.length ) {
		var rows = list.children('li');
		var cache = (mapper)
				? rows.map(mapper)
				: rows.map(function() {
					return new Date(this.innerHTML);
				});

		this.children('li').each(filter);
	}

	return this;

	function filter(idx, el)
	{
		var sieve = range[idx];

		$(el).click(function ()
		{
			ts.sieve = sieve;
			cache.each(function(i, c)
			{
				if (null == sieve) {
					rows.show(); // show all
					rows.each(function(_i, el) {
						$(el).trigger('timesink-show', $(el));
					});
				} else {
					if (c >= sieve) {
						$(rows[i]).show();
						$(rows[i]).trigger('timesink-show', $(rows[i]));
					} else {
						$(rows[i]).hide();
						$(rows[i]).trigger('timesink-hide', $(rows[i]));
					}
				}
			});
		});
	}
};
