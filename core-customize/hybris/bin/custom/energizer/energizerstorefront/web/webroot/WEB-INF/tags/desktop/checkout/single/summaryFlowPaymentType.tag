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

<div class="summaryPaymentType summarySection ${summarySectionClass}"  		
	data-set-payment-type-url="${setPaymentTypeURL}" 
	data-set-purchase-order-number-url="${setPurchaseOrderNumberURL}" >
	
	
	<ycommerce:testId code="paymentType_text">
		<div class="contentSection">
			<div class="content">
				<div class="headline"><spring:theme code="text.account.orderHistory.poNumber" htmlEscape="false"/><span class="mandatory"><img width="5" height="6" alt="Required" title="Required" src="/_ui/desktop/common/images/mandatory.gif"></span></div>
				<div>
		           <%--  <c:forEach items="${paymentTypes}" var="paymentType">
		                <form:radiobutton path="paymentTypes" id="PaymentTypeSelection_${paymentType.code}" name="PaymentType" value="${paymentType.code}" label="${paymentType.displayName}"/><br>
		            </c:forEach> --%>
		            <br>
					
					<label><spring:theme code="checkout.summary.purchaseOrderNumber"/></label>
					<br>
					
					<c:choose>
						<c:when test="${siteUid eq 'personalCare'}">
							<input type="text" id="PurchaseOrderNumber"  name="PurchaseOrderNumber" maxlength="20" style="width: 268px !important;"/> 
						</c:when>
						<c:otherwise>
							<input type="text" id="PurchaseOrderNumber"  name="PurchaseOrderNumber" maxlength="19" />
						</c:otherwise>
					</c:choose>
					
				</div>
			</div>
		</div>
	</ycommerce:testId>
</div>