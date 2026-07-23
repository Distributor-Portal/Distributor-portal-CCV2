<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="rating" required="true" %>
<%@ attribute name="addClass" required="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>




<c:if test="${not empty rating}">
<c:set var="starWidth" value="16" />
<c:set var="starCount" value="5" />

<span class="stars ${addClass}" style="width: <fmt:formatNumber maxFractionDigits="0" value="${rating * starWidth}" />px; margin-right: <fmt:formatNumber maxFractionDigits="0" value="${(starCount - rating) * starWidth}" />px;">${rating}</span>


</c:if>