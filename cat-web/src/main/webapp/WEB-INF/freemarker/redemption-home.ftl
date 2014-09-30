<#import "spring.ftl" as spring/>
<html>

<head>
    <title>Coupon Redemption</title>
    <link rel="stylesheet" href="<@spring.url '/angular/app/css/bootstrap.css' />"/>
    <link rel="stylesheet" href="<@spring.url '/angular/app/css/angular-ui.min.css' />"/>
    <link rel="stylesheet" href="<@spring.url '/angular/app/css/app.css' />"/>
</head>

<body>
  <a id="link-coupon-redemption" href="<@spring.url '/home/redeemCoupon' />">Coupon Redemption Page</a><br/>
  <a id="link-lucky-number-download" href="<@spring.url '/home/luckyNumbers' />">Download Lucky Numbers</a><br/>
  <form action="<@spring.url '/home/redeemCoupon' />" method="POST">
  Coupon Code:<input id="input-coupon-code" type="text" name="couponCode"/><input id="button-redeem" type="submit" value="Redeem"/><br/>
  </form>
  <#if message??>
      <span id="text-coupon-message">${message}</span><br/>
  </#if>
  <a id="link-logout" href="<@spring.url '/logout' />">Logout</a/>
</body>

</html>