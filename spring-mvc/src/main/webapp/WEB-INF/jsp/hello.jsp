<%--
  Created by IntelliJ IDEA.
  User: darcy
  Date: 2019-06-25
  Time: 13:34
  To change this template use File | Settings | File Templates.
--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>hello</title>
</head>
<body>
<h1>你好，${name} </h1>
<h2><a href="<c:url value="/index"/>">返回主页</a></h2>
</body>
</html>
