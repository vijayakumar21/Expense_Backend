<%--
  Created by IntelliJ IDEA.
  User: vijay-14224
  Date: 06/11/26
  Time: 2:32 pm
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <title>ADDITION</title>
  </head>
  <body bgColor = "cyan">
        <%
        int num1 = Integer.parseInt(request.getParameter("num1"));
        int num2 = Integer.parseInt(request.getParameter("num2"));
        int result = num1+num2;
        out.println("Value of result using JSP = " + result);
        %>
  </body>
</html>
