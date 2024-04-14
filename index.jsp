<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.io.*, javax.servlet.*, javax.servlet.http.*"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login Page</title>
    <link rel="stylesheet" href="IndexCSS.css">
    <link href="https://fonts.googleapis.com/css2?family=Press+Start+2P&display=swap" rel="stylesheet">
    <script>
        function disablePaste() {
            document.getElementById('captchaInput').addEventListener('paste', function(event) {
                event.preventDefault();
                alert('Copying and pasting is not allowed for captcha!');
            });
        }
        window.onload = function() {
            disablePaste();
        };
    </script>
</head>
<body>
    <div class="header">
        ${initParam.headerContent}
    </div>
    <div class="content">
        <div class="container">
            <div class="wrap">
                <form name="loginForm" action="LoginServlet" method="post">
                    <img src="https://images-wixmp-ed30a86b8c4ca887773594c2.wixmp.com/f/775dfae9-f9b5-46c9-bcd8-62e7d40ba177/dg8li6u-34e71b80-762d-43fc-8b6f-188857d3f28a.gif?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1cm46YXBwOjdlMGQxODg5ODIyNjQzNzNhNWYwZDQxNWVhMGQyNmUwIiwiaXNzIjoidXJuOmFwcDo3ZTBkMTg4OTgyMjY0MzczYTVmMGQ0MTVlYTBkMjZlMCIsIm9iaiI6W1t7InBhdGgiOiJcL2ZcLzc3NWRmYWU5LWY5YjUtNDZjOS1iY2Q4LTYyZTdkNDBiYTE3N1wvZGc4bGk2dS0zNGU3MWI4MC03NjJkLTQzZmMtOGI2Zi0xODg4NTdkM2YyOGEuZ2lmIn1dXSwiYXVkIjpbInVybjpzZXJ2aWNlOmZpbGUuZG93bmxvYWQiXX0.M5pIxZn0ag9CA4_YBKQiehdzV_EOlBRVcMUW7O_KdGc" alt="Cat Logo" class="logo" width="50" height="50">
                    <h1>Starry Nights</h1>
                    <div class="input-box">
                        <input type="text" name="username" placeholder="Username">
                    </div>
                    <div class="input-box">
                        <input type="password" name="password" placeholder="Password">
                    </div>
                    <div class="captcha">
                        <h3><jsp:include page="CaptchaServlet"></jsp:include></h3>
                    </div>
                    <div class="input-box">
                        <input type="text" name="captchaInput" id="captchaInput" placeholder="Enter Captcha">
                    </div>
                    <button type="submit" class="button">Log In</button>
                </form>
            </div>
        </div>
    </div>
    <div class="footer">        
        ${initParam.footerContent}
    </div>
</body>
</html>
