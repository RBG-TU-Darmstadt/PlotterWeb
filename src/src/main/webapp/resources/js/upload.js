upload = {

	timeoutCallback: undefined,

	uniformOptions: {
		fileDefaultText: "Keine Datei ausgewählt"
	},

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
		$("input, textarea, select, button").uniform(this.uniformOptions);

		// Get current jobs
		this.getJobs();

		$('#options-dialog-link').fancybox({
			overlayShow: true,
			hideOnOverlayClick: false,
			hideOnContentClick: false,
			enableEscapeButton: true,
			showCloseButton: false,
			onClosed: $.proxy(this, 'resetDialog')
		});

		// Upload event
		$('#upload-dialog button').click($.proxy(this, 'upload'));

		// Options change event
		$('#options-dialog input[name=copies], #options-dialog select[name=size]').change($.proxy(this, 'price'));

		// Options dialog events
		$('#cancel-button').click($.fancybox.close);
		$('input[name=print-confirm]').change($.proxy(this, 'printConfirm'));
		$('#print-button').click($.proxy(this, 'print'));
	},
	
	getJobs: function() {
		var self = this;
		Manager.getJobs(function(json) {
			var jobs = $.parseJSON(json);

			// Clear job container
			$("#jobs-dialog").empty();

			// Show jobs
			$.each(jobs, function(i, job) {
				var dom = self.renderJob(job);

				$("#jobs-dialog").prepend(dom);

				dom.wrap("<div class='plotter-job' />");
			});

			// Handle last list entry
			$('#jobs-dialog > .job:last').addClass('job-last');
		});
	},
	
	upload: function() {
		var file = dwr.util.getValue('file');

		// Activate loading overlay
		$('.plotter-upload-overlay').show();

		// Show progressbar
		$('.plotter-upload-progressbar').progressbar();

		// Start progress poller
		this.progress();

		var self = this;
		Manager.uploadFile(file, {
			callback: function(json) {
				var result = $.parseJSON(json);

				// Stop activity indicator
				$('.plotter-upload-progressbar').progressbar({
					'value': 100
				});
				// Set cleanup timer
				setTimeout(self.cleanupUpload, 200);

				if( ! result.success) {
					// Error
					if(result.error == "file-empty") {
						alert("Bitte wählen sie eine PDF Datei aus!");
					}
					else if(result.error == "upload-failed") {
						alert("Es ist ein Fehler beim Upload aufgetreten. Bitte versuchen sie es erneut!");
					}
					else if(result.error == "file-not-valid") {
						alert("Die Datei konnte nicht verarbeitet werden. Bitte wählen sie ein valides PDF aus!");
					}
				}
				else {
					// Populate options dialog
					var dialog = $('#options-dialog');
					dialog.find('#filename').text(result.job.filename);
					dialog.find('#pages').text(result.job.pages);
					dialog.find('input[name=job-key]').val(result.key);
					dialog.find('input[name=email]').val(result.mail);

					// Preview images
					$.each(result.images, function(i, image) {
						var dom = $("<div class='plotter-preview-page'>" + 
							"<img src='" + image + "'></img>" +
							"<div class='plotter-preview-page-number'>" + (i + 1) + "</div>" +
							"</div>");
	
						$("#preview-scroll").append(dom);
					});
					// Set preview images scroller width
					$('#preview-scroll > .plotter-preview-page img').load(function() {
						var width = 0;
						$('#preview-scroll > .plotter-preview-page').each(function(i, elem) {
							width += $(elem).outerWidth(true);
						});
						$('#preview-scroll').css('width', width);
					});
	
					// Open dialog
					$('#options-dialog-link').click();
				}
			},
			errorHandler: function(message) {
				alert("Fehler beim Upload, bitte laden sie die Seite neu oder versuchen sie es später erneut.");

				// Cleanup
				self.cleanupUpload();
			},
			timeout: 90*1000 // 1 minute timeout
		});
	},

	progress: function() {
		Manager.getUploadStatus(function(json) {
			var result = $.parseJSON(json);

			if(result.success) {
				$('.plotter-upload-progressbar').progressbar({
					'value': result.progress * 100
				});
			}

			if(result.progress < 1 || ! result.success) {
				// Reset progress poller
				setTimeout(upload.progress, 250);
			} else {
				setTimeout(function() {
					// Destroy progressbar
					$('.plotter-upload-progressbar').progressbar("destroy");

					// Show activity indicator
					$('.plotter-upload-activity').show();
				}, 100);
			}
		});
	},

	cleanupUpload: function() {
		// Destroy progressbar
		$('.plotter-upload-progressbar').progressbar("destroy");

		// Hide activity indicator
		$('.plotter-upload-activity').hide();

		// Deactivate loading overlay
		$('.plotter-upload-overlay').hide();

		// Fix DWR messing up of file input fields by reapplying uniform.js
		$.uniform.restore('input[name=file]');
		$('.plotter-form input').uniform(this.uniformOptions);
	},

	price: function() {
		var key = dwr.util.getValue('job-key');
		var size = dwr.util.getValue('size');
		var copies = dwr.util.getValue('copies');

		var self = this;
		Manager.calculatePrice(key, size, copies, function(json) {
			var result = $.parseJSON(json);

			if (result.success) {
				// Populate price
				var dialog = $('#options-dialog');
				dialog.find('#price').text(result.price);
			} else {
				$('#price').text('--');

				alert("Preisberechnung fehlgeschlagen.");
			}
		});
	},

	printConfirm: function(event, two) {
		// Toggle print button
		$('#print-button').attr('disabled', ! $(event.target).attr('checked'));

		// Update print button styling
		$.uniform.update('#print-button');
	},

	print: function() {
		var key = dwr.util.getValue('job-key');
		var size = dwr.util.getValue('size');
		var copies = dwr.util.getValue('copies');
		var email = dwr.util.getValue('email');

		// Disable print & cancel buttons
		$('#print-button, #cancel-button').attr('disabled', true);

		// Update print & cancel buttons styling & show activity indicator
		$.uniform.update('#print-button, #cancel-button');
		$('#options-dialog-buttons .print-activity-indicator').show();

		var self = this;
		Manager.print(key, size, copies, email, function(json) {
			var result = $.parseJSON(json);

			if( ! result.success) {
				// Error
				if(result.error == "format-not-valid") {
					alert("Bitte wählen sie eine Druckgröße aus!");
				}
				else if(result.error == "copies-not-valid") {
					alert("Bitte wählen sie die Anzahl der Kopien aus!");
				}
				else if(result.error == "mail-not-valid") {
					alert("Bitte kontrollieren sie Ihre Email-Adresse!");
				}

				// Reset print & cancel buttons
				$('#print-button').attr('disabled', false);
				$('#cancel-button').attr('disabled', false);

				// Hide print activity indicator
				$('#options-dialog-buttons .print-activity-indicator').hide();

				// Update form styling
				$.uniform.update('#print-button, #cancel-button');
			}
			else {
				$.fancybox.close();

				// Show activity indicator
				var dom = $("<div class='plotter-job'><div class='plotter-job-activity'>" +
						"</div></div>");
				dom.hide();

				$("#jobs-dialog").prepend(dom);

				dom.fadeIn(1600);

				// 1 minute timeout when the PlotterApplication does not answer
				self.timeoutCallback = setTimeout(self.receiveJobTimeout, 60*1000);
			}
		});
	},

	resetDialog: function() {
		// Reset images
		$("#preview-scroll").empty();

		// Reset format
		$('#format').val('none');

		// Reset price
		$('#price').text('--');

		// Reset print confirm
		$('input[name=print-confirm]').attr('checked', false);

		// Reset print & cancel buttons
		$('#print-button').attr('disabled', true);
		$('#cancel-button').attr('disabled', false);

		// Hide print activity indicator
		$('#options-dialog-buttons .print-activity-indicator').hide();

		// Reset file input field
		$('#upload-dialog form')[0].reset();

		// Update form styling
		$.uniform.update('input[name=file], #format, #price, input[name=print-confirm], #print-button, #cancel-button');
	},

	receiveJobCallback: function(data) {
		$.proxy(upload.receiveJob, upload)(data);
	},

	receiveJob: function(json) {
		// Clear the PlotterApplication timeout
		clearTimeout(this.timeoutCallback);

		var job = $.parseJSON(json);

		// Show job info
		$("#jobs-dialog .plotter-job:first").prepend(this.renderJob(job));

		// Fade out activity indicator
		$("#jobs-dialog .plotter-job:first .plotter-job-activity").fadeOut('slow', function() {
			$(this).remove();
		});
	},

	receiveJobTimeout: function() {
		// Fade out activity indicator
		$("#jobs-dialog .plotter-job:first > div")
			.removeClass("plotter-job-activity")
			.addClass("plotter-job-failed")
			.append(
				$("<div></div>").text("Es ist ein Fehler währened der Übertragung an den Plotter aufgetreten. Bitte versuchen sie es später erneut!")
			);
	},

	renderJob: function(job) {
		return $("<div class='plotter-job-item'>" +
					"<div class='plotter-job-item-left'>" +
						"<label>Dateiname:</label> <span class='plotter-job-filename'>" + job.filename + "</span><br />" +
						"<label>Datum/Zeit:</label> " + plotter.formatDate(job.date) + "<br />" +
						"<label>Preis:</label> " + plotter.formatPrice(job.price) +
					"</div>" +
					"<div class='plotter-job-item-right'>" +
						"<label>Format:</label> " + job.format + "<br />" +
						"<label>Seiten:</label> " + job.pages + "<br />" +
						"<label>Kopien:</label> " + job.copies +
					"</div>" +
				"</div>");
	}
};

$(document).ready(function() {
	upload.init();
});