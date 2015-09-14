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
	<script type="text/javascript">
	window.$zopim||(function(d,s){var z=$zopim=function(c){z._.push(c)},$=z.s=
	d.createElement(s),e=d.getElementsByTagName(s)[0];z.set=function(o){z.set.
	_.push(o)};z._=[];z.set._=[];$.async=!0;$.setAttribute("charset","utf-8");
	$.src="//v2.zopim.com/?2znFOx4yfdOY5vlYuUyG2DhHFZe20yy2";z.t=+new Date;$.
	type="text/javascript";e.parentNode.insertBefore($,e)})(document,"script");
	</script>
  </head>
  <body>
    <noscript>Your browser does not support JavaScript!</noscript>
  </body>
</html>
