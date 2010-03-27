var channel_path = '/xd_receiver.htm'; 

FB_RequireFeatures(["Api"], function(){ 
	// Create an ApiClient object, passing app's API key and 
	// a site relative URL to xd_receiver.htm 
	FB.Facebook.init(api_key, channel_path); 

	var api = FB.Facebook.apiClient; 

	
	// require user to login 
	api.requireLogin(function(exception){ 

		var uid = api.get_session().uid;
		
//		 FB.Facebook.apiClient.fql_query("SELECT name, pic FROM user WHERE uid=" + uid,
//				 function(rows) {
//				   var content = "Hello, " + rows[0].name + " your UID is: " + uid;
//				   updateMessageArea(content);
//		 });
		 
 
		 var messageApi = new MessageAPI();
		 messageApi.loadMessages(uid);
	}); 
}); 

function updateMessageArea(content)
{
	$('#MessageArea').html(content);
}