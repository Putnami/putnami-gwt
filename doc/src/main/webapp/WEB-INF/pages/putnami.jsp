<%@ page import="org.springframework.security.web.csrf.CsrfToken" %>
<!DOCTYPE html>
<html lang="en">
  <head>
    <title>Putnami - Framework Web New Generation</title>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="_csrf" content="<%= ((CsrfToken)request.getAttribute("_csrf")).getToken() %>"/>
    <meta name="_csrf_header" content="<%= ((CsrfToken)request.getAttribute("_csrf")).getHeaderName() %>"/>
    
	<script type="text/javascript" src="Documentation/Documentation.nocache.js"></script>
  </head>
  <body>
    <noscript>Your browser does not support JavaScript!</noscript>
  </body>
</html>
