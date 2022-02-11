<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring"  uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- Information (confirmation) messages --%>
<c:if test="${not empty accConfMsgs}">
		<c:forEach items="${accConfMsgs}" var="msg">
			<div class="alert positive">
				<spring:theme code="${fn:escapeXml(msg.code)}" arguments="${fn:escapeXml(msg.attributes)}"/>
			</div>
		</c:forEach>
</c:if>
<c:if test="${not empty forgotPwdConfMsgs}">
		<c:forEach items="${forgotPwdConfMsgs}" var="msg">
			<div id="forgotPwdConfMsgs" class="alert positive">
				<spring:theme code="${fn:escapeXml(msg.code)}" arguments="${fn:escapeXml(msg.attributes[0])}"/>
			</div>
		</c:forEach>
</c:if>
<c:if test="${not empty b2bUnitMaxUserConfMsgs}">
		<c:forEach items="${b2bUnitMaxUserConfMsgs}" var="msg">
			<div id="b2bUnitMaxUserConfMsgs" class="alert positive">
				<spring:theme code="${fn:escapeXml(msg.code)}" arguments="${fn:escapeXml(msg.attributes[0])}"/>
			</div>
		</c:forEach>
</c:if>
<%-- Warning messages --%>
<c:if test="${not empty accInfoMsgs}">
		<c:forEach items="${accInfoMsgs}" var="msg">
			<div class="alert neutral">
				<spring:theme code="${fn:escapeXml(msg.code)}" arguments="${fn:escapeXml(msg.attributes)}"/>
			</div>
		</c:forEach>
</c:if>

<%-- Error messages (includes spring validation messages)--%>
<c:if test="${not empty accErrorMsgs}">
		<c:forEach items="${accErrorMsgs}" var="msg">
			<div class="alert negative">
				<spring:theme code="${fn:escapeXml(msg.code)}" arguments="${fn:escapeXml(msg.attributes)}"/>
			</div>
		</c:forEach>
</c:if>

<%-- Error messages (includes spring validation messages)--%>
<c:if test="${not empty businessRuleError}">
		<c:forEach items="${businessRuleError}" var="msg">
			<div class="alert negative">
				<spring:theme code="${fn:escapeXml(msg.code)}" arguments="${fn:escapeXml(msg.attributes)}"/>
			</div>
		</c:forEach>
</c:if>