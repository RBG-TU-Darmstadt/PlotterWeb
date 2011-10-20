<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<jsp:useBean id="processDAO"  class="plotter.servlet.ProcessDAO" scope="request" />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
		xmlns:ui="http://java.sun.com/jsf/facelets"
		xmlns:f="http://java.sun.com/jsf/core"
		xmlns:c="http://java.sun.com/jstl/core"
		xmlns:sf="http://www.springframework.org/tags/faces">

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Plotter TUD FB20</title>

	<!-- jQuery -->
	<script type="text/javascript" src="../resources/js/jquery-1.5.2.min.js"> </script>

	<!-- jQuery UI -->
	<script type="text/javascript" src="../resources/js/jquery-ui-1.8.11.custom.min.js"> </script>
	<link rel="stylesheet" type="text/css" href="../resources/css/smoothness/jquery-ui-1.8.11.custom.css" media="screen" />

	<!-- jQuery Fancybox -->
	<script type="text/javascript" src="../resources/fancybox/jquery.fancybox-1.3.4.pack.js"> </script>
	<link rel="stylesheet" type="text/css" href="../resources/fancybox/jquery.fancybox-1.3.4.css" media="screen" />

	<!-- jQuery Uniform -->
	<script type="text/javascript" src="../resources/js/jquery.uniform-1.5.min.js"> </script>
	<link rel="stylesheet" type="text/css" href="../resources/css/uniform.default.css" media="screen" />

	<!-- Direct Web Remoting -->
	<script type='text/javascript' src='../dwr/engine.js'> </script>
	<script type='text/javascript' src='../dwr/interface/Manager.js'> </script>
	<script type='text/javascript' src='../dwr/util.js'> </script>

	<!-- Plotter -->
	<script type="text/javascript" src="../resources/js/plotter.js"> </script>
	<script type="text/javascript" src="../resources/js/upload.js"> </script>
	<link rel="stylesheet" type="text/css" href="../resources/css/plotter.css" />
</head>
<body>

	<div id="container" class="plotter-view">

		<div id="greeting" class="plotter-cnt plotter-greeting">
			Hallo <span id="name" class="plotter-name"><%= processDAO.getFirstName() %> <%= processDAO.getLastName() %></span>
		</div>

		<div class="plotter-cnt plotter-upload-cnt">
			<h2 class="plotter-heading">Datei Drucken:</h2>
			<div id="upload-dialog" class="plotter-upload">
				<label>PDF Datei auswählen:</label>

				<form class="plotter-form">
					<input type="file" name="file" class="plotter-file" />
				</form>

				<div id="upload-button" class="plotter-btn-cnt">
					<button>Hochladen &amp; Druckeinstellungen wählen</button>
				</div>

				<a id="options-dialog-link" href="#options-dialog"></a>
			</div>
			<div class="plotter-upload-overlay">
				<div class="plotter-upload-progressbar"></div>
				<div class="plotter-upload-activity">
					<div class="plotter-activity-indicator"></div>
					PDF wird verarbeitet
				</div>
			</div>
		</div>

		<div id="options-dialog-container" class="plotter-cnt">
			<div id="options-dialog" class="plotter-dialog-cnt">
				<h2 class="plotter-heading">Druckoptionen wählen:</h2>

				<div class="plotter-info-cnt">
					<div><label>Dateiname:</label> <span id="filename" class="plotter-data"></span></div>
					<div><label>Seiten:</label> <span id="pages" class="plotter-data"></span></div>
				</div>

				<div id="preview" class="plotter-preview-cnt">
					<div id="preview-scroll"></div>
				</div>

				<input type="hidden" name="job-key"/>

				<div class="plotter-cols">
					<div class="plotter-col-one-cnt plotter-col-cnt">
						<label>Größe:</label>
						<select name="size" size="1" id="format">
							<option value="none" selected="selected">Bitte wählen</option>
							<option value="A2">Din A2 (<%= processDAO.getPrice("A2") %> &euro;/Seite)</option>
							<option value="A1">Din A1 (<%= processDAO.getPrice("A1") %> &euro;/Seite)</option>
							<option value="A0">Din A0 (<%= processDAO.getPrice("A0") %> &euro;/Seite)</option>
						</select>
					</div>

					<div class="plotter-col-two-cnt plotter-col-cnt" style="visibility:hidden;">
						<label>Kopien:</label>
						<input type="text" name="copies" size="3" value="1" disabled="disabled" />
					</div>

					<div class="plotter-col-three-cnt plotter-col-cnt">
						Preis: <span id="price">--</span> &euro;
					</div>
				</div>
				<div class="clear-layout"></div>

				<label class="plotter-label-mail">Druckbestätigung an folgende Email-Adresse senden:</label>
				<input type="text" name="email" size="30" class="plotter-mail"/>

				<div id="options-dialog-buttons" class="plotter-dialog-btn-cnt">
					<input type="checkbox" name="print-confirm" /> Ich habe den oben angezeigten Kaufpreis gelesen
					<div class="plotter-split-btn-cnt">
						<div class="plotter-btn-left">
							<button id="print-button" class="button" disabled="disabled">Drucken</button>
							<div class="print-activity-indicator"></div>
						</div>
						<div class="plotter-btn-right">
							<button id="cancel-button">Abbrechen</button>
						</div>
					</div>
					<div class="clear-layout"></div>
				</div>
			</div>
		</div>

		<div class="plotter-cnt">
			<h2 class="plotter-heading">Letzten 5 Druckaufträge:</h2>
			<div id="jobs-dialog" class="plotter-jobs-cnt"></div>
		</div>

	</div>

</body>
</html>