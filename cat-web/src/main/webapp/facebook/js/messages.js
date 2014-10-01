
function MessageAPI()
{
	var messageAPI = this;
	
	var MESSAGE_URL = "/cat/unsecure/rest/facebook/messages";
	var CHECKBOX_PREFIX = "message_select_";
	var MESSAGE_LINE_PREFIX = "message_";

	var signedRequest =  dbi.signedRequest || document.getElementById('signedRequest').value;
	
	// Add click events
	setupControls();
	
	this.loadMessages = function()
	{
		// Show loading screen
		var loading = $("#LoadingMessage");
		loading.css("display", "block");

		
		// Hide no message content
		var noMessage = $("#NoMessagesDiv");
		
		// Show an error message if the request fails
		$("#MessageArea").ajaxError(function() {
			loading.css("display", "none");
			showError("An error occurred. Please refresh the page to try again.");
		});
		
		// Get app ID from querystring
		var appName = $.query.get("app_name");
		if (!appName){
			appName = $.query.get("app_id");
		}
		
		// Make the message request
		var url = MESSAGE_URL + "/list/" + appName  + "/" + window.location.search
		$.ajax({
			url:url,
			data:{'uid':dbi.currentUID, 'signedRequest':signedRequest},
			dataType:'json',
			type:'POST',
			success: function(data){
				// Clear message area
				var area = $("#MessageListDiv");
				area.html("");
				
				// Hide loading message
				loading.css("display", "none");
				
				// No messages
				if (data == null ||
					data.length == 0)
				{
					area.html(noMessage.html());
				}
				else
				{
					// Show message area
					area.css("display", "block");
					
					// Add container
					var container = $("<div />");
					container.attr("class", "messageContainer");
					area.append(container);
					
					// Create row for each message
					for (var i = 0; i < data.length; i++)
					{
						var o = data[i];
					   var row = createRow(o);
					   container.append(row);
					}
					
					// Add tracking events
					setupLinkTracking();
				}
	
				// Resize frame after messages have loaded
				FB.Canvas.setSize({ width: 750 });
			 }
			});
	};
	
	function setupLinkTracking(){
		$('.messageContainer a:not(.button)').click(function(){
			_gaq.push(['_trackEvent', 'Message', 'Link', 'fbid=' + dbi.currentUID + ',link=' + $(this).attr('href')]);
		});
	}
	
	function respond(messageId, response)
	{
		// Show loading
		showResponseLoading(messageId);
		
		// Build URL
		var url = MESSAGE_URL + "/" + messageId + "/" + response + "/" + window.location.search;
		
		// Make request
		$.ajax({
			  url: url,
			  type: 'POST',
			  dataType: 'json',
			  data:{'uid': dbi.currentUID, 'signedRequest':signedRequest},
			  success: function(data) {
			    updateMessage(data);
			  }
			});
	}
	
	function showDeleteLoading(messageId)
	{
		var line = $("#" + MESSAGE_LINE_PREFIX + messageId);
		var loading = $("<img />");
		loading.attr("src", "/cat/facebook/images/fb_loading.gif");
		
		line.find(".removeButton").replaceWith(loading);
	}
	
	function showResponseLoading(messageId)
	{
		var line = $("#" + MESSAGE_LINE_PREFIX + messageId);
		var loading = $("<img />");
		loading.attr("src", "/cat/facebook/images/fb_loading.gif");
		
		line.find(".keywords").replaceWith(loading);
	}
	
	function updateMessage(message)
	{
		var line = $("#" + MESSAGE_LINE_PREFIX + message.facebookMessageId);
		var newLine = createRow(message);
		line.replaceWith(newLine);
	}
	
	function deleteMessage (messageId)
	{
		// Show loading
		showDeleteLoading(messageId);
		
		// Build URL
		var url = MESSAGE_URL + "/" + messageId + "/" + window.location.search;
		
		// Make request
		$.ajax({
			  url: url,
			  type: 'POST',
			  dataType: 'json',
			  data:{'uid': dbi.currentUID, 'signedRequest':signedRequest},
			  success: function(data) {
			    removeMessage(messageId);
			  }
			});
	}
	
	function deleteSelected()
	{
		$("input:checked").each(function (){
			var id = $(this).attr("id");
			if (id.indexOf(CHECKBOX_PREFIX) == 0)
			{
				var messageId = id.substring(CHECKBOX_PREFIX.length);
				deleteMessage(messageId);
			}
		});
	}
	
	function refreshMessages()
	{
		messageAPI.loadMessages();
	}
	
	function removeMessage(messageId)
	{
		var line = $("#" + "message_" + messageId);
		line.remove();
	}
	
	function showError(message)
	{
		// Use a table to center message content
		var div = $("<div />");
		div.attr("class", "errorMessage");
		div.html(message);
		
		// Replace content area with message
		var area = $("#MessageListDiv");
		area.html("");
		area.append(div);
	}
	
	
	function setupControls()
	{
		// Wire refresh button
		var refresh = $("#RefreshButton");
		refresh.bind("click", function(){
			refreshMessages();
		});
		
		// Wire delete button
		var deleteButton = $("#DeleteButton");
		deleteButton.bind("click", function(){
			deleteSelected();
		});
		

		// Add select all/none links
		var all = $("#SelectAll");
		all.bind('click', function() {
			selectCheckboxes(true);
		});

		var none = $("#SelectNone");
		none.bind('click', function() {
			selectCheckboxes(false);
		});
		
	}
	
	
	function selectCheckboxes(all)
	{
		$('input:checkbox').attr("checked", all);
	}
	
	function createRow(message)
	{
		var container = $("<div />");
		container.attr("id", MESSAGE_LINE_PREFIX + message.facebookMessageId);
		
		var table = $("<table />");
		table.attr("class", "messageLine");
		
		var row = $("<tr />");
		container.append(table);
		table.append(row);
		
		// Add check box
		var cell = $("<td />");
		cell.attr("class", "selectColumn");
		var checkbox = $("<span class='custom-checkbox'><input type='checkbox' /><span class='box'><span class='tick'></span></span></span>");
		checkbox.find('input').attr("id", CHECKBOX_PREFIX + message.facebookMessageId);
		cell.append(checkbox);
		row.append(cell);
		
		// Create div to hold title and body content
		cell = $("<td />");
		cell.attr("class", "messageColumn");
		row.append(cell);
		var content = $("<div />");
		cell.append(content);
		
		// Add title
		var title = $("<div />");
		title.attr("class", "title");
		title.text(message.title);
		content.append(title);

    // Add title
    var date = $("<div />");
    date.attr("class", "date");
    date.text(message.formattedCreateDate);
    content.append(date);
		
		// Add body
		var body = $("<div />");
		body.attr("class", "messageBody");
		body.html(message.body);
		content.append(body);
		
		// Show response if one has been submitted
		if (message.response != null)
		{
			var response = $("<span />");
			response.attr("class", "responseContent");
			response.text("Responded: " + message.response);
			content.append(response);
		}
		// Create responses
		else if (message.metadata != null)
		{
			var keywordDiv = $("<div />");
			keywordDiv.attr("class", "keywords");
			
			// Create buttons for each keyword
			var keywords = message.metadata.split(",");
			for (var i = 0; i < keywords.length; i++)
			{
				if (keywords[i].length > 0)
				{
					var action = $("<a href='#'>" + keywords[i] + "</a>");
					action.attr("class", "button");
					action.bind('click', {messageId: message.facebookMessageId, response: keywords[i]}, function(event) {
						_gaq.push(['_trackEvent', 'Message', 'Respond', 'fbid=' + dbi.currentUID + 
						           ',messageId=' + event.data.messageId + ',response=' + event.data.response]);
						respond(event.data.messageId, event.data.response);
					});
					keywordDiv.append(action);
				}
			}
			
			// Add the div if there are any keywords
			if (keywords.length > 0)
				content.append(keywordDiv);
		}
		
		// Add print column
		cell = $("<td />");
		cell.attr("class", "buttonColumn");
		var printLink = $("<a href='#'>Print</a>");
		printLink.attr("class", "button");
		
		// Add print call
		printLink.bind('click', {messageId: message.facebookMessageId}, function(event) {
			_gaq.push(['_trackEvent', 'Message', 'Print', 
			           'fbid=' + dbi.currentUID + ',messageId=' + event.data.messageId]);
			PrintUtil.showCoupon(event.data.messageId);
		});
		

		// Add print cell
		cell.append(printLink);
		row.append(cell);
		
		// Add delete column
		cell = $("<td />");
		cell.attr("class", "buttonColumn");
		var deleteLink = $("<a href='#'>Delete</a>");
		deleteLink.attr("class", "button");
		
		
		// Add delete call
		deleteLink.bind('click', {messageId: message.facebookMessageId}, function(event) {
			_gaq.push(['_trackEvent', 'Message', 'Delete', 
			           'fbid=' + dbi.currentUID + ',messageId=' + event.data.messageId]);
			deleteMessage(event.data.messageId);
		});
		
		cell.append(deleteLink);
		row.append(cell);
		
		return container;
	}
}