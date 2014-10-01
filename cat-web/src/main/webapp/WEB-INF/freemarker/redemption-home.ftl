<#import "spring.ftl" as spring/>
<html>

<head>
    <title>Coupon Redemption</title>
    <link rel="stylesheet" href="<@spring.url '/angular/app/css/bootstrap.css' />"/>
    <link rel="stylesheet" href="<@spring.url '/angular/app/css/angular-ui.min.css' />"/>
    <link rel="stylesheet" href="<@spring.url '/angular/app/css/app.css' />"/>
</head>

<body>
<div class="container modal">
  <#--<a id="link-coupon-redemption" href="<@spring.url '/home/redeemCoupon' />">--> <div class="modal-header"><h3>Lucky Number Redemption Page</h3></div><#--</a>-->
  <form class="navbar-form" action="<@spring.url '/home/redeemCoupon' />" method="POST">
  <div style="margin:15px;">
  <input id="input-coupon-code" placeholder="Enter Lucky Number Code" type="text" name="couponCode" style="height:100%;"/>
  <input class="btn btn-default" id="button-redeem" type="submit" value="Redeem"/>
</div>
  </form>
  
  <#if message??>
      <div class="well" style="margin:0px 15px 15px 15px;" id="text-coupon-message">${message}</div>
  </#if>
  <div class="modal-footer">
    <div style="float:left"><a class="btn btn-default" id="link-lucky-number-download" href="<@spring.url '/home/luckyNumbers' />">Download Lucky Numbers</a></div>
	<a class="btn btn-default" id="link-logout" href="<@spring.url '/logout' />">Logout</a/>
	</div>
</div>
</body>

</html>