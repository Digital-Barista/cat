function MessageAPI()
{
	var MESSAGE_URL = "/cat/rest/facebook/messages";
	
	this.loadMessages = function(uid)
	{
		// Show an error message if the request fails
		$("#MessageArea").ajaxError(function() {
				showMessage("Your messages cannot be loaded");
		});
		
		// Get app ID from path name
		var parts = window.location.pathname.split("/");
		var last = parts[parts.length - 1];
		if (last.length == 0)
			last = parts[parts.length - 2];
		var app = last;
		
		// Make the message request
		var url = MESSAGE_URL + "/" + app + "/" + uid;
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
		span.html(message);

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
		
		// Add delete button
		var deleteButton = $("<a href='#'>" + "Delete" + "</a>");
		deleteButton.attr("class", "button");
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
		var table = $("<table />");
		table.attr("class", "messageLine");
		
		var row = $("<tr />");
		container.append(table);
		table.append(row);
		
		// Add check box
		var cell = $("<td />");
		cell.attr("class", "selectColumn");
		var checkbox = $("<input type='checkbox' />");
		cell.append(checkbox);
		row.append(cell);

		// Add create date
		cell = $("<td />");
		cell.attr("class", "dateColumn");
		cell.text(message.@formattedCreateDate);
		row.append(cell);
		
		
		// Create div to hold title and body content
		cell = $("<td />");
		row.append(cell);
		var content = $("<div />");
		cell.append(content);
		
		// Add title
		var title = $("<div />");
		title.attr("class", "title");
		title.text(message.@title);
		content.append(title)
		
		// Add body
		var body = $("<div />");
		body.text(message.@body);
		content.append(body);
		
		// Create responses
		if (message.@metadata != null)
		{
			var keywords = message.@metadata.split(",");
			for (var i = 0; i < keywords.length; i++)
			{
				if (keywords[i].length > 0)
				{
					var action = $("<button type='button'>" + keywords[i] + "</button>");
					content.append(action);
				}
			}
		}
		
		// Add delete column
		cell = $("<td />");
		cell.attr("class", "selectColumn");
		var deleteLink = $("<a href='#' />");
		deleteLink.attr("class", "removeButton");
		cell.append(deleteLink);
		row.append(cell);
		
		return container;
	}
}