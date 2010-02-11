//jQuery.fn.liveUpdate = function(list){
jQuery.fn.liveUpdate = function(list, mapper){
	list = jQuery(list);

	if ( list.length ) {
		var rows = list.children('li');
//		var cache = rows.map(function(){
		var cache = (mapper)? rows.map(mapper) : rows.map(function(){
				return this.innerHTML.toLowerCase();
			});

		this
			.keyup(filter).keyup()
			.parents('form').submit(function(){
				return false;
			});
	}

	return this;

	function filter(){
		var term = jQuery.trim( jQuery(this).val().toLowerCase() ), scores = [];

		if ( !term ) {
			rows.show();

			// dispatch show event
			rows.each(function(i, el) {
				$(el).trigger('liveupdate-show', $(el));
			});
		} else {
/*
			rows.hide();

			cache.each(function(i){
				var score = this.score(term);
				if (score > 0) { scores.push([score, i]); }
			});
			jQuery.each(scores.sort(function(a, b){return b[0] - a[0];}), function(){
				jQuery(rows[ this[1] ]).show();
			});
*/
			cache.each(function(i)
			{
				var score = this.score(term);
				if (score > 0) {
					$(rows[i]).show();
					$(rows[i]).trigger('liveupdate-show', $(rows[i]));
				} else {
					$(rows[i]).hide();
					$(rows[i]).trigger('liveupdate-hide', $(rows[i]));
				}
			});
		}
	}
};
