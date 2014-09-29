<#import "spring.ftl" as spring/>

<html>

<head>
    <title>Login Page</title>
</head>

<body>
  <a href="/home/redemCoupon">Coupon Redemption Page</a><br/>
  <a href="/home/luckyNumbers" onclick="return false;">Download Lucky Numbers</a><br/>
  <form action="/home/redeemCoupont" method="POST">
  Coupon Code:<input type="text" name="coupon"/><input type="submit" value="Redeem"/>
  </form>
  <#if model['message']!=null>
    <b>${model['message']}</b>
  </#if>
</body>

</html>