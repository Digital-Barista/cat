<#import "spring.ftl" as spring/>

<#macro button text id="" href="" class="">
	<#local buttonClass = "" />
	<#if class?has_content>
		<#local buttonClass = class />
	</#if>
	
	<a <#if id?has_content>id="${id}"</#if> <#if href?has_content>href="${href}"</#if> class="button ${buttonClass}">
		<span class="button-content">
			<span class="button-text">${text}</span>
		</span>
	</a>
</#macro>

<html>
<head>
  <title>Login Page</title>
  <link type="text/css" href="<@spring.url '/new-client/css/style.css'/>" rel="stylesheet" />
  <script src="<@spring.url '/new-client/js/libs/jquery.js' />"></script>
  <script>
    $(document).ready(function(){
      var form = $('form');
      
      $('#login-button').click(function(){
          form.submit();
      });
      
      $('input').keypress(function(e){
        if(e.which == 13){
         form.submit();
        }
      });
      
      $('#username').focus();
     });
  </script>
</head>

<body> 

<div id="login">
  <div class="login-header">
    Sign In
  </div>
  
  <form id="login-form" name="login-form" action="<@spring.url "/j_spring_security_check"/>" method="POST"> 
    <div class="username">
      <div class="label">Username:</div><input type="text" id="username" name="j_username" >
    </div>
    <div class="password">
      <div class="label">Password:</div><input type="password" id="password" name="j_password"/>
    </div>
    <div class="buttons">
    	<@button id="login-button" href="javascript:void(0)" text="Login" />
    </div>
  </form>
  <div style="clear:both;"></div>
</div>

</body>

</html>