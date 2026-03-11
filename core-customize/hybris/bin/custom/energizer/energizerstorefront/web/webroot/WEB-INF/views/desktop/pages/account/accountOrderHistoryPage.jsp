<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/desktop/template" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="nav" tagdir="/WEB-INF/tags/desktop/nav" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/desktop/common" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="breadcrumb" tagdir="/WEB-INF/tags/desktop/nav/breadcrumb" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:url value="/my-account/filterOrders" var="getfilterOrdersUrl"/>

<link href="${commonResourcePath}/css/cal.css" rel="stylesheet">
<script src="${commonResourcePath}/js/jquery-1.7.2.min.js"></script>
<script src="${commonResourcePath}/js/jquery-ui.js"></script>
 
<script type="text/javascript">
 $(function() {
	$(".datepicker").datepicker({
        dateFormat : 'mm/dd/yy',
        showOn : "button",
        buttonImage : "/_ui/desktop/common/images/calendar.gif",
        buttonImageOnly : true,
        buttonText : "Select Date",
        firstDay: 0,
  });
}); 

function resetFilterOrdersForm(){
	$('#filterOrdersForm').find("input[type=text]").val("");
}
</script> 
<template:page pageTitle="${pageTitle}">

	<div id="breadcrumb" class="breadcrumb">
		<breadcrumb:breadcrumb breadcrumbs="${breadcrumbs}"/>
	</div>
	<div id="globalMessages">
		<common:globalMessages/>
	</div>
	<nav:accountNav selected="orders" />
	
	
	
	<div class="column accountContentPane clearfix orderList">
		<div class="headline"><spring:theme code="text.account.orderHistory" text="Order History"/></div>
		<c:if test="${not empty searchPageData.results}">
			<div class="description"><spring:theme code="text.account.orderHistory.viewOrders" text="View your orders"/></div>

	<div style="margin-top: 10px;">
	   <form:form action="${getfilterOrdersUrl}" modelAttribute="customOrderSelectionForm" method="POST" id="filterOrdersForm">
			<span class="description"><spring:theme code="text.account.order.filter.from.date" text="From Date: " />
				<form:input path="fromDate" class="datepicker" type="text" placeholder="mm/dd/yyyy" style="width: 152px;" autocomplete="off"/>
			</span>	
				&nbsp;&nbsp;&nbsp;&nbsp;     
			<span class="description"><spring:theme code="text.account.order.filter.to.date" text="To Date: " />
				<form:input path="toDate"  class="datepicker" type="text" placeholder="mm/dd/yyyy" style="width: 152px;" autocomplete="off"/>
			</span>
			   
			<span style="float: right; margin-top: -18px;">
				<form:button class="positive test" type="button" style="margin-right: 25px;" onclick="resetFilterOrdersForm();">
					<spring:theme code="text.account.order.filter.reset.dates" text="Reset Dates" />
				</form:button>
				<form:button class="positive test" type="submit">
					<spring:theme code="text.account.order.filter" text="Filter" />
				</form:button>
			</span>
		</form:form> 
	</div>	
			<nav:pagination top="true"  supportShowPaged="${isShowPageAllowed}"  supportShowAll="${isShowAllAllowed}"  searchPageData="${searchPageData}" searchUrl="/my-account/orders?sort=${searchPageData.pagination.sort}" msgKey="text.account.orderHistory.page"  numberPagesShown="${numberPagesShown}"/>
  
			<table class="orderListTable">
				<thead>
					<tr>
					<th id="header1"><spring:theme code="text.account.orderHistory.referenceNumber" text="Reference No"/></th>
					<c:choose>
					<c:when test="${not empty siteUid && siteUid eq 'personalCareEMEA'}">
						
						<th id="header7"><spring:theme code="text.account.orderHistory.poNumber" text="P.O.Number"/></th>
					</c:when>
					<c:otherwise>
					</c:otherwise>
					</c:choose>
						<th id="header2"><spring:theme code="text.account.orderHistory.sap.orderNumber" text="Order No"/></th>
						<%-- <th id="header3"><spring:theme code="text.account.orderHistory.orderType" text="Order Type"/></th> --%>
						<th id="header3"><spring:theme code="text.account.order.purchase" text="Purchaser"/></th> 
						 <c:if test="${isSalesRepUserLogin}">
						<th id="header8"><spring:theme code="text.account.order.shipTo" text="Ship To"/></th>
						</c:if>
						<c:if test="${isSalesRepUserLogin}">
						<th id="header9"><spring:theme code="text.account.order.companyName" text="Company Name"/></th>
						</c:if>
						<th id="header4"><spring:theme code="text.account.orderHistory.approvalStatus" text="Approval Status"/></th>
						<th id="header5"><spring:theme code="text.account.orderHistory.datePlaced" text="Date Placed"/></th>
						<th id="header6"><spring:theme code="text.account.orderHistory.orderValue" text="Order Value"/></th>
						<c:if test="${isSalesRepUserLogin}">
						<th id="header10"><spring:theme code="text.account.order.invoiceNumber" text="Invoice Number"/></th>
						</c:if>
						<%-- <th id="header7"><spring:theme code="text.account.orderHistory.actions" text="Actions"/></th> --%> 
					</tr>
				</thead>
				<tbody>
					<c:forEach items="${searchPageData.results}" var="order">

						<c:url value="/my-account/order/${order.code}" var="myAccountOrderDetailsUrl"/>
								
						<tr>
							<td headers="header1">
								<ycommerce:testId code="orderHistory_ReferenceNumber_link">
									<c:choose>
										<c:when test="${empty order.erpOrderCreator}">
											<a href="${myAccountOrderDetailsUrl}">${order.code}</a>
										</c:when>
										<c:otherwise>
											<p><spring:theme code="text.account.orderHistory.notApplicable" text="Not Applicable" /></p> 
										</c:otherwise>
									 </c:choose>
									
								</ycommerce:testId>
							</td>
							<c:choose>
								<c:when test="${not empty siteUid && siteUid eq 'personalCareEMEA'}">
											<td headers="header7">
												${order.purchaseOrderNumber}
											</td>
								</c:when>
								<c:otherwise>
									
								</c:otherwise>
							</c:choose>	
							<td headers="header2">
								<ycommerce:testId code="orderHistory_orderNumber_link">
									<a href="${myAccountOrderDetailsUrl}">${order.referenceNumber}</a>
								</ycommerce:testId>
							</td>
							<td headers="header3">
							<ycommerce:testId code="orderHistory_purchaser_label">
							<c:choose>
								<c:when test="${not empty order.salesRepName}">
									${order.salesRepName}
								</c:when>
								<c:otherwise>
										<p>${order.b2bOrderData.b2bCustomerData.name}</p>
								</c:otherwise>
							</c:choose> 
							</ycommerce:testId>
							</td>
						   <c:if test="${isSalesRepUserLogin}">		
							<td headers="header8">
								<ycommerce:testId code="orderHistory_shipTo_label">
									${order.b2bOrderData.deliveryAddress.erpAddressId}
								</ycommerce:testId>
							</td>
							</c:if>
							<c:if test="${isSalesRepUserLogin}">	
							<td headers="header9">
								<ycommerce:testId code="orderHistory_companyName_label">
									${order.b2bOrderData.deliveryAddress.companyName}
								</ycommerce:testId>
							</td>	
							</c:if> 
											
							<%-- <td headers="header3"> 
								<ycommerce:testId code="orderHistory_orderType_label">
									 <c:if test="${empty order.erpOrderCreator}">
									   <p><spring:theme code="text.account.order.portal" /></p>
									</c:if>
								  	<c:if test="${not empty order.erpOrderCreator}">
									   <p><spring:theme code="text.account.order.offline" /></p>
									</c:if>
								</ycommerce:testId>
							</td> --%>
							
							
							<td headers="header4">
								<ycommerce:testId code="orderHistory_orderStatus_label">
									<%-- <p><spring:theme code="text.account.order.status.display.${order.statusDisplay}"/></p> --%>
									<p><spring:theme code="text.account.order.status.display.${order.status}"/></p>
								</ycommerce:testId>
							</td>
							<td headers="header5">
								<ycommerce:testId code="orderHistory_orderDate_label">
									<p><fmt:formatDate value="${order.placed}" dateStyle="long" timeStyle="short" type="both"/></p>
								</ycommerce:testId>
							</td>
							<td headers="header6">
								<ycommerce:testId code="orderHistory_Total_links">
								<c:set var="originalFormattedVal" value="${order.total.formattedValue}" />
								<c:set var="currencyISO" value="${order.total.currencyIso}" /> 
								<c:choose>
									<c:when test="${order.b2bOrderData.placedBySalesRep and currencyISO eq 'USD' and fn:contains(originalFormattedVal, '$')}">
										<c:set var="priceValue" value="${fn:substringAfter(originalFormattedVal, '$')}" />
										<c:set var="currencyIsoPriceValue" value="${currencyISO}&nbsp;${priceValue}" />
										<p>${currencyIsoPriceValue}</p>
									</c:when>
									<c:when test="${order.b2bOrderData.placedBySalesRep and currencyISO eq 'USD' and fn:contains(originalFormattedVal, currencyISO)}">
										<c:set var="priceValue" value="${fn:substringAfter(originalFormattedVal, currencyISO)}" />
										<c:set var="currencyIsoPriceValue" value="${currencyISO}&nbsp;${priceValue}" />
										<p>${currencyIsoPriceValue}</p>
									</c:when>
									<c:when test="${order.b2bOrderData.placedBySalesRep and currencyISO ne 'USD' and fn:contains(originalFormattedVal, currencyISO)}">
										<c:set var="priceValue" value="${fn:substringAfter(originalFormattedVal, currencyISO)}" />
										<c:set var="currencyIsoPriceValue" value="${currencyISO}&nbsp;${priceValue}" />
										<p>${currencyIsoPriceValue}</p>
									</c:when>
									<c:otherwise>
										<p>${originalFormattedVal}</p>
									</c:otherwise>
								</c:choose>
								</ycommerce:testId>
							</td>
							<c:if test="${isSalesRepUserLogin}">	
							<td headers="header10">
								<ycommerce:testId code="orderHistory_invoiceNumber_label">
								 <p>${order.b2bOrderData.invoiceNumber}</p>
								</ycommerce:testId>
							</td>	
							</c:if> 
							 <%-- <td headers="header7">
								<ycommerce:testId code="orderHistory_Actions_links">
									<p><a href="${myAccountOrderDetailsUrl}"><spring:theme code="text.view" text="View"/></a></p>
								</ycommerce:testId>
							</td>  --%>
						</tr>
					</c:forEach>
				</tbody>
			</table>

			<nav:pagination top="false" supportShowPaged="${isShowPageAllowed}"  supportShowAll="${isShowAllAllowed}" searchPageData="${searchPageData}" searchUrl="/my-account/orders?sort=${searchPageData.pagination.sort}" msgKey="text.account.orderHistory.page"  numberPagesShown="${numberPagesShown}"/>

		</c:if>
		
		<c:choose>
			<c:when test="${empty searchPageData.results and not filterOrdersRequest}">
				<p><spring:theme code="text.account.orderHistory.noOrders" text="You have no orders"/></p>
			</c:when>
			<c:when test="${empty searchPageData.results and filterOrdersRequest}">
				<p><spring:theme code="text.account.orderHistory.noOrders.date.range" text="You have no orders in the selected date range. Please choose a different date range."/></p>
				
				<div style="margin-top: 10px;">
				   <form:form action="${getfilterOrdersUrl}" modelAttribute="customOrderSelectionForm" method="POST" id="filterOrdersForm">
						<span class="description"><spring:theme code="text.account.order.filter.from.date" text="From Date: " />
							<form:input path="fromDate" class="datepicker" type="text" placeholder="mm/dd/yyyy" style="width: 152px;" autocomplete="off"/>
						</span>	
							&nbsp;&nbsp;&nbsp;&nbsp;     
						<span class="description"><spring:theme code="text.account.order.filter.to.date" text="To Date: " />
							<form:input path="toDate"  class="datepicker" type="text" placeholder="mm/dd/yyyy" style="width: 152px;" autocomplete="off"/>
						</span>
						     
						<span style="float: right; margin-top: -18px;">
							<form:button class="positive test" type="button" style="margin-right: 25px;" onclick="resetFilterOrdersForm();">
								<spring:theme code="text.account.order.filter.reset.dates" text="Reset Dates" />
							</form:button>
							<form:button class="positive test" type="submit">
								<spring:theme code="text.account.order.filter" text="Filter" />
							</form:button>
						</span>
					</form:form> 
				</div>
			</c:when>
		</c:choose>
	</div>

</template:page>
