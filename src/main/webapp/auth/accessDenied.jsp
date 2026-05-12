<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Access Denied - LiteFlow</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f8d7da;
            color: #721c24;
            display: flex;
            align-items: center;
            justify-content: center;
            height: 100vh;
        }
        .error-box {
            background: #fff;
            border: 1px solid #f5c6cb;
            border-radius: 8px;
            padding: 30px 40px;
            text-align: center;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        h1 { margin-bottom: 10px; }
        a {
            color: #155724;
            text-decoration: none;
            font-weight: bold;
        }
    </style>
</head>
<body>
<div class="error-box">
    <h1>🚫 Access Denied</h1>
    <p>Bạn không có quyền truy cập vào trang này.</p>
    <p><a href="${pageContext.request.contextPath}/dashboard.jsp">Quay về Dashboard</a></p>
</div>
</body>
</html>
