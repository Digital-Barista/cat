<#import "spring.ftl" as spring/>

<html>

<head>
    <title>Login Page</title>
</head>

<body>
  <a href="<@spring.url '/home/redeemCoupon' />">Coupon Redemption Page</a><br/>
  <a href="<@spring.url '/home/luckyNumbers' />" onclick="return false;">Download Lucky Numbers</a><br/>
  <form action="<@spring.url '/home/redeemCoupont' />" method="POST">
  Coupon Code:<input type="text" name="coupon"/><input type="submit" value="Redeem"/>
  </form>
  <#if model['message']!=null>
    <b>${model['message']}</b>
  </#if>
</body>

</html>