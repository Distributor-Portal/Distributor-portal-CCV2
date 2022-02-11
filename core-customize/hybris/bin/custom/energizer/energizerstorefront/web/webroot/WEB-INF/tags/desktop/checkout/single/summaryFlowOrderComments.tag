<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>

<spring:url value="/_ui/desktop/common/images/spinner.gif" var="spinnerUrl" />
<spring:url value="/checkout/single/summary/setPaymentType.json" var="setPaymentTypeURL" />
<spring:url value="/checkout/single/summary/getCheckoutCart.json" var="getCheckoutCartUrl" />
<spring:url value="/checkout/single/summary/setPurchaseOrderNumber.json" var="setPurchaseOrderNumberURL" />


<c:choose>
	<c:when test="${siteUid eq 'personalCare'}">
		<c:set var="summaryDeliveryAddressClass" value="summaryDeliveryAddressLATAM" />
		<c:set var="summarySectionClass" value="summarySectionLATAM" />
		<c:set var="orderCommentsClass" value="orderCommentsLATAM" />
	</c:when>
	<c:otherwise>
		<c:set var="summaryDeliveryAddressClass" value="" />
		<c:set var="summarySectionClass" value="" />
		<c:set var="orderCommentsClass" value="" />
	</c:otherwise>
</c:choose>

<div class="summaryPaymentType summarySection ${summarySectionClass}">
	
	<%-- <ycommerce:testId code="paymentType_text"> --%>
		<div class="contentSection">
			<div class="content">
				<div class="headline"><spring:theme code="text.account.orderHistory.OrderComments" htmlEscape="false"/></div>
				<div>
					<textarea id="orderComments" class="orderComments ${orderCommentsClass}" name="orderComments" maxlength="264"></textarea>
				</div>
			</div>
		</div>
	<%-- </ycommerce:testId> --%>
</div>