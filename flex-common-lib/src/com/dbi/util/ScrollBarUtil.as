package com.dbi.util
{
    import mx.controls.scrollClasses.ScrollBar;
    import mx.controls.scrollClasses.ScrollThumb;
    import mx.core.Container;
    import mx.core.mx_internal;
    import mx.styles.StyleProxy;
            
    use namespace mx_internal;
    
    public class ScrollBarUtil
    {
    	public static function replaceContainerThumbs(container:Container):void
    	{
    		var vert:ScrollBar = container.rawChildren.getChildByName('verticalScrollBar') as ScrollBar;
    		var hor:ScrollBar = container.rawChildren.getChildByName('verticalScrollBar') as ScrollBar;
    		
    		if (vert != null)
    			replaceScrollThumb(vert);
    		if (hor != null)
    			replaceScrollThumb(hor);
    	}
    	
        public static function replaceScrollThumb(scrollBar : ScrollBar) : ScrollThumb
        {
            if (scrollBar != null &&
            	scrollBar.scrollThumb != null)
            {
                if (!(scrollBar.scrollThumb is FixedSizeScrollThumb))
                {
                    scrollBar.removeChild(scrollBar.scrollThumb);
                    
                    var fixedSizeScrollThumb : FixedSizeScrollThumb = new FixedSizeScrollThumb();
                    fixedSizeScrollThumb.x = scrollBar.scrollThumb.x;
                    fixedSizeScrollThumb.y = scrollBar.scrollThumb.y;
                    
                    fixedSizeScrollThumb.styleName = new StyleProxy(scrollBar, null);
                    fixedSizeScrollThumb.upSkinName = "thumbUpSkin";
                    fixedSizeScrollThumb.overSkinName = "thumbOverSkin";
                    fixedSizeScrollThumb.downSkinName = "thumbDownSkin";
                    fixedSizeScrollThumb.iconName = "thumbIcon";
                    fixedSizeScrollThumb.skinName = "thumbSkin";
                    
                    scrollBar.scrollThumb = fixedSizeScrollThumb;
                    scrollBar.addChildAt(fixedSizeScrollThumb, scrollBar.getChildIndex(scrollBar.downArrow));
                    
                    return fixedSizeScrollThumb;
                }
                else
                {
                    return scrollBar.scrollThumb;
                }
            }
            else
            {
                return null;
            }
        }

    }
}