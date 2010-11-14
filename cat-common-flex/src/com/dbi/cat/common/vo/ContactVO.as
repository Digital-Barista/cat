package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.Contact")]
	public class ContactVO
	{
		public var contactId:String;
		public var clientId:String;
		public var type:String; // Contact Type = EntryPointType
		public var address:String;
		public var createDate:Date;
		public var contactTags:ArrayCollection;
		public var blacklisted:Boolean;
		public var UID:String;
		
		private var _tagListLabel:String;
		public function get tagListLabel():String
		{
			if (_tagListLabel == null)
			{
				_tagListLabel = "";
				for each (var tag:ContactTagVO in contactTags)
				{
					if (_tagListLabel.length > 0)
						_tagListLabel += ", ";
					_tagListLabel += tag.tag;
				}
			}
			return _tagListLabel;
		}
		public function set tagListLabel(value:String):void
		{
			_tagListLabel = value;
		}
		
		public function ContactVO()
		{
		}

	}
}