var channel_path = '/xd_receiver.htm'; 

FB_RequireFeatures(["Api"], function(){ 
	// Create an ApiClient object, passing app's API key and 
	// a site relative URL to xd_receiver.htm 
	FB.Facebook.init(api_key, channel_path); 

	var api = FB.Facebook.apiClient; 

	
	// require user to login 
	api.requireLogin(function(exception){ 

		var uid = api.get_session().uid;
		 var messageApi = new MessageAPI();
		 messageApi.loadMessages(uid);
		 
		 FB.CanvasClient.startTimerToSizeToContent();
	}); 
}); 

function updateMessageArea(content)
{
	$('#MessageArea').html(content);
}