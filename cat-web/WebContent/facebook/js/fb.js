var channel_path = '/cat/facebook/xd_receiver.htm'; 
var currentUID;

function fbInit()
{
	var appId = fetchQueryParameter("fb_sig_app_id");
	
	// Initialize facebook API
	FB.init({appId: appId, status: true, cookie: true, xfbml: true});

	// Set canvas size
	FB.Canvas.setSize();

	// Listen for session change events
	FB.Event.subscribe('auth.sessionChange', function(response) {
		handleResponse(response);
	});
	
	// Check the login status
	FB.getLoginStatus(function(response) {
		handleResponse(response);
	});
	
	// Wire events
	if ($("#InviteButton") &&
		$("#Invite") )
	{
		$("#InviteButton").click(function(){
			if ($("#Invite").css("display") == "block")
				$("#Invite").css("display", "none");
			else
				$("#Invite").css("display", "block");
			
			FB.Canvas.setSize();
		});
	}
}
function handleResponse(response)
{
	if (response.session) 
	  {
		  currentUID = response.session.uid;
		  hideLogin();
		  fetchMessages();
		  showBookmark();
		  
		  FB.Canvas.setSize();
	  } 
	  else 
	  {
		  showLogin();
		  hideBookmark();
	  }
}
function showLogin()
{
	$("#LoadingMessage").css("display", "none");
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


