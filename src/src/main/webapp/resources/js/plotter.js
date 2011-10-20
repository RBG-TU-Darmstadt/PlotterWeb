plotter = {

	formatDate: function(timestamp) {
		var date = new Date(timestamp);

		return plotter.padDate(date.getDate()) + "." + plotter.padDate(date.getMonth() + 1) + "." + plotter.padDate(date.getFullYear())
			+ ", " + plotter.padDate(date.getHours()) + ":" + plotter.padDate(date.getMinutes());
	},

	padDate: function(num) {
		return ("0" + num).slice(-2);
	},

	formatPrice: function(price) {
		return (price * 1).toFixed(2) + " €";
	}

};