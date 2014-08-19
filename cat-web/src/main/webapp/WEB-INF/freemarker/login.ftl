<#import "spring.ftl" as spring/>

<html>

<head>
    <title>Login Page</title>
    <link rel="stylesheet" href="angular/app/css/bootstrap.css"/>
    <link rel="stylesheet" href="angular/app/css/angular-ui.min.css"/>
    <link rel="stylesheet" href="angular/app/css/app.css"/>
</head>

<body>

<header class="navbar navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container">
            <a class="brand" href=""></a>
            <div class="nav-collapse">
                <ul class="nav">
                </ul>
            </div>
        </div>
    </div>
</header>

<div class="container login modal">
    <form class="form-horizontal" action="<@spring.url "/j_spring_security_check"/>" method="POST">
        <div class="modal-header">
            <h3>CAT</h3>
        </div>
        <div class="control-group">
            <label class="control-label" for="username">Username</label>

            <div class="controls">
                <input type="text" id="username" placeholder="Username" name="j_username">
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="inputPassword">Password</label>

            <div class="controls">
                <input type="password" id="inputPassword" placeholder="Password" type="password" name="j_password">
            </div>
        </div>
        <div class="modal-footer">
                <button type="submit" class="btn btn-primary">Sign in</button>
        </div>
    </form>
</div>

<script>
    var contextPath = '<@spring.url ''/>';
</script>
</body>

</html>