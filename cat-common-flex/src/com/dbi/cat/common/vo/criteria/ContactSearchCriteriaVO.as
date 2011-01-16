package com.dbi.cat.common.vo.criteria
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.criteria.ContactSearchCriteria")]
	public class ContactSearchCriteriaVO
	{
		public var clientIds:ArrayCollection;
		public var entryTypes:ArrayCollection;
		public var contactTags:ArrayCollection;
	
		public function ContactSearchCriteriaVO()
		{
		}

	}
}