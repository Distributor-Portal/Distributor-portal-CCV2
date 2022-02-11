<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/desktop/template" %>
<%@ taglib prefix="cart" tagdir="/WEB-INF/tags/desktop/cart" %>
<%@ taglib prefix="user" tagdir="/WEB-INF/tags/desktop/user" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/desktop/formElement"%>
<%@ taglib prefix="checkout" tagdir="/WEB-INF/tags/desktop/checkout" %>
<%@ taglib prefix="single-checkout" tagdir="/WEB-INF/tags/desktop/checkout/single" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/desktop/common" %>
<%@ taglib prefix="breadcrumb" tagdir="/WEB-INF/tags/desktop/nav/breadcrumb" %>

<spring:url value="/checkout/single/simulateOrder" var="placeOrderUrl" />
<spring:url value="/checkout/single/termsAndConditions" var="getTermsAndConditionsUrl" />

<template:page pageTitle="${pageTitle}">
	
	<script type="text/javascript">
		var getTermsAndConditionsUrl = "${getTermsAndConditionsUrl}";
	    var leadTime = '${leadTime}';
    </script>

	<div id="breadcrumb" class="breadcrumb"></div>

	<div id="globalMessages">
		<common:globalMessages/>
	</div>

<single-checkout:summaryFlow cartData="${cartData}" />


	<input type="hidden" value="${poNumberPattern}" id="poNumPatternId" />
	
	<div id="placeOrder" class="clearfix">
	<%-- <form:form action="${placeOrderUrl}" id="placeOrderForm1" commandName="placeOrderForm"> --%>
	
		<form:form action="${placeOrderUrl}"  modelAttribute="placeOrderForm">
			<input type="hidden" id="securityCode1" class="securityCodeClass" />
			<form:input type="hidden" id="securityCode" class="securityCodeCard" path="securityCode" value=""/>
			<form:input type="hidden" id="orderCommentsHiddenVar" path="orderComments" value=""/>
			
			
			<button type="submit" class="positive right" id="checkoutPlaceOrder" ><spring:theme code="checkout.summary.placeOrder"  /></button>
			
			<%-- <button type="submit" class="positive right placeOrderButton" ><spring:theme code="checkout.summary.placeOrder"/></button> --%>
			<%-- <button type="button" class="positive right requestQuoteButton"><spring:theme code="checkout.summary.negotiateQuote"/></button>
			<button type="button" class="positive right scheduleReplenishmentButton"><spring:theme code="checkout.summary.scheduleReplenishment"/></button> --%>
			
			
			<div class="terms left">
			<c:if test="${isSalesRepUserLogin eq 'false'}">
					<formElement:formCheckbox idKey="Terms1" labelKey="checkout.summary.placeOrder.readTermsAndConditions" inputCSS="checkbox-input" labelCSS="checkbox-label" path="termsCheck" mandatory="true" />
			</c:if>
			</div>
			 <%-- <cart:negotiateQuote/>
			 <cart:replenishmentScheduleForm/>  --%>
		</form:form>
		
		</div>
		
		
		
	
		<%-- <div id="checkoutOrderDetails" >
			<checkout:summaryCartItems cartData="${cartData}"/>
			<div class="span-16 ">
				<cart:cartPromotions cartData="${cartData}"/>
				&nbsp;
			</div>
			<div class="span-8 last ">
				<cart:ajaxCartTotals/>
			</div>
			
		</div> --%>
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
	
	<%-- <form:form action="${placeOrderUrl}" id="placeOrderForm2" commandName="placeOrderForm"> --%>
	
	<form:form action="${placeOrderUrl}" id="placeOrderForm2" modelAttribute="placeOrderForm">
		<form:input type="hidden" id="securityCode2" class="securityCodeClass" path="securityCode"/>
		
		<%-- <button type="submit" class="positive right placeOrderButton" ><spring:theme code="checkout.summary.placeOrder"/></button> --%>
		
		<button type="submit" class="positive right" id="checkoutPlaceOrder2" ><spring:theme code="checkout.summary.placeOrder"/></button>
		
		<div class="terms left">
		<c:if test="${isSalesRepUserLogin eq 'false'}">
			<formElement:formCheckbox idKey="Terms2" labelKey="checkout.summary.placeOrder.readTermsAndConditions" inputCSS="checkbox-input" labelCSS="checkbox-label" path="termsCheck" mandatory="true" />
		</c:if>
		</div>
	</form:form>

</div>
	
</template:page>
