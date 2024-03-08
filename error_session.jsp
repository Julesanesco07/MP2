<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" href="ErrorCSS.css">
        <title>Error Page</title>
    </head>
    <body>
        <div class="header">
            ${initParam.headerContent}
        </div>
        <h3>Session not found.</h3>
        <form name = "backForm" action = "BackServlet" method = "post">
            <div class = "buttons">
                <button type = "submit" class = "button">Back</button>
            </div>
        </form>
        <div class="footer">        
            ${initParam.footerContent}
        </div>
    </body>
</html>
