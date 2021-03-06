<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="principal" />
<sec:authentication property="details" var="details" />

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Spring security</title>

  <script src="<c:url value="/resources/bower_components/jquery/dist/jquery.min.js" />"></script>

  <link href="<c:url value="/resources/bower_components/bootstrap/dist/css/bootstrap.min.css" />" rel="stylesheet">
  <!--[if lt IE 9]>
  <script src="<c:url value="/resources/bower_components/html5shiv/dist/html5shiv.min.js" />"></script>
  <script src="<c:url value="/resources/bower_components/respond/dest/respond.min.js" />"></script>
  <![endif]-->
  <link rel="stylesheet" href="<c:url value="/resources/bower_components/font-awesome/css/font-awesome.min.css" />">
</head>

<body>

<div class="container">

	<h2><i class="fa fa-inbox" aria-hidden="true"></i> User Profile</h2>

	<div class="panel panel-default">
		<div class="panel-body">
			<div class="page-header">
				<h1>Session Info</h1>
			</div>
			<div><i class="fa fa-user" aria-hidden="true"></i> User Name: ${principal.username}</div>
			<div><i class="fa fa-key" aria-hidden="true"></i> Session ID: ${details.sessionId}</div>
			<div class="page-header">
				<h1>Member Info</h1>
			</div>
			<div><i class="fa fa-user" aria-hidden="true"></i> User Name: ${member.userId}</div>
			<div><i class="fa fa-key" aria-hidden="true"></i> Session ID: ${member.sessionId}</div>
			<div><i class="fa fa-key" aria-hidden="true"></i> Last Access Date: ${member.lastAccessDate}</div>
			<div><i class="fa fa-key" aria-hidden="true"></i> IP Address: ${member.lastAccessIp}</div>
		</div>
	</div>

	<div class="text-center">
		<a class="btn btn-default" href="<c:url value="/member/visitor" />" role="button">접속현황</a>
		<a class="btn btn-default" href="<c:url value="/member/logout" />" role="button">로그아웃</a>
	</div>

</div>


</body>

</html>