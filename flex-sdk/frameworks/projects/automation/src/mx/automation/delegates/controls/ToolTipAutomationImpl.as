////////////////////////////////////////////////////////////////////////////////
//
//  ADOBE SYSTEMS INCORPORATED
//  Copyright 2006-2007 Adobe Systems Incorporated
//  All Rights Reserved.
//
//  NOTICE: Adobe permits you to use, modify, and distribute this file
//  in accordance with the terms of the license agreement accompanying it.
//
////////////////////////////////////////////////////////////////////////////////

package mx.automation.delegates.controls 
{
import flash.display.DisplayObject;
    
import mx.automation.Automation;
import mx.automation.delegates.core.UIComponentAutomationImpl;
import mx.core.mx_internal;
import mx.controls.ToolTip;

use namespace mx_internal;

[Mixin]
/**
 * 
 *  Defines methods and properties required to perform instrumentation for the 
 *  ToolTip control.
 * 
 *  @see mx.controls.ToolTip 
 *
 */
public class ToolTipAutomationImpl extends UIComponentAutomationImpl 
{
    include "../../../core/Version.as";
    
    //--------------------------------------------------------------------------
    //
    //  Class methods
    //
    //--------------------------------------------------------------------------

    /**
     *  Registers the delegate class for a component class with automation manager.
     *  
     *  @param root The SystemManger of the application.
     */
    public static function init(root:DisplayObject):void
    {
        Automation.registerDelegateClass(ToolTip, ToolTipAutomationImpl);
    }   

    //--------------------------------------------------------------------------
    //
    //  Constructor
    //
    //--------------------------------------------------------------------------

    /**
     *  Constructor.
     * @param obj ToolTip object to be automated.     
     */
    public function ToolTipAutomationImpl(obj:ToolTip)
    {
        super(obj);
    }

    /**
     *  @private
     *  storage for the owner component
     */
    protected function get toolTip():ToolTip
    {
        return uiComponent as ToolTip;
    }
    
    //----------------------------------
    //  automationValue
    //----------------------------------

    /**
     *  @private
     */
    override public function get automationValue():Array
    {
        return [ toolTip.getTextField().text ];
    }
        
}
}