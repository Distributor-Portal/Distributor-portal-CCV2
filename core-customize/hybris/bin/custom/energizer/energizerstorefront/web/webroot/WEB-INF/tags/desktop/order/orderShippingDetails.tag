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


<div class="orderBox address obShippinFixCls">
	<div class="headline"><spring:theme code="text.account.orderHistory.shippingDetaiils" text="Shipping Details"/></div>
		
		<!-- Hiding Container & Vessel Number only for EMEA, display it for LATAM -->
		<c:choose>
			<c:when test="${siteUid eq 'personalCareEMEA'}">
			</c:when>
			<c:otherwise>
			<c:if test="${not orderData.placedBySalesRep}">
				<p class="reduce_space_cls"><spring:theme code="text.account.orderHistory.containerNumber"/>&nbsp;:&nbsp;${order.containerId}</p>
				<p class="reduce_space_cls"><spring:theme code="text.account.orderHistory.carrierName"/>&nbsp;:&nbsp;${order.vesselNumber}</p>
			</c:if>
			</c:otherwise>
		</c:choose>
		
		<c:choose>
			<c:when test="${siteUid eq 'personalCare'}">
				<p class="reduce_space_cls"><spring:theme code="product.product.details.future.date" />&nbsp;:
					<c:if test="${not empty order.requestedDeliveryDate }">
						 ${DateTimeUtil.displayDate(order.requestedDeliveryDate)} 
					</c:if>
				</p>
			</c:when>
			<c:otherwise>
				<p class="reduce_space_cls"><spring:theme code="product.product.details.future.date" />&nbsp;:
					<c:if test="${not empty order.requestedDeliveryDate }">
						 ${DateTimeUtil.displayRequestedDeliveryDate(order.requestedDeliveryDate,'dd/MM/yyyy')} 
					</c:if>
				</p>
			</c:otherwise>
		</c:choose>
</div>
