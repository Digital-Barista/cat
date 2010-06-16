function PrintUtil() 
{
}

PrintUtil.showCoupon = function(messageId)
{
	// Pull out message parts
	var line = $("#" + "message_" + messageId);
	var title = $(line).find(".title");
	var message = $(line).find(".messageBody");
	
	// Open window
	var w = window.open();
	w.document.open();

	// Write CSS
	w.document.write('<link href="/cat/facebook/css/coupon.css" type="text/css" rel="stylesheet" />');
	
	// Write content
	w.document.write('<div class="coupon" style="background:url(\'images/coupon_background.jpg\') no-repeat;">' + 
			'<h2>' + title[0].innerHTML + "</h2>" + 
			message[0].innerHTML + '</div>');
	
	// Close stream
	w.document.close();
}