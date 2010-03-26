function MessageAPI()
{
	var MESSAGE_URL = "/cat/facebook/get_messages";
	
	this.loadMessages = function(uid)
	{
		var url = MESSAGE_URL + "/" + uid;
		$.getJSON(url, function(data){
			
			// Clear message area
			var area = $("#MessageArea");
			area.html("");
			
			// Add the control bar
			var controls = createControls();
			area.append(controls);
			
			// Use a table to hold the list
			var table = $("<table />");
			table.attr("class", "messageTable");
			$("#MessageArea").append(table);
			
			// Add the header row
			var header = createHeader();
			table.append(header);
			
			// Create row for each message
			for (var i = 0; i < data.length; i++)
			{
				var o = data[i];
			   var row = createRow(o.message);
			   table.append(row);
			}
			
			// No messages
			if (data == null ||
				data.length == 0)
			{
				var empty = $("<div />");
				empty.attr("class", "empty");
				empty.text("You currently have no messages");
				
				$("#MessageArea").append(empty);
			}
		 });
	}
	
	function createControls()
	{
		var controls = $("<div />");
		controls.attr("class", "controlBar");
		
		var deleteButton = $("<button type='button'>Delete</button>");
		controls.append(deleteButton);
		
		return controls;
	}
	
	function createHeader()
	{
		var row = $("<tr />");
		row.attr("class", "headerRow");
		
		var cell = $("<td />");
		row.append(cell);
		
		// Add select all/none links
		var all = $("<a />");
		all.text("All");
		all.attr("href", "#");
		cell.append(all);
		
		var space = $("<span />");
		space.text(" ");
		cell.append(space);

		var none = $("<a />");
		none.text("None");
		none.attr("href", "#");
		cell.append(none);
		
		// Add received date
		cell = $("<td />");
		cell.text("Received");
		row.append(cell);
		
		// Add message column
		cell = $("<td />");
		cell.text("Message");
		row.append(cell);

		// Add action column
		cell = $("<td />");
		cell.text("Respond");
		row.append(cell);
		
		return row;
	}
	
	function createRow(message)
	{
		
		var row = $("<tr />");
		
		// Add check box
		var cell = $("<td />");
		var checkbox = $("<input type='checkbox' />");
		cell.append(checkbox);
		row.append(cell);

		// Add create date
		cell = $("<td />");
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
			cell = $("<td />");
			row.append(cell);
			
			var keywords = message.@metadata.split(",");
			for (var i = 0; i < keywords.length; i++)
			{
				var action = $("<button type='button'>" + keywords[i] + "</button>");
				cell.append(action);
			}
		}
		
		return row;
	}
}