<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
		xmlns:ui="http://java.sun.com/jsf/facelets"
		xmlns:f="http://java.sun.com/jsf/core"
		xmlns:c="http://java.sun.com/jstl/core"
		xmlns:sf="http://www.springframework.org/tags/faces">

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Plotter TUD FB20 - Abrechnung</title>

	<!-- jQuery -->
	<script type="text/javascript" src="../../resources/js/jquery-1.5.2.min.js"> </script>

	<!-- jQuery UI -->
	<link rel="stylesheet" type="text/css" href="../../resources/css/smoothness/jquery-ui-1.8.11.custom.css" />

	<!-- jQuery Uniform -->
	<script type="text/javascript" src="../../resources/js/jquery.uniform-1.5.min.js"> </script>
	<link rel="stylesheet" type="text/css" href="../../resources/css/uniform.default.css" media="screen" />

	<!-- jQuery Datatables -->
	<link rel="stylesheet" type="text/css" href="../../resources/datatables/css/demo_table_jui.css" />
	<script type="text/javascript" src="../../resources/datatables/js/jquery.dataTables.min.js"> </script>

	<!-- Direct Web Remoting -->
	<script type='text/javascript' src='../../dwr/engine.js'> </script>
	<script type='text/javascript' src='../../dwr/interface/Manager.js'> </script>
	<script type='text/javascript' src='../../dwr/util.js'> </script>

	<!-- Plotter -->
	<script type="text/javascript" src="../../resources/js/plotter.js"> </script>
	<script type="text/javascript" src="../../resources/js/bill.js"> </script>
	<link rel="stylesheet" type="text/css" href="../../resources/css/bill.css" />
</head>
<body>

	<div id="container" class="bill-view">

		<h2>Plotter Abrechnung</h2>

		<table cellpadding="0" cellspacing="0" border="0"></table>

		<div class="bill-buttons">
			<div id="xls-button" class="bill-btn-cnt">
				<button>Excel Tabelle herunterladen</button>
			</div>
			<div id="delete-button" class="bill-btn-cnt">
				<button>Einträge Löschen</button>
			</div>
		</div>
	</div>

</body>
</html>