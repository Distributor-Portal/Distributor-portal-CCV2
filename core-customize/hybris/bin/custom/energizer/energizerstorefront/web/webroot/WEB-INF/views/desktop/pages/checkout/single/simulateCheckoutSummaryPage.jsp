<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/desktop/template"%>
<%@ taglib prefix="cart" tagdir="/WEB-INF/tags/desktop/cart"%>
<%@ taglib prefix="user" tagdir="/WEB-INF/tags/desktop/user"%>
<%@ taglib prefix="formElement"
	tagdir="/WEB-INF/tags/desktop/formElement"%>
<%@ taglib prefix="checkout" tagdir="/WEB-INF/tags/desktop/checkout"%>
<%@ taglib prefix="single-checkout"
	tagdir="/WEB-INF/tags/desktop/checkout/single"%>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/desktop/common"%>
<%@ taglib prefix="breadcrumb"
	tagdir="/WEB-INF/tags/desktop/nav/breadcrumb"%>
 <jsp:useBean id="DateTimeUtil"  class="com.energizer.storefront.util.EnergizerDateTimeUtil" />  

<spring:url value="/checkout/single/summary/getDeliveryModes.json" var="getDeliveryModesUrl"/>
<spring:url value="/checkout/single/summary/setDeliveryMode.json" var="setDeliveryModeUrl"/>


 <link href="${commonResourcePath}/css/cal.css" rel="stylesheet">
 <script src="${commonResourcePath}/js/jquery-1.7.2.min.js"></script>
 <script src="${commonResourcePath}/js/jquery-ui.js"></script>
 
<spring:url value="/checkout/single/placeOrder" var="placeOrderUrl" />
<spring:url value="/checkout/single/termsAndConditions"
	var="getTermsAndConditionsUrl" />

<template:page pageTitle="${pageTitle}">

	<div id="breadcrumb" class="breadcrumb"></div>

	<div id="globalMessages">
		<common:globalMessages />
	</div>

	<!-- Added for Delivery Notes - LATAM -->
	<c:if test="${siteUid eq 'personalCare' and not isSalesRepUserLogin}">
		<div class="alert negative" id="deliveryNotesFileEmpty" style="display: none;">
			<span id="deliveryNotesFileEmptyText" style="display: none;">
				<spring:theme code="cart.delivery.notes.file.upload.empty"/>
			</span> 
		</div>
		<div class="alert negative" id="deliveryNotesFileRegexError" style="display: none;">
			<span id="deliveryNotesFileRegexErrorText" style="display: none;">
				<spring:theme code="cart.delivery.notes.file.regex.error"/>
			</span> 
		</div>
		<div class="alert negative" id="deliveryNotesFileLengthError" style="display: none;">
			<span id="deliveryNotesFileLengthErrorText" style="display: none;">
				<spring:theme code="cart.delivery.notes.file.length.error"/>
			</span> 
		</div>
		<div class="alert positive" id="deliveryNotesFileUploadSuccess" style="display: none;">
			<span id="deliveryNotesFileUploadSuccessText" style="display: none;">
				<spring:theme code="cart.delivery.notes.file.upload.success"/>
			</span>
		</div>
		<div class="alert negative" id="deliveryNotesFileUploadFailure" style="display: none;">
			<span id="deliveryNotesFileUploadFailureText" style="display: none;">
				<spring:theme code="cart.delivery.notes.file.upload.failure"/>
			</span>
		</div>
		<div class="alert negative" id="deliveryNotesFileRemovalFailure" style="display: none;">
			<span id="deliveryNotesFileRemovalFailureText_en" style="display: none;"></span> 
			<span id="deliveryNotesFileRemovalFailureText_es" style="display: none;"></span>
		</div>
	</c:if>
	<!-- Added for Delivery Notes - LATAM -->
	
	<single-checkout:simulateSummaryFlow />
	
	<div class="orderBoxes clearfix"> 
		<!--  Upload & Display Delivery Note Files ONLY for LATAM - START -->
		<c:if test="${siteUid eq 'personalCare' and not isSalesRepUserLogin}">	
			<cart:deliveryNoteCartItem cartData="${cartData1}"/> 
		</c:if>
		<!--  Upload & Display Delivery Note Files ONLY for LATAM - END -->
	</div>

	<div id="placeOrder" class="clearfix">
		<form:form action="${placeOrderUrl}" id="placeOrderForm1" modelAttribute="placeOrderForm">
		<c:if test="${isSalesRepUserLogin eq 'false'}">
			<formElement:formCheckbox idKey="Terms1PlaceOrder" labelKey="checkout.summary.placeOrder.readTermsAndConditions" inputCSS="checkbox-input" labelCSS="checkbox-label" path="termsCheck" mandatory="true" />
			</c:if>
			<!-- Added for Estimated Delivery Date by Soma - START -->
			<c:if test="${siteUid eq 'personalCare'}">
				<c:set var="cartData" value="${cartData1}" />
				<input type="hidden" value="${cartData.leadTime}" id="leadTime" />
				<div class="estimatedDeliveryDate" style="float: right !important;"> 
					<span style="float: left;padding-right: 40px; padding-top: 4px;">
					<c:if test="${not empty cartData1.requestedDeliveryDate}" >
						<input type="hidden" value="${DateTimeUtil.getTimeInMilliSeconds(cartData1.requestedDeliveryDate)}" id="requestedDeliveryDay" />
						<input type="hidden" value="${DateTimeUtil.getTimeInMilliSeconds(cartData1.requestedDeliveryDate)}" id="initialRequestedDeliveryDate" />
					</c:if>
					<c:if test="${not empty cartData1.leadTime}" >
						<spring:theme code="basket.your.shopping.estimatedleadtime"/> <br/> ${cartData1.leadTime} Days <br/>
					</c:if>
						
						<spring:theme code="product.product.details.future.date"/> <br/>
						<%-- <div id="deliveryDateId">
							<c:choose>
								<c:when test="${siteUid eq 'personalCare'}">
									<c:if test="${not empty cartData.requestedDeliveryDate }">
										${DateTimeUtil.displayDate(cartData1.requestedDeliveryDate)}
									</c:if>
								</c:when>
								<c:otherwise>
									<c:if test="${not empty cartData.requestedDeliveryDate }">
										${DateTimeUtil.displayRequestedDeliveryDate(cartData1.requestedDeliveryDate,'dd/MM/yyyy')}
									</c:if>
								</c:otherwise>
							</c:choose>
						</div>  --%>    
						<!-- <div class="contentSection"></div> -->
						<!-- <input type="text" id="datepicker-2"  placeholder="mm-dd-yyyyy" style="width: 152px;margin-top: 10px;"/> -->
						<input type="text" id="datepicker-2"  placeholder="dd-mm-yyyy" readonly="readonly" style="width: 152px;margin-top: 5px;"/>  
					</span>
					<span style="float: left;"> 
						<button type="submit" id="simulatePlaceOrderId1" class="positive placeOrderButton">  
							<spring:theme code="simulateCheckout.summary.placeOrder" />
						</button>
					</span>
				</div>
			</c:if>
			<!-- Added for Estimated Delivery Date by Soma - END -->
			
			<c:if test="${siteUid eq 'personalCareEMEA'}">
				<button type="submit" id="simulatePlaceOrderId1" class="positive right placeOrderButton">  
					<spring:theme code="simulateCheckout.summary.placeOrder" />
				</button>
			</c:if>
			
			<%-- <button type="submit" id="simulatePlaceOrderId1" class="positive right placeOrderButton">  
				<spring:theme code="simulateCheckout.summary.placeOrder" />
			</button> --%>
			
		</form:form>
	</div>
	<div id="checkoutOrderDetails">
		<checkout:cartItems cartData="${cartData1}" />
		<div class="span-16 ">
			<cart:cartPromotions cartData="${cartData1}" />
			&nbsp;
		</div>
		<div class="span-8 last ">
			<checkout:cartTotals cartData="${cartData1}" showTaxEstimate="true"/> 
		</div>
	</div>

	<div class="span-24">
		<form:form action="${placeOrderUrl}" id="placeOrderForm2" modelAttribute="placeOrderForm">
		<c:if test="${isSalesRepUserLogin eq 'false'}">
			<formElement:formCheckbox idKey="Terms2PlaceOrder" labelKey="checkout.summary.placeOrder.readTermsAndConditions" inputCSS="checkbox-input" labelCSS="checkbox-label" path="termsCheck" mandatory="true" />
			</c:if>
			<button type="submit" id="simulatePlaceOrderId2" class="positive right placeOrderButton" >
				<spring:theme code="simulateCheckout.summary.placeOrder" />
			</button>
		</form:form>
	</div>

</template:page>
