package com.dbi.cat.business
{
	import com.dbi.cat.common.constants.IntervalType;
	import com.dbi.cat.common.vo.CalendarConnectorVO;
	import com.dbi.cat.common.vo.CampaignVO;
	import com.dbi.cat.common.vo.ConnectorVO;
	import com.dbi.cat.common.vo.CouponNodeVO;
	import com.dbi.cat.common.vo.EntryPointVO;
	import com.dbi.cat.common.vo.ImmediateConnectorVO;
	import com.dbi.cat.common.vo.IntervalConnectorVO;
	import com.dbi.cat.common.vo.MessageVO;
	import com.dbi.cat.common.vo.NodeVO;
	import com.dbi.cat.common.vo.ResponseConnectorVO;
	
	import mx.collections.ArrayCollection;
	import mx.formatters.DateFormatter;
	
	[Bindable]
	public class CampaignTestManager
	{
		public static const COLOR_GREEN:String = "#64BF00";
		public static const COLOR_RED:String = "#FF0000";
		public static const COLOR_BLUE:String = "#0000FF";
		public static const COLOR_BLACK:String = "#000000";
		
		
		private var dateFormat:DateFormatter;
		private var currentDate:Date;
		private var startDate:Date;
		private var uidMap:Object;
		private var visitedImmediateConnectors:Object = new Object();
		
		public var currentNode:NodeVO;
		public var testCampaign:CampaignVO;
		public var campaignTestOutput:String;
		public var isTesting:Boolean = false;
			
		private function get adjustedDate():Date
		{
			// Add elapsed time to user supplied start date
			var diff:Number = new Date().time - startDate.time;
			var date:Date = new Date(currentDate.time + diff);
			return date;
		}
		
		public function CampaignTestManager()
		{
			dateFormat = new DateFormatter();
			dateFormat.formatString = "MM/DD/YYYY - J:NN";
		}

		public function startTest(campaign:CampaignVO, start:Date=null):void
		{
			isTesting = true;
			currentDate = start != null ? start : new Date();
			startDate = new Date();
			campaignTestOutput = "";
			currentNode = null;
			testCampaign = campaign;
			
			// Index connectors and nodes by UID
			uidMap = new Object();
			for each (var c:ConnectorVO in testCampaign.connectors)
				uidMap[c.uid] = c;
			for each (var n:NodeVO in testCampaign.nodes)
				uidMap[n.uid] = n;
			
			out("Started campaign: ", campaign.name, COLOR_BLUE);
			nextStep();
		}
		public function endTestCampaign():void
		{
			isTesting = false;
			currentNode = null;
			testCampaign = null;
			campaignTestOutput = "";
		}
		public function receiveResponse(response:String):void
		{
			// If not started find a matching entry point
			if (currentNode == null)
			{
				var match:Boolean = false;
				for each (var node:NodeVO in testCampaign.nodes)
				{
					if (node is EntryPointVO)
					{
						var entry:EntryPointVO = node as EntryPointVO;
						if (entry.valid &&
							entry.keyword.toLowerCase() == response.toLowerCase())
						{
							match = true;
							currentNode = entry;
							out("Response: ", response);
							out("Matched entry point with keyword: ", entry.keyword);
							break;
						}
					}
				}
				if (!match)
				{
					out("Response: ", response);
					out("Did not match any entry points", "", COLOR_RED);
				}
			}
			// If there is a current node look for response connectors
			else
			{
				match = false;
				for each (var uid:String in currentNode.downstreamConnections)
				{
					var connector:ConnectorVO = uidMap[uid];
					if (connector is ResponseConnectorVO)
					{
						var rConnector:ResponseConnectorVO = connector as ResponseConnectorVO;
						if (rConnector.valid &&
							rConnector.keyword.toLowerCase() == response.toLowerCase())
						{
							match = true;
							out("Response: ", response);
							out("Matched response connector with keyword: ", rConnector.keyword);
							
							// Find destination node of this matching response connector
							followConnector(rConnector);
							break;
						}
					}
				}
				if (!match)
				{
					out("Response: ", response);
					out("", "Did not match any response connectors", COLOR_RED);
				}
			}
			nextStep();
		}
		public function followNextTimeConnector():void
		{
			if (currentNode == null)
			{
				out("", "You have not joined the campaign yet", COLOR_RED);
			}
			else
			{
				var nextConnector:ConnectorVO;
				var minDate:Date;
				
				// Find next connector to be fired according to date
				for each (var uid:String in currentNode.downstreamConnections)
				{
					var connector:ConnectorVO = uidMap[uid];
					if (connector is CalendarConnectorVO)
					{
						var cConnector:CalendarConnectorVO = connector as CalendarConnectorVO;
						if (cConnector.valid &&
							cConnector.targetDate.time > adjustedDate.time &&
							(minDate == null ||
							cConnector.targetDate.time < minDate.time) )
						{
							minDate = cConnector.targetDate;
							nextConnector = cConnector;
						}
					}
					else if (connector is IntervalConnectorVO)
					{
						var iConnector:IntervalConnectorVO = connector as IntervalConnectorVO;
						var intervalDate:Date = addInterval(currentDate, iConnector);
						if (iConnector.valid &&
							(minDate == null ||
							 intervalDate.time < minDate.time) )
						{
							minDate = intervalDate;
							nextConnector = iConnector;		
						}
					}
				}
				
				// Follow connector if one was found
				if (nextConnector != null)
				{
					currentDate = minDate;
					startDate = new Date();
					if (nextConnector is IntervalConnectorVO)
						out("", "Following interval connector", COLOR_BLUE);
					else
						out("", "Following date connector", COLOR_BLUE);
						
					followConnector(nextConnector);
				}
				else
				{
					out("", "There are no time based connectors to follow", COLOR_RED);
				}
			}
		}
		
		private function addInterval(date:Date, interval:IntervalConnectorVO):Date
		{
			var time:Number = 0;
			switch(interval.intervalType)
			{
				case IntervalType.MINUTES:
					time = interval.interval * 60 * 1000;	
					break;
				case IntervalType.HOURS:
					time = interval.interval * 60 * 60 * 1000;
					break;
				case IntervalType.DAYS:
					time = interval.interval * 24 * 60 * 60 * 1000;	
					break;
				case IntervalType.WEEKS:
					time = interval.interval * 7 * 24 * 60 * 60 * 1000;	
					break;
				case IntervalType.MONTHS:
					var years:Number = Math.floor(interval.interval / 12);
					var months:Number = interval.interval % 12;
					var temp:Date = new Date(date.fullYear + years, date.month + months, date.date, date.hours, date.minutes, date.seconds);
					time = temp.time - date.time;
					break;
			}
			
			return new Date(date.time + time);
		}
		private function followConnector(connector:ConnectorVO):void
		{
			var node:NodeVO = uidMap[connector.destinationUID];
			if (node != null)
			{
				currentNode = node;
				
				// Send message for this node if appropriate
				if (currentNode is MessageVO)
				{
					var message:MessageVO = currentNode as MessageVO;
				
					if (message.valid)
						out("Sending message: ", message.message);
					else
					{
						out("Invalid message: ", message.name, COLOR_RED);
					}
				}
				// Handle coupon messages
				else if (currentNode is CouponNodeVO)
				{
					var coupon:CouponNodeVO = currentNode as CouponNodeVO;
				
					// Make sure required fields are filled
					if (coupon.valid)
					{
						// Determine message by unavailable date
						if (coupon.unavailableDate != null &&
							coupon.unavailableDate.time < adjustedDate.time)
						{
							out("Sending coupon unavailable message: ", coupon.unavailableMessage);
						}
						else
						{
							out("Sending coupon available message: ", coupon.availableMessage);
						}
					}
					else
					{
						out("Invalid message: ", coupon.name, COLOR_RED);
					}
				}
				nextStep();
			}
			else
			{
				out("", "There are no more connectors to follow", COLOR_RED);
			}
		}
		private function nextStep():void
		{
			// If not started look for entry points
			if (currentNode == null)
			{
				var count:Number = 0;
				for each (var node:NodeVO in testCampaign.nodes)
				{
					if (node is EntryPointVO &&
						EntryPointVO(node).valid)
						count++;
				}
				
				if (count > 0)
					out("", "Waiting to enter one of " + count + " valid entry points", COLOR_BLUE);
				else
					out("", "There are no valid entry points", COLOR_RED);
			}
			else
			{
				// Look at all connectors on this node and determine next step
				for each (var uid:String in currentNode.downstreamConnections)
				{
					var connector:ConnectorVO = uidMap[uid];
					
					// Ignore invalid connectors
					if (connector.valid)
					{
						if (connector is ImmediateConnectorVO)
						{
							if (visitedImmediateConnectors[connector.uid] != null)
							{
								out("", "LOOP DETECTED! CANNOT CONTINUE", COLOR_RED);
							}
							else
							{
								visitedImmediateConnectors[connector.uid] = connector.uid;
								out("", "Following immediate connector", COLOR_BLUE);
								followConnector(connector);
							}
							
							// Clear visited connectors
							visitedImmediateConnectors = new Object();
							break;
						}
					}
				}
			}
		}
		
		private function out(message:String, description:String, color:String=COLOR_GREEN):void
		{
			campaignTestOutput += 
						"<font font-weight='bold' color='" + COLOR_BLACK + "'>" +
							dateFormat.format(adjustedDate) + 
						"</font><br />" +
			
			 				message +
						"<font color='" + color + "'>" + 
						 	description + 
						"</font><br/><br />";
		}
	}
}