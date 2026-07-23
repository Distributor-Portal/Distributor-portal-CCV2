<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>


<a <c:if test="${not empty localURL}"> href="${localURL}"</c:if> <c:if test="${not empty target and target eq 'NEWWINDOW'}"> target="_blank"</c:if>><c:if test="${not empty linkName}"> ${linkName }</c:if></a>