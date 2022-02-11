<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="nav" tagdir="/WEB-INF/tags/desktop/nav" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<sec:authorize access="hasAnyRole('ROLE_CUSTOMERGROUP')">
    <li class="logged_in"><ycommerce:testId code="header_LoggedUser"><spring:theme code="header.welcome" arguments="${user.firstName}, ${user.unit.name};${user.lastName},${user.unit.name}" argumentSeparator=";" htmlEscape="true"/></ycommerce:testId></li>
</sec:authorize>

<!-- Added Code changes for WeSell Implementation - START -->
<c:if test="${not empty salesRepEmployee}">
<sec:authorize access="hasAnyRole('ROLE_EMPLOYEEGROUP')">
  <li class="logged_in"><ycommerce:testId code="header_LoggedUser"><spring:theme code="header.welcome" arguments="${salesRepEmployee.name}, ${user.unit.name}" argumentSeparator=";" htmlEscape="true"/></ycommerce:testId></li>
</sec:authorize>
</c:if>
<!-- Added Code changes for WeSell Implementation - END -->
<c:if test="${navigationNode.visible}">
    <c:forEach items="${navigationNode.links}" var="link">
    	<c:choose>
    	<c:when test="${ isSalesRepUserLogin and link.uid eq 'MyCompanyLink'}">
    	
    	</c:when>
    	<c:otherwise>
    		<cms:component component="${link}" evaluateRestriction="true" element="li" />
    	</c:otherwise>
    	</c:choose>
        
    </c:forEach>
</c:if>