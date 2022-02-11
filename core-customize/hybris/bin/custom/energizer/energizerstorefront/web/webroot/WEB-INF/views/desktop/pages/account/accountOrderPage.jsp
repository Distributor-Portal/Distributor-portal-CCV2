<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/desktop/template" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="nav" tagdir="/WEB-INF/tags/desktop/nav" %>
<%@ taglib prefix="order" tagdir="/WEB-INF/tags/desktop/order" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/desktop/common" %>
<%@ taglib prefix="breadcrumb" tagdir="/WEB-INF/tags/desktop/nav/breadcrumb" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:useBean id="DateTimeUtil"  class="com.energizer.storefront.util.EnergizerDateTimeUtil" /> 


<template:page pageTitle="${pageTitle}">

	<script type="text/javascript"> 
		
		function openDeliveryNoteFile(orderCode, fileName){
			window.open('/my-account/downloadDeliveryNoteFile?orderCode=' + orderCode + '&fileName=' + fileName + '&inline=false', fileName);
		}
		
		function removeDeliveryNoteFile(orderCode, fileName){ 
			console.log('remove delivery note file');
			$.ajax({
                url: '/my-account/removeDeliveryNoteFile',
                data : {'orderCode' : orderCode, 'fileName' : fileName},
                type: 'POST',    
                success: function(data) {
                	if(data=="Success"){	
                		//console.log('Remove delivery note success !');
                		location.href = location.href;
                	}
                	if(data=="Error"){
                		//console.log('Remove delivery note failure !');
                	}
                }
});
			
		}
	</script>

	<div id="breadcrumb" class="breadcrumb">
		<breadcrumb:breadcrumb breadcrumbs="${breadcrumbs}"/>
	</div>
	<div id="globalMessages">
		<common:globalMessages/>
	</div>
	<nav:accountNav selected="orders" />

	<div class="column accountContentPane clearfix">
		<div class="headline">Order Details</div>
		<div class="span-19">
			<div class="span-7 spacing_cls" style="width: 338px !important;">
				<c:choose>
					<c:when test="${empty orderData.erpOrderCreator}">
						 <spring:theme code="text.account.orderHistory.orderNumber" /> : ${orderData.code}<br />
					</c:when>
					<c:otherwise>
						<spring:theme code="text.account.orderHistory.orderNumber" /> : <p><spring:theme code="text.account.orderHistory.notApplicable" text="Not Applicable" /></p></br>
					</c:otherwise>
				 </c:choose>
				
				<c:if test="${not empty orderData.erpOrderNumber}">
				<spring:theme code="text.account.orderHistory.SAPorderNumber" /> : ${orderData.erpOrderNumber}<br />
				</c:if>
				<spring:theme code="text.account.orderHistory.orderPlaced" /> : 
				<c:choose>
					<c:when test="${siteUid eq 'personalCare'}">
						<c:if test="${not empty orderData.created}">
							${DateTimeUtil.displayDate(orderData.created)}
						</c:if>
					</c:when>
					<c:otherwise>
						<c:if test="${not empty orderData.created}">
							${DateTimeUtil.displayRequestedDeliveryDate(orderData.created,'dd/MM/yyyy')} 
						</c:if>
					</c:otherwise>
				</c:choose>
				
				<br />
				<c:if test="${not empty orderData.statusDisplay}">
					<p class="reduce_space_cls"><spring:theme code="text.account.orderHistory.orderStatus" /> : <spring:theme code="text.account.order.status.display.${orderData.status}" /></p>
					<p class="reduce_space_cls"><spring:theme code="text.account.orderHistory.poNumber" /> : ${orderData.purchaseOrderNumber}</p>
				</c:if>	
				<c:choose>
					<c:when test="${empty orderData.erpOrderCreator}">
					<p class="reduce_space_cls"> <spring:theme code="text.account.order.type" /> : <spring:theme code="text.account.order.portal" /></p>
					</c:when>
					<c:otherwise>
					 <p class="reduce_space_cls"><spring:theme code="text.account.order.type" /> :<spring:theme code="text.account.order.offline" /></p>
					</c:otherwise>
				 </c:choose>
				 
				<c:if test="${not empty orderData.orderComments}">
					<spring:theme code="text.account.orderHistory.OrderComments" /> : 
						<p class="reduce_space_cls" style="word-wrap: break-word;text-align: justify;">
							${orderData.orderComments}
						</p>				
				</c:if>	
			</div>	
			
	
			<div class="span-5">&nbsp;
				<order:receivedPromotions order="${orderData}"/>
			</div>
			
			<c:if test="${orderData.triggerData ne null}">
				<order:replenishmentScheduleInformation order="${orderData}"/>
			</c:if>
			
			<div class="span-6 last order-totals">
				<order:orderTotalsItem order="${orderData}"/>
			</div>
			
			<div class="span-19 last orderFix-cls">
				<%-- <sec:authorize ifAnyGranted="ROLE_B2BCUSTOMERGROUP,ROLE_B2BADMINGROUP,ROLE_EMPLOYEEGROUP"> --%>
				<sec:authorize access="hasAnyRole('ROLE_B2BCUSTOMERGROUP','ROLE_B2BADMINGROUP','ROLE_EMPLOYEEGROUP')">
					<order:reorderButton order="${orderData}"/>
				</sec:authorize>	
				<%-- <fmt:parseDate var="invoiceCutOffDate" value="Aug 30 2016" pattern="MMM dd yyyy" />  --%>		
				<c:if test="${not empty orderData.status && orderData.status == 'INVOICED'}"> 
					<order:viewInvoiceButton orderData="${orderData}"/>					
				</c:if> 
			</div>
			
		</div>
		
		<div class="orderBoxes clearfix">
			<!--  Upload & Display Delivery Note Files ONLY for EMEA - START -->
			<%-- <c:if test="${siteUid eq 'personalCareEMEA'}"> --%>
			<c:if test="${not isSalesRepLoggedIn}">
				<order:deliveryNotesItem order="${orderData}"/>
			</c:if>	
			<%-- </c:if> --%>
			<!--  Upload & Display Delivery Note Files ONLY for EMEA - END -->
				
			<order:deliveryAddressItem order="${orderData}"/>
			<order:orderShippingDetails order="${orderData}"/>
			<!-- display only if credit card payment -->
			<c:if test="${orderData.paymentType.code.equals(CheckoutPaymentType.CARD.getCode()) }">
				<div class="orderBox billing">
					<order:billingAddressItem order="${orderData}"/>
				</div>
			</c:if>
			<c:if test="${not empty orderData.paymentInfo}">
				<div class="orderBox payment">
					<order:paymentDetailsItem order="${orderData}"/>
				</div>
			</c:if>
		</div>
		<c:if test="${not orderData.placedBySalesRep}">
			<c:if test="${not empty orderData.b2bPermissionResult}">
				<order:orderApprovalDetailsItem order="${orderData}" />
			</c:if> 
			
			<c:if test="${not empty siteUid && siteUid eq 'personalCare'}" >
				<order:orderHistoryLoadingDetails order="${orderData}" />
			</c:if>
		</c:if>
	    <order:energizerOrderDetailsItem order="${orderData}"/> 

	</div>
</template:page>