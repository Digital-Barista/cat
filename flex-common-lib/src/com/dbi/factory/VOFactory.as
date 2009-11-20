package com.dbi.factory
{
	import com.dbi.util.VOUtil;
	
	import flash.utils.describeType;
	import flash.utils.getDefinitionByName;
	import flash.xml.XMLDocument;
	import flash.xml.XMLNode;
	import flash.xml.XMLNodeType;
	
	import mx.collections.ArrayCollection;
	import mx.rpc.xml.SimpleXMLDecoder;
	import mx.utils.ObjectUtil;
	
	/**
	 * This class will take an object and try to map it to the Class type specified.
	 * Any sub property of the Class type specified that implements the IValueObject
	 * interface will be decoded into its proper type. The following metadata tags may
	 * be added to properties in the Class type specified to change the behavior 
	 * of the mapping:
	 * 
	 * NOTE: It is required to add -keep-as3-metadata+=Property to the compiler options or
	 * the propery metadata will be lost during compile time and decoding and encoding using the 
	 * factory will not function correctly.
	 * 
	 * <p>
	 * [Property]
	 * </p>
	 * <ul>
	 * <li>
	 * name="PropertyName" -   Will look for a property of name 
	 * 			"PropertyName" on the source object to copy. A '.' delimeter may be used
	 * 			in the property name to traverse the object hierarchy.  If this attribute is
	 * 			not specified the name of the variable will be used to look up the value on the
	 * 			source object.
	 * <br />
	 * <br />
	 * </li>
	 * <li>
	 * ignore=[encode|decode|all] - Will ignore this property during encoding, decoding, or both
	 * <br />
	 * <br />
	 * </li>
	 * <li>
	 * field=[XML|Attribute] - When encoding an object if the encodeFunction is
	 * 			specified and the field attribute is set to "XML" the encoder will assume that
	 * 			an XMLNode will be returned from the function and will add it to the encoded
	 * 			node being returned without any additional encoding being performed on its properties.
	 * <br />
	 * <br />
	 * </li>
	 * <li>
	 * decodeFunction="FunctionName" - Will attempt to call a function
	 * 			by the name of "FunctionName" and pass the value of the variable on the source
	 * 			object as a parameter when decoding an object.  Any value returned from this 
	 * 			function will be copied to the destination object and will NOT be decoded itself.
	 * <br />
	 * <br />
	 * </li>
	 * <li>
	 * encodeFunction="FunctionName" - When encoding an object will attempt to call a function 
	 * 			by the name of "FunctionName" on the object to encode passing in the encoded object
	 * 			property value as a parameter.  If the field attribute is set to XML the return type
	 * 			will be an XMLNode otherwise it will be of type Object.
	 * <br />
	 * <br />
	 * </li>
	 * <li>
	 * classType="ClassName" - May be placed on an ArrayCollection which will 
	 * 			try to decode all objects within the array collection on the source to
	 * 			the class type specified.  The class name given must be fully qualified.
	 * </li>
	 * </ul>
	 * @author khoyt2
	 * 
	 */	
	public class VOFactory
	{
		public static const FIELD_XML:String = "XML";
		public static const FIELD_ATTRIBUTE:String = "Attribute";
		
		public static const IGNORE_ENCODE:String = "encode";
		public static const IGNORE_DECODE:String = "decode";
		public static const IGNORE_ALL:String = "all";
		
		private static var classInfoMap:Object = new Object();
		private static var voPropertyMap:Object = new Object();
		
		public function VOFactory()
		{
		}
		
		/**
		 * Takes a value object and converts it to XML with a root node with the
		 * name of "nodeName"
		 *  
		 * @param source Object fitting the IValueObject interface to serialize
		 * @param nodeName Name of the root element of the returned XMLNode
		 * 
		 * @return A new XMLNode serialized from the source object
		 * 
		 */		
		public static function encode(source:IVOFactoryObject, nodeName:String):XMLNode
		{
			var newNode:XMLNode = new XMLNode(XMLNodeType.ELEMENT_NODE, nodeName);
			if (source != null)
			{
				var classInfo:Object = ObjectUtil.getClassInfo(source);
				var properties:ArrayCollection = getPropertyList(getDefinitionByName(classInfo.name) as Class);
				
				for each (var property:VOProperty in properties)
				{
					// Ignore this property if the ignore flag is set
					if (property.ignore != IGNORE_ENCODE && property.ignore != IGNORE_ALL)
					{
						// If the property is encoding its own XML don't try to encode it
						if (property.encodeFunctionName != null &&
							property.field != null &&
							property.field.toLocaleLowerCase() == FIELD_XML.toLocaleLowerCase())
						{
							newNode.appendChild(source[property.encodeFunctionName](source[property.voName]));
						}
						else
						{
							var node:XMLNode = newNode;
							var voParts:Array = property.serviceName.split(".");
							
							// Create sub nodes for "." delimited properties
							for (var i:int = 0; i < voParts.length; i++)
							{
								var part:String = voParts[i];
								
								// Add sub nodes until last part is reached
								if (i < voParts.length - 1)
								{
									var subNode:XMLNode = new XMLNode(XMLNodeType.ELEMENT_NODE, voParts[i]);
									node.appendChild(subNode);
									node = subNode;
								}
								else
								{
									// Evaluate properties with encode functions
									if (property.encodeFunctionName != null)
									{
										node.attributes[part] = source[property.encodeFunctionName](source[property.voName]);
									}
									// Add each item in typed collections
									else if (property.collectionClassType != null)
									{
										for each (var o:IVOFactoryObject in source[property.voName])
										{
											node.appendChild(encode(o, part));
										}
									}
									// Add IValueObjects as sub nodes
									else if (property.isValueObject)
									{
										node.appendChild(encode(IVOFactoryObject(source[property.voName]), part));
									}
									else
									{
										node.attributes[part] = source[property.voName];
									}
								}
							}
						}
					}
				}
			}
			return newNode;
		}
		
		/**
		 * Take a collection of objects and convert it to XML
		 *  
		 * @param collection ArrayCollection of <code>IVOFactoryObject</code> objects
		 * @param collectionNodeName Name of root node that will be created holding the list of object nodes
		 * @param itemNodeName Name of node for each object in the collection
		 * 
		 * @return XMLNode containing the serialized collection
		 * 
		 */		
		public static function encodeCollection(collection:ArrayCollection, 
			collectionNodeName:String, itemNodeName:String):XMLNode
		{
			var xml:XMLNode = new XMLNode(XMLNodeType.ELEMENT_NODE, collectionNodeName);
			
			for each (var item:IVOFactoryObject in collection)
			{
				xml.appendChild(encode(item, itemNodeName));
			}	
			return xml;
		}
		
		/**
		 * Take an object and if it is a collection decode each object otherwise just
		 * decode the object and add it to a new collection
		 * 
		 * @param collection <code>ArrayCollection</code> to decode to <code>ArrayCollection</code>
		 * 				of <code>collectionClassType</code> objects
		 * @param collectionClassType Class to use for creating objects
		 * 
		 * @return New collection with decoded objects of type collectionClassType
		 */
		public static function decodeCollection(collection:Object, collectionClassType:Class):ArrayCollection
		{
			var ret:ArrayCollection = new ArrayCollection();
			if (collection != null &&
				collectionClassType != null)
			{
				if (collection is ArrayCollection)
					for each (var o:Object in collection)
						ret.addItem(decode(o, collectionClassType));
				else
					ret.addItem(decode(collection, collectionClassType));
			}
			return ret;
		}
		
		/**
		 * Create a new object of type returnType and copy properties into it
		 * according to the metadata attributes.
		 * 
		 * @param source Object to be decoded
		 * @param returnType Class type that should be returned
		 * 
		 * @return New class of type returnType
		 */
		public static function decode(source:Object, returnType:Class):Object
		{
			var destination:Object = new returnType();
			if (source != null)
			{
				var propertyList:ArrayCollection = getPropertyList(returnType);
				
				for each (var property:VOProperty in propertyList)
				{
					// Don't decode the propery if the ignore flag is set
					if (property.ignore != IGNORE_DECODE && property.ignore != IGNORE_ALL)
					{
						var sourceValue:Object = getPropertyValue(source, property.serviceName);
						
						// If source value is supposed to be an array collection make sure it is
						if (property.voClassType == ArrayCollection &&
							sourceValue != null)
							sourceValue = VOUtil.getCollection(sourceValue);
							
						if (property.decodeFunctionName != null)
						{
							destination[property.voName] = destination[property.decodeFunctionName](sourceValue);
						}
						else if (sourceValue != null)
						{
							// If this is a typed ArrayCollection copy each element into a new collection
							if (property.collectionClassType != null)
							{
								destination[property.voName] = decodeCollection(sourceValue, property.collectionClassType);
							}
							// If this is an IVOFactoryObject try to decode the property
							else if (property.isValueObject)
							{
								destination[property.voName] = decode(sourceValue, property.voClassType);
							}
							else
							{
								destination[property.voName] = sourceValue;
							}
						}
					}
				}
			}
			return destination;
		}
		
		/**
		 * Use reflection to look up info about the classType and fill in
		 * a map with information needed for decoding
		 * 
		 * @param classType Class to return type information about
		 * 
		 * @return New array collection holding a list of object with property info
		 */
		private static function getPropertyList(classType:Class):ArrayCollection
		{
			if (voPropertyMap[classType] == null)
			{
				var info:Object = getClassInfo(classType);
				
				// Get list of all variables and accessors
				var properties:ArrayCollection = new ArrayCollection();
				for each (var o:Object in info.type.accessor)
					properties.addItem(o);
				for each (o in info.type.variable)
					properties.addItem(o);
					
				// Build a list of property mappings
				var newMap:ArrayCollection = new ArrayCollection();
				for each (var property:Object in properties)
				{
					var propMap:VOProperty = new VOProperty();
					propMap.voName = property.name;
					propMap.voClassType = getDefinitionByName(property.type) as Class;
					
					// Use the name of the property if a "Property" attribute wasn't specified
					propMap.serviceName =  getMetaProperty(property, "name");
					if (propMap.serviceName == null) 
						propMap.serviceName = property.name;
						
					propMap.decodeFunctionName = getMetaProperty(property, "decodeFunction");
					propMap.encodeFunctionName = getMetaProperty(property, "encodeFunction");
					propMap.collectionClassTypeName = getMetaProperty(property, "classType");
					propMap.field = getMetaProperty(property, "field");
					propMap.ignore = getMetaProperty(property, "ignore");
					propMap.isValueObject = new propMap.voClassType() is IVOFactoryObject;
					
					if (propMap.collectionClassTypeName != null)
						propMap.collectionClassType = getDefinitionByName(propMap.collectionClassTypeName) as Class;
					
					newMap.addItem(propMap);
				}
				
				voPropertyMap[classType] = newMap;
			}
			return voPropertyMap[classType];
		}
		
		/**
		 * Get the XML property info for the class specified
		 * 
		 * @param classType Class to get property info for
		 * 
		 * @return XML decoded into an object containing property info
		 */
		private static function getClassInfo(classType:Class):Object
		{
			if (classInfoMap[classType] == null)
			{
				var classInstance:Object = new classType();
				var xmlString:String = describeType(classInstance).toXMLString();
				classInfoMap[classType] = new SimpleXMLDecoder(true).decodeXML(new XMLDocument(xmlString));
			}
			return classInfoMap[classType];
		}
		
		/**
		 * Look for the value on the source object by the propertyName given
		 * traversing down the object hierarchy.
		 * 
		 * @param source Object to get property value from
		 * @param propertyName Name of the property to access (may "." delimited)
		 * 
		 * @return Value from the source object
		 */
		private static function getPropertyValue(source:Object, propertyName:String):Object
		{
			var ret:Object;
			var delimeter:Number = propertyName.indexOf(".");
			
			if (delimeter < 0)
			{
				if (source.hasOwnProperty(propertyName))
					ret = source[propertyName];
			}
			else
			{
				var nextProp:String = propertyName.substring(0, delimeter);
				if (source.hasOwnProperty(nextProp) &&
					source[nextProp] != null)
					ret = getPropertyValue(source[nextProp], propertyName.substring(delimeter + 1));
			}
			
			return ret;
		}
		
		/**
		 * Look through the metadata properties of the given property node and
		 * return the one matching a name.
		 * 
		 * @param property Accessor or variable object from class info
		 * @param name Name of metatdata tag to find
		 * 
		 * @return Value of property object matching the given name
		 */
		private static function getMetaProperty(property:Object, name:String):String
		{
			for each (var meta:Object in getMetaList(property))
			{
				if (meta.hasOwnProperty("name") &&
					meta.name == "Property")
				{
					if (meta.arg is ArrayCollection)
					{
						for each (var arg:Object in meta.arg)
							if (arg.key == name)
								return arg.value;
					}
					else if (meta.arg.key == name)
					{
						return meta.arg.value;
					}
					return null;
				}
			}
			return null;
		}
		
		/**
		 * Get the list of metadata objects from the given property object
		 * 
		 * @param property Acessor or variable object from class info
		 * 
		 * @return List of metadata objects from the property object
		 */
		private static function getMetaList(property:Object):ArrayCollection
		{
			var list:ArrayCollection;
			
			if (property.metadata != null)
			{
				// Allow for multiple or single metadata elements
				if (property.metadata is ArrayCollection)
					list = property.metadata;
				else
				{
					list = new ArrayCollection();
					list.addItem(property.metadata);
				}
			}
			return list;
		}
	}
}