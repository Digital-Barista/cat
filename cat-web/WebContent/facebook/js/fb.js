var channel_path = '/cat/facebook/xd_receiver.htm'; 
var currentUID;

function fbInit()
{
	var appId = fetchQueryParameter("fb_sig_app_id");
	console.log("appId: " + appId);
	
	// Initialize facebook API
	FB.init({appId: appId, status: true, cookie: true, xfbml: true});

	// Start resize timer
	window.fbAsyncInit = function() {
		  FB.Canvas.setAutoResize();
		}

	// Listen for session change events
	FB.Event.subscribe('auth.sessionChange', function(response) {
		console.log(response);
		handleResponse(response);
	});
	
	// Check the login status
	FB.getLoginStatus(function(response) {
		handleResponse(response);
		});
}
function handleResponse(response)
{
	if (response.session) 
	  {
		  currentUID = response.session.uid;
		  hideLogin();
		  fetchMessages();
		  showBookmark();
	  } 
	  else 
	  {
		  showLogin();
		  hideBookmark();
	  }
}
function showLogin()
{
	$("#Login").css("display", "block");
	$("#MessageArea").css("display", "none");
}
function hideLogin()
{
	$("#Login").css("display", "none");
	$("#MessageArea").css("display", "block");
}
function showBookmark()
{
	$("#Bookmark").css("display", "block");
}
function hideBookmark()
{
	$("#Bookmark").css("display", "none");
}

function fetchMessages()
{
	var messageApi = new MessageAPI();
	messageApi.loadMessages();
	
}

function fetchQueryParameter(name)
{
	var query = window.location.search;
	
	var parts = query.split("&");
	for (var i = 0; i < parts.length; i++)
	{
		var param = parts[i].split("=");
		if (param.length == 2 &&
			param[0] == name)
		{
			return param[1];
		}
	}
	return null;
}

//FB_RequireFeatures(["Api"], function(){ 
//	// Create an ApiClient object, passing app's API key and 
//	// a site relative URL to xd_receiver.htm 
//	FB.Facebook.init(api_key, channel_path); 
//
//	 
//	var api = FB.Facebook.apiClient; 
//
//	FB.Connect.requireSession(function(){
//		FB.CanvasClient.startTimerToSizeToContent();
//		var uid = api.get_session().uid;
//		var messageApi = new MessageAPI();
//		messageApi.loadMessages(uid);
//	},null, true);
	
	
	
	// require user to login 
//	api.requireLogin(function(exception){ 
//
//		if (exception)
//		{
//			alert(exception);
//		}
//		else
//		{
//			FB.CanvasClient.startTimerToSizeToContent();
//			var uid = api.get_session().uid;
//			 var messageApi = new MessageAPI();
//			 messageApi.loadMessages(uid);
//		}
//		 
//	}); 
//});  



//FB.ensureInit(function () {
//	FB.Facebook.apiClient.fql_query("SELECT uid, name FROM user WHERE uid IN (SELECT uid2 FROM friend WHERE uid1 = 100000679573930)",
//            function(rows) {
//				var s = "";
//              for (i in rows)
//              {
//            	  s += rows[i].uid + ", " + rows[i].name + "\n";
//              }
//              alert(s);
//            });
//
//  });


