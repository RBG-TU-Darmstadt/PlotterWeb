bill = {

	table: undefined,

	init: function() {
		// Enable Reverse AJAX with DWR
		dwr.engine.setActiveReverseAjax(true);
		dwr.engine.setErrorHandler(function(message, exception) {
			if (typeof window.console != undefined) {
				console.log("Error message is: " + message + " - Error Details: " + dwr.util.toDescriptiveString(exception, 2));
			}
		});

		// Register with manager
		Manager.register();

		// Style forms with Uniform
		$("input, textarea, select, button").uniform();

		// Get and show the documents
		this.loadDocuments(this.initTable);

		$('#xls-button button').click($.proxy(this, 'exportDocuments'));
		$('#delete-button button').click($.proxy(this, 'deleteDocuments'));
	},
	
	loadDocuments: function(callback) {
		var self = this;
		Manager.getDocuments(function(json) {
			var documents = $.parseJSON(json);

			var data = [];

			// Show documents
			$.each(documents, function(i, document) {
				data.push([
					"<input type='checkbox' name='ids[]' value='" + document.id + "' />",
					document.username,
					document.date,
					document.format,
					document.pages,
					document.copies,
					document.price
				]);
			});

			// Execute callback
			callback(data);
		});
	},
	
	initTable: function(data) {
		this.table = $('table').dataTable({
			"bJQueryUI": true,
			"sPaginationType": "full_numbers",
			"aaData": data,
			"aoColumns": [
				{
					"bSortable": false,
					"bSearchable ": false
				},
				{
					"sTitle": "Benutzer"
				},
				{
					"sTitle": "Datum",
					"sClass": "rightaligned",
					"fnRender": function(obj) {
						return plotter.formatDate(obj.aData[obj.iDataColumn]);
					},
					"bUseRendered": false
				},
				{
					"sTitle": "Format",
					"sClass": "rightaligned"
				},
				{
					"sTitle": "Seiten",
					"sClass": "rightaligned"
				},
				{
					"sTitle": "Kopien",
					"sClass": "rightaligned"
				},
				{
					"sTitle": "Preis",
					"sClass": "rightaligned",
					"fnRender": function(obj) {
						return plotter.formatPrice(obj.aData[obj.iDataColumn]);
					},
					"bUseRendered": false
				}
			]
		});

		// Select all checkbox
		var dom = $("<input type='checkbox' />").click(function() {
			$('.bill-view table input[type=checkbox]').attr('checked', $(this).attr('checked'));
		});
		$(".dataTables_length").prepend(dom);
		dom.after("<span style='margin-right: 20px'>Alle auswählen</span>");
	},

	updateTable: function(data) {
		this.table.fnClearTable(false);

		// Set new data
		this.table.fnAddData(data);

		this.table.fnDraw();
	},
	
	getSelectedDocuments: function() {
		var ids = [];

		$('.bill-view table input[type=checkbox]:checked').each(function() {
			ids.push($(this).val());
		});

		return ids;
	},
	
	exportDocuments: function() {
		var selectedDocuments = this.getSelectedDocuments();

		var self = this;
		Manager.exportDocuments(selectedDocuments, function(data) {
			dwr.engine.openInDownload(data);
		});
	},
	
	deleteDocuments: function() {
		var confirmation = confirm("Einträge wirklich löschen?");

		if ( ! confirmation)
			return;

		var selectedDocuments = this.getSelectedDocuments();

		var self = this;
		Manager.removeDocuments(selectedDocuments, function() {
			self.loadDocuments(self.updateTable);
		});
	}

};

$(document).ready(function() {
	bill.init();
});