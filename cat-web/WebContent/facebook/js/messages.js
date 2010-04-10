

function MessageAPI()
{
	var messageAPI = this;
	
	var MESSAGE_URL = "/cat/unsecure/rest/facebook/messages";
	var CHECKBOX_PREFIX = "message_select_";
	var MESSAGE_LINE_PREFIX = "message_";
	
	var currentUID;
	
	this.loadMessages = function(uid)
	{
		// Save UID
		currentUID = uid;
		
		// Show loading screen
		var loading = $("<img />");
		loading.attr("src", "/cat/facebook/images/fb_loading.gif");
		var loadContainer = $("<div>Loading Messages  </div>");
		loadContainer.append(loading);
		showMessage(loadContainer);
		
		// Show an error message if the request fails
		$("#MessageArea").ajaxError(function() {
				showMessage("An error occurred. Please refresh the page to try again.");
		});
		
		// Get app ID from path name
		var parts = window.location.pathname.split("/");
		var last = parts[parts.length - 1];
		if (last.length == 0)
			last = parts[parts.length - 2];
		var app = last;
		
		// Make the message request
		var url = MESSAGE_URL + "/list/" + app + "/" + uid + "/" + window.location.search;
		$.getJSON(url, function(data){

			// Clear message area
			var area = $("#MessageArea");
			area.html("");
			
			// No messages
			if (data == null ||
				data.length == 0)
			{
				showMessage("You currently have no messages");
			}
			else
			{
				// Add banner
				var banner = $("<h2>Messages</h2>");
				area.append(banner);
				
				// Add header
				var header = createHeader();
				area.append(header);

				
				// Add the control bar
				var controls = createControls();
				area.append(controls);

				// Add container
				var container = $("<div />");
				container.attr("class", "messageContainer");
				area.append(container);
				
				// Create row for each message
				for (var i = 0; i < data.length; i++)
				{
					var o = data[i];
				   var row = createRow(o.message);
				   container.append(row);
				}

				// Add footer
				var footer = createFooter();
				area.append(footer);
			}
			
		 });
	}
	
	function respond(messageId, response)
	{
		// Show loading
		showResponseLoading(messageId)
		
		// Build URL
		var url = MESSAGE_URL + "/" + messageId + "/" + response + "/" + window.location.search;
		
		// Make request
		$.ajax({
			  url: url,
			  type: 'PUT',
			  dataType: 'json',
			  success: function(data) {
			    updateMessage(data.message);
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
			  type: 'DELETE',
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
		if (currentUID != null)
			messageAPI.loadMessages(currentUID);
	}
	
	function removeMessage(messageId)
	{
		var line = $("#" + "message_" + messageId);
		line.remove();
	}
	
	function showMessage(message, className)
	{
		// Use a table to center message content
		var table = $("<table />");
		table.attr("class", "messageTable");
		
		var row = $("<tr />");
		var cell = $("<td />");
		cell.attr("class", "messageCell");
		
		var span = $("<span />");
		if (className != null)
			span.attr("class", className);
		span.append(message);

		cell.append(span);
		row.append(cell);
		table.append(row);
		
		// Replace content area with message
		var area = $("#MessageArea");
		area.html("");
		area.append(table);
	}
	
	function createHeader()
	{
		var header = $("<div />");
		header.attr("class", "header");
		

		// Add refresh button
		var refresh = $("<a href='#'>" + "Refresh" + "</a>");
		refresh.attr("class", "button");
		refresh.bind("click", function(){
			refreshMessages();
		});
		header.append(refresh);
		
		// Add delete button
		var deleteButton = $("<a href='#'>" + "Delete" + "</a>");
		deleteButton.attr("class", "button");
		deleteButton.bind("click", function(){
			deleteSelected();
		});
		header.append(deleteButton);
		
		return header;
	}

	function createFooter()
	{
		var footer = $("<div />");
		footer.attr("class", "footer");
		return footer;
	}
	
	function createControls()
	{
		var controls = $("<div />");
		controls.attr("class", "controlBar");
		

		// Add select all/none links
		var links = $("<span />");
		links.text("Select: ");
		controls.append(links);
		
		var all = $("<a />");
		all.text("All");
		all.attr("href", "#");
		controls.append(all);
		all.bind('click', function() {
			selectCheckboxes(true);
		});

		
		var space = $("<span />");
		space.text(", ");
		controls.append(space);

		var none = $("<a />");
		none.text("None");
		none.attr("href", "#");
		controls.append(none);
		none.bind('click', function() {
			selectCheckboxes(false);
		});
		
		return controls;
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
		var checkbox = $("<input type='checkbox' />");
		checkbox.attr("id", CHECKBOX_PREFIX + message.facebookMessageId);
		cell.append(checkbox);
		row.append(cell);

		// Add create date
		cell = $("<td />");
		cell.attr("class", "dateColumn");
		cell.text(message.formattedCreateDate);
		row.append(cell);
		
		
		// Create div to hold title and body content
		cell = $("<td />");
		row.append(cell);
		var content = $("<div />");
		cell.append(content);
		
		// Add title
		var title = $("<div />");
		title.attr("class", "title");
		title.text(message.title);
		content.append(title)
		
		// Add body
		var body = $("<div />");
		body.text(message.body);
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
						respond(event.data.messageId, event.data.response);
					});
					keywordDiv.append(action);
				}
			}
			
			// Add the div if there are any keywords
			if (keywords.length > 0)
				content.append(keywordDiv);
		}
		
		// Add delete column
		cell = $("<td />");
		cell.attr("class", "selectColumn");
		var deleteLink = $("<a href='#' />");
		deleteLink.attr("class", "removeButton");
		
		
		// Add delete call
		deleteLink.bind('click', {messageId: message.facebookMessageId}, function(event) {
			deleteMessage(event.data.messageId);
		});
		
		cell.append(deleteLink);
		row.append(cell);
		
		return container;
	}
}