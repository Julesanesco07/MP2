<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login Page</title>
    <link rel="stylesheet" href="SuccessCSS.css">
        <link href="https://fonts.googleapis.com/css2?family=Press+Start+2P&display=swap" rel="stylesheet">

</head>
<body>
        
        <%
            if (session == null) {
                response.sendRedirect("error_session.jsp");
            } 
            
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0); 
        %>
        
    <div class="wrap">
        <div class="header">
            ${initParam.headerContent}
        </div>
        
        <% 
            String username = (String) session.getAttribute("username");
            String role = (String) session.getAttribute("role");

            if (username != null && !username.isEmpty()) {
        %>
            <h1>Success</h1>
            <h2>Welcome, <%= username %></h2>
            <h3>Your Role: <%= role %></h3>
            
             <%if (session != null) {
            session.invalidate();
            System.out.println("Session invalidated successfully.");
        } else {
            System.out.println("No session found to invalidate.");
        }%>
            <form name="backForm" action="BackServlet" method="post">
                <div class="buttons">
                    <button type="submit" class="button">Logout</button>
                </div>
            </form>
        <% 
            } else {
                response.sendRedirect("error_session.jsp");
            } 
        %>
        
        <div class="footer">        
            ${initParam.footerContent}
        </div>
    </div>
</body>
</html>
