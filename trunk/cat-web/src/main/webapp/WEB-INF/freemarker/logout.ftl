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
  <link type="text/css" href="<@spring.url '/css/style.css'/>" rel="stylesheet" />
</head>

<body> 

<div id="logout">
  <h1>You have successfully logged out</h1>
  <@button text="Login" href="${springMacroRequestContext.getContextPath()}/login" />
</div>

</body>

</html>