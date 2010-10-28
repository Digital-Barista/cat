package com.dbi.cat.common.vo
{
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.MessageNode")]
	public class MessageVO extends NodeVO
	{
		public function MessageVO()
		{
			super();
		}
		
		public var message:String;
		public var messages:Object;
		public var messageType:String;
		public var type:String;
		
		override public function get valid():Boolean
		{
			if ( message != null &&
				message.length > 0)
				return true;
			
			if (messages != null)
			{
				for (var key:String in messages)
				{
					if (messages[key] != null &&
						messages[key].length > 0)
						return true;
				}
			}
			
			return false;
		}
	}
}