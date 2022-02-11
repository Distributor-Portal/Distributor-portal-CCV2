<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="order" required="true" type="de.hybris.platform.commercefacades.order.data.AbstractOrderData" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/desktop/product" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:useBean id="DateTimeUtil"  class="com.energizer.storefront.util.EnergizerDateTimeUtil" />

<div class="orderBox estimatedDeliveryDate"> 
	<div class="headline">
		<spring:theme code="text.estimatedDeliveryDate" text="Delivery Date"/>
		<span class="mandatory">
			<img width="5" height="6" alt="Required" title="Required" style="vertical-align: text-top !important;" src="/_ui/desktop/common/images/mandatory.gif">
		</span>
	</div>
	
	<div>
		<spring:theme code="product.product.details.future.date"/>
	</div>
	
	<c:set var="deliveryDate" value="${order.requestedDeliveryDate}" />
	
	<div style="margin-top: 5px;">
		<c:choose>
			<c:when test="${siteUid eq 'personalCare'}">
				<c:if test="${not empty deliveryDate}">
					<%-- <c:set var="deliveryDate" value="${DateTimeUtil.displayDate(deliveryDate)}" /> --%>
					<c:set var="deliveryDate" value="${DateTimeUtil.displayRequestedDeliveryDate(deliveryDate,'MM-dd-yyyy')}" />
				</c:if>
			</c:when>
			<c:otherwise>
				<c:if test="${not empty deliveryDate}">
					<c:set var="deliveryDate" value="${DateTimeUtil.displayRequestedDeliveryDate(deliveryDate,'dd-MM-yyyy')}" />
				</c:if>
			</c:otherwise>
		</c:choose>
		
		<input type="text" name="requestedDeliveryDate" id="checkoutConfirmation_requestedDeliveryDate" readonly="readonly" value="${deliveryDate}"/> 
		
	</div>	
	
	<br/>
	
	<c:if test="${empty deliveryDate}">
		<div style="color: red;"><spring:theme code="text.estimatedDeliveryDate.pending.approval"/></div>
	</c:if>
	
</div>
