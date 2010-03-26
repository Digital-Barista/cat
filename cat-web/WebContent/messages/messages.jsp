<html>
	<head>
	</head>
	
	<body>
		<script src="http://static.ak.connect.facebook.com/js/api_lib/v0.4/FeatureLoader.js.php" type="text/javascript"></script>
		<script src="js/jquery-1.4.2.min.js" type="text/javascript"></script>
		<script src="js/fb.js" type="text/javascript"></script>
		<h1>Your Messages:</h1>
		<div id="MessageArea">
			<img src="images/loading.gif" />
		</div>
		Session Info: <%= request.getParameter("session") %>
	</body>
</html>