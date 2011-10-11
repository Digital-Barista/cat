<!DOCTYPE html>

<%
  String appName = request.getParameter("app_id");
  String appUrl = "http://apps.facebook.com/" + appName + "/";
%>

<html>
  <head>
    <link href="css/fb.css" type="text/css" rel="stylesheet" />
    <style>
      body{
        text-align:center;
       }
    </style>
  </head>
  <body>
    <h1>You have successfully invited your friends</h1>
    <a href="<%=appUrl%>" class="button" id="Inbox" target="_top">Back to Inbox</a>
  </body>
</html>