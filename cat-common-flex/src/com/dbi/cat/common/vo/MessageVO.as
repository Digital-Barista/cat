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
		public var messageType:String;
		public var type:String;
		
		override public function get valid():Boolean
		{
			return message != null &&
				message.length > 0;
		}
	}
}