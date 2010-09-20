<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
	<head>
		<link href="/cat/facebook/css/fb.css" type="text/css" rel="stylesheet" />
	</head>
	
	<body onload="fbInit()">
		<script src="http://connect.facebook.net/en_US/all.js"></script>
		<script src="/cat/facebook/js/jquery-1.4.2.min.js" type="text/javascript"></script>
		<script src="/cat/facebook/js/messages.js" type="text/javascript"></script>
		<script src="/cat/facebook/js/fb.js" type="text/javascript"></script>
		<script src="/cat/facebook/js/util.js" type="text/javascript"></script>
		
		
		<div id="fb-root"></div>
		
		<div id="MainContainer">
			<div id="Bookmark" class="bookmark">
				<a id="InviteButton" class="toolButton fb_button fb_button_medium">
					<span class="fb_button_text">Invite Friends</span>
				</a>
				<div class="toolButton">
					<fb:bookmark></fb:bookmark>
				</div>
			</div>
			
			<div id="Invite">
				<fb:serverFbml width="740px">
					<script type="text/fbml">
						<fb:fbml>
							<fb:request-form
								method='POST'
								type='join'
								invite='true'
								action="<%= request.getRequestURL().toString() + '?' + request.getQueryString() %>"
								content="<fb:req-choice url='http://apps.facebook.com/doyles_casino/'
										label='Try this app'/>" >
								<fb:multi-friend-selector
									actiontext="Invite your friends to use this application">
							</fb:request-form>
						</fb:fbml>
					</script>
				</fb:serverFbml>
			</div>
			
			<table id="Login" class="login">
				<tr>
					<td class="loginCell">
						Connect with this application to get your messages<br />
						<a href="#" class="button" onclick="FB.login()">Connect</a>
					</td>
				</tr>
			</table>
			<div id="MessageArea">
				<table class="loadingTable">
					<tr>
						<td class="loadingCell">
							<div class="loadingMessage">
								Loading   
								<img src="/cat/facebook/images/fb_loading.gif" />
							</div>
						</td>
					</tr>
				</table>
			</div>
		</div>
	</body>
</html>