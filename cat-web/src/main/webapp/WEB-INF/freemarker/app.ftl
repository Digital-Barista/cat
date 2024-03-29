<#import "spring.ftl" as spring/>

<!doctype html>
<html lang="en" ng-app="cat">
<head>
    <meta charset="utf-8">
    <title>CAT Client</title>
    <link rel="stylesheet" href="css/bootstrap.css"/>
    <link rel="stylesheet" href="css/angular-ui.min.css"/>
    <link rel="stylesheet" href="css/app.css"/>
</head>
<body>

<div sessiontimeout></div>
<div dataloader></div>
<div modalmessage></div>
<div modalloader></div>

<header class="navbar navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container">
            <a class="brand" href="">CAT</a>
            <div class="nav-collapse">
                <ul class="nav">
                    <li class="active">
                        <a scrollto="" href="#">Home</a>
                    </li>
                    <li>
                        <a scrollto="" href="#">Profile</a>
                    </li>
                </ul>
                <ul class="pull-right nav">
                    <li class="dropdown">
                        <a href="#" data-toggle="dropdown" class="dropdown-toggle">
                            Help <b class="caret"></b>
                        </a>
                        <ul class="dropdown-menu">
                            <li><a href="mailto:support@digitalbarista.com">Contact Us</a></li>
                            <li class="dropdown-submenu">
                                <a href="#">Tutorials</a>
                                <ul class="dropdown-menu">
                                    <li><a href="http://www.vimeo.com/29322671" target="_blank">1 - The Dashboard - SMP Overview</a></li>
                                    <li><a href="http://www.vimeo.com/29322729" target="_blank">2 - Messaging Overview</a></li>
                                    <li><a href="http://www.vimeo.com/29322790" target="_blank">3 - The Basic Message</a></li>
                                    <li><a href="http://www.vimeo.com/29322865" target="_blank">4 - The Coupon Message</a></li>
                                    <li><a href="http://www.vimeo.com/29322963" target="_blank">5 - The Welcome Message</a></li>
                                    <li><a href="http://www.vimeo.com/29323039" target="_blank">6 - Authorizing Your Twitter Account</a></li>
                                </ul>
                            </li>
                        </ul>
                    </li>
                    <li><a href="<@spring.url '/logout'/>">Logout</a></li>
                </ul>
            </div>
        </div>
    </div>
</header>

<div class="container main-container">
    <div class="row">
        <div leftmenu class=""></div>
        <div ng-view class="main-view"></div>
    </div>
</div>


<!-- In production use:
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min.js"></script>
-->
<script>
    var contextPath = '<@spring.url ''/>';
</script>

<script src="lib/jquery/jquery-1.8.3.min.js"></script>
<script src="lib/jquery/jquery-ui-min.js"></script>
<script src="lib/tiny_mce/jquery.tinymce.js"></script>
<script src="lib/tiny_mce/tiny_mce.js"></script>
<script src="lib/bootstrap/bootstrap.js"></script>
<script src="lib/angular/angular.js"></script>
<script src="lib/angularui/angular-ui.js"></script>
<script src="js/app.js"></script>
<script src="js/services.js"></script>
<script src="js/controllers.js"></script>
<script src="js/filters.js"></script>
<script src="js/directives.js"></script>
<script type="text/javascript" src="https://www.google.com/jsapi"></script>


</body>
</html>
