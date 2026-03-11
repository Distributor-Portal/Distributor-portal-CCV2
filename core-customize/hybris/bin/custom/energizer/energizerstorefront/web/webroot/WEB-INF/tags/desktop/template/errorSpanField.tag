<%@ tag body-content="scriptless" trimDirectiveWhitespaces="true" %>
<%@ attribute name="path" required="true" rtexprvalue="true"%>
<%@ attribute name="errorPath" required="false" rtexprvalue="true"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<spring:bind path="${not empty errorPath ? errorPath : path}">
<c:choose>
	<c:when test="${not empty status.errorMessages}">
		<div class="control-group error">
			<jsp:doBody/>
			<div class="help-inline">
				<!--Added to display the error message in red color 			 -->
			<font color="red">
				<form:errors path="${not empty errorPath ? '' : path}" htmlEscape="false"/>
			</font>
			</div>
		</div>
	</c:when>
	<c:otherwise>
		<div class="control-group">
			<jsp:doBody/>
		</div>
	</c:otherwise>
</c:choose>
</spring:bind> 