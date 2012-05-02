<!DOCTYPE html>
<%@page import="org.codehaus.jettison.json.JSONObject"%>
<%@page import="com.digitalbarista.cat.ejb.session.FacebookManager"%>
<%@page import="javax.naming.InitialContext"%>
<%@page import="java.net.URL"%>
<%@page import="com.digitalbarista.cat.business.FacebookTrackingInfo"%>
<%@page import="com.digitalbarista.cat.business.FacebookApp"%>
<%!

	public String getInclude(ServletContext context, String appId, String fileName)
	{
		try
		{
			String relativePath = appId + "/" + fileName;
			String contextPath = "/facebook/" + relativePath;
			URL exists = context.getResource(contextPath);
			if (exists != null)
			{
				return relativePath;
			}
		}
		catch(Exception e)
		{
		}
		return fileName;
	}
%>

<%
	String appName = request.getParameter("app_id");
	String signedRequest = request.getParameter("signed_request");
	String sessionSignedRequest = (String)session.getAttribute("signed_request");
	String appUrl = "http://apps.facebook.com/" + appName + "/";
	String inviteActionUrl = request.getRequestURL().toString() + "/invite_success.jsp?app_id=" + appName;
	String appId = null;
	
	String noMessagesInclude = getInclude(config.getServletContext(), appName, "no_messages.jsp");
	String analyticsInclude = getInclude(config.getServletContext(), appName, "analytics.jsp");

  String ref = request.getParameter("ref");
  String layoutClass = "";
  
  // If redirected from the canvas page assume this is mobile
  if (ref != null &&
  		ref.equals("web_canvas") )
	{
    layoutClass = "mobile";
	}

	// Lookup app by name to get facebook app ID needed for javascript SDK
	InitialContext context = new InitialContext();
	FacebookManager facebookManager = (FacebookManager)context.lookup("ejb/cat/FacebookManager");
	FacebookApp app = facebookManager.findFacebookAppByName(appName);
	if (app != null)
	{
		appId = app.getId();
	}

	// Get tracking info
	FacebookTrackingInfo trackingInfo = facebookManager.getFacebookTrackingInfo(request);

	// Use the signedRequest from the session if missing from the query params
	if (signedRequest == null ||
			signedRequest.length() < 1)
	{
		signedRequest = sessionSignedRequest;
	}
	
	// Save the current signedRequest to the session
	session.setAttribute("signed_request", signedRequest);
%>


<html class="<%=layoutClass%>">
	<head>
	 <meta name="viewport" content="width=device-width" />
	 <meta name="viewport" content="initial-scale=1.0" />
	
    <link href="css/fb.css" type="text/css" rel="stylesheet" />
    
		<script type="text/javascript">
		  var _gaq = _gaq || [];
		  _gaq.push(['_setCustomVar', 1, 'appName', '<%=trackingInfo.getAppName()%>']);
		  _gaq.push(['_setCustomVar', 2, 'fbid', '<%=trackingInfo.getFacebookUserId()%>']);
		</script>
		<jsp:include page="<%=analyticsInclude%>"></jsp:include>
		<script type="text/javascript">
		  (function() {
		    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
		    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
		    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
		  })();
		</script>
	</head>
	<body>
		<script src="http://connect.facebook.net/en_US/all.js"></script>
		<script src="js/jquery-1.4.2.min.js" type="text/javascript"></script>
		<script src="js/jquery.query-2.1.7.js" type="text/javascript"></script>
		<script src="js/messages.js" type="text/javascript"></script>
		<script src="js/fb.js" type="text/javascript"></script>
		<script src="js/util.js" type="text/javascript"></script>
		
		<input id="signedRequest" type="hidden" value="<%=signedRequest%>" />
		<input id="appId" type="hidden" value="<%=appId%>" />
		
		<div id="fb-root"></div>
		
		<div id="MainContainer">
			<div id="Bookmark" class="bookmark">
				<a id="InviteButton" class="toolButton fb_button fb_button_medium">
					<span class="fb_button_text">Invite Friends</span>
				</a>
			</div>
			
			<table id="Login" class="login">
				<tr>
					<td class="loginCell">
						Connect with this application to get your messages<br />
						<a id="LoginButton" href="#" class="button">Connect</a>
					</td>
				</tr>
			</table>
			<div id="MessageArea">
				<table class="banner">
				  <tbody>
					<tr>
					  <td class="bannerTitle">
						Messages
					  </td>
					</tr>
				  </tbody>
				</table>
				<div class="header">
				  <a id="RefreshButton" class="button" href="#">Refresh</a><a id="DeleteButton" class="button" href="#">Delete</a>
				</div>
				<div class="controlBar">
				  <span>Select:</span> <a id="SelectAll" href="#">All</a><span>,&nbsp;</span><a id="SelectNone" href= "#">None</a>
				</div>
				<div id="MessageListDiv"></div>
				<div class="footer"></div>
			</div>
			<div id="NoMessagesDiv">
				<jsp:include page="<%=noMessagesInclude%>"></jsp:include>
			</div>
			<div id="LoadingMessage">
				Loading   
				<img src="/cat/facebook/images/fb_loading.gif" />
			</div>
		</div>
	
	</body>
</html>