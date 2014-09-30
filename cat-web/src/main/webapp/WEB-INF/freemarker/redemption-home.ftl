<#import "spring.ftl" as spring/>
<html>

<head>
    <title>Login Page</title>
</head>

<body>
  <a href="<@spring.url '/home/redeemCoupon' />">Coupon Redemption Page</a><br/>
  <a href="<@spring.url '/home/luckyNumbers' />">Download Lucky Numbers</a><br/>
  <form action="<@spring.url '/home/redeemCoupon' />" method="POST">
  Coupon Code:<input type="text" name="couponCode"/><input type="submit" value="Redeem"/><br/>
  </form>
  <#if message??>
      <b>${message}</b><br/>
  </#if>
  <a href="<@spring.url '/logout' />">Logout</a/>
</body>

</html>