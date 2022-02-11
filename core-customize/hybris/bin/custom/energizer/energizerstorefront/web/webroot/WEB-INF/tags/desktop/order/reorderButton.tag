<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="order" required="true" type="de.hybris.platform.commercefacades.order.data.AbstractOrderData" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:url value="/checkout/single/summary/reorder" var="reorderUrl" />

<form:form action="${reorderUrl}" id="reorderForm" modelAttribute="reorderForm">
	<form:input type="hidden" name="orderCode" path="orderCode" value="${order.code}" />
	<c:choose>
	<c:when test="${orderData.isOrderBlock == true}">
	<button type="submit" class="positive right pad_right re-order" id="reorderButton" disabled="disabled">
		<spring:theme code="checkout.blocked.order" text="Reorder"/>
	</button>
	</c:when>
	<c:when test="${displayed == false}">
	<button type="submit" class="positive right pad_right re-order" id="reorderButton" disabled="disabled">
		<spring:theme code="text.order.reorderbutton" text="Reorder"/>
	</button>
	</c:when>
	<c:otherwise>
	<button type="submit" class="positive right pad_right re-order" id="reorderButton">
		<spring:theme code="text.order.reorderbutton" text="Reorder"/>
	</button>
	</c:otherwise>
	</c:choose>
</form:form>
