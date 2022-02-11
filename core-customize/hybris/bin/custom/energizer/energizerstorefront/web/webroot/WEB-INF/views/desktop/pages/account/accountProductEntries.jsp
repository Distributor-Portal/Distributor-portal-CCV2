<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/desktop/template"%>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme"%>
<%@ taglib prefix="nav" tagdir="/WEB-INF/tags/desktop/nav"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/desktop/common"%>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/desktop/formElement"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags"%>
<%@ taglib prefix="breadcrumb"
	tagdir="/WEB-INF/tags/desktop/nav/breadcrumb"%>


<template:page pageTitle="${pageTitle}">
	<div id="breadcrumb" class="breadcrumb">
		<breadcrumb:breadcrumb breadcrumbs="${breadcrumbs}" />
	</div>
	<div id="globalMessages">
		<common:globalMessages />
	</div>
	<nav:accountNav selected="fileupload" />
	
		<div>
		<c:if test="${not empty cartData and not empty cartData.shippingPoint}">
		<p><spring:theme code="text.account.quickorder.currentShippingPointInCart"/>${cartData.shippingPoint}</p>
		
		</c:if>
		</div>
		
		
	<c:url value="/my-cart/addtocart" var="encodedUrl"  scope="page" />
	<form:form  name="testform" action="${encodedUrl}" id="excelupload" method="post" modelAttribute = "excelUploadForm">
		<table  cellspacing="10" cellpadding="10" border="1">
			<thead>
				<tr>
						<th bordercolor="solid black" id="header1" style="text-align:left;">
						<spring:theme
						code="text.account.excelUpload.orderShipmentPoint" text="Shipping Point" />
						</th>
						
						<%-- <th id="header2"><spring:theme
						code="text.account.excelUpload.productData" text="Product Data" />
						 --%>
						 
									
									<!--	<th id="header3" >
										<spring:theme
						code="text.account.excelUpload.orderShipmentPointName" text="Shipping Point Name" />
										
										</th> -->
								
										<th id="header4">
										<spring:theme
										code="text.account.excelUpload.materialId" text="Material Id" />
										</th>
						
										<th id="header5">
										<spring:theme
										code="text.account.excelUpload.customerMaterialId" text="Customer Material Id" />
										</th>
						
										<th id="header6">
										<spring:theme
										code="text.account.excelUpload.uom" text="UOM" />
										</th>
										<th id="header7">
										<spring:theme
										code="text.account.excelUpload.quantity" text="Quantity" />
						              </th>
										
										

					</tr>
			
			</thead>
			
			<tbody>
				
				<c:forEach items="${shipmentData}" var="shipmentDetails">				
						

					<tr>
						<td headers="header1" >
						</td>
								<c:set var="setShipmentKey" value="0"></c:set>
								<c:forEach items="${shipmentDetails.value}" var="uploadDataList" varStatus="listLoop">
								
							<tr>
							<c:if test="${setShipmentKey eq 0 and listLoop.index eq 0}">
							<td headers="header2" style="text-align:left;"><ycommerce:testId
								code="orderHistory_orderNumber_link">
								
								<c:choose>
								<c:when test="${siteUid eq 'personalCareEMEA'}">
								<input type="checkbox" id='buttonStatus_${shipmentDetails.key}' name="shippingPoint" class="shippingPointRadioButton" value="${shipmentDetails.key}">
								</c:when>
								<c:otherwise>
								<input type="radio" id='buttonStatus_${shipmentDetails.key}' name="shippingPoint" class="shippingPointRadioButton" value="${shipmentDetails.key}">
								</c:otherwise>	
								</c:choose>	
														
								<c:out value="${shipmentDetails.key}" default="Not Available"
									escapeXml="false"></c:out>&nbsp-
									</ycommerce:testId>
									<c:set var="setShipmentKey" value="1"></c:set>
									
									<c:forEach items="${shipmentName}" var="shipmentNameDetails">
								<c:set var="setShipmentNameKey" value="0"></c:set>
							    <c:if test="${shipmentDetails.key eq shipmentNameDetails.key}">
							  <c:out value="${shipmentNameDetails.value}" default="Not Available"
									escapeXml="false"></c:out>
									</c:if>
									
								</c:forEach> 
									
									</td>
							</c:if>
						
							<c:if test="${setShipmentKey eq 1 and listLoop.index gt 0}">
							<td headers="header2" >&nbsp;&nbsp;</td>
							</c:if>
								
								
								
								<td headers="header3"><ycommerce:testId
								code="orderHistory_orderNumber_link">
								<c:out value="${uploadDataList.materialId}" default="Not Available"
									escapeXml="false"></c:out>
								</ycommerce:testId></td>
								<td headers="header4"><ycommerce:testId
								code="orderHistory_orderStatus_label">
								<c:out value="${uploadDataList.customerMaterialId}"
									default="Not Available" escapeXml="false"></c:out>
								</ycommerce:testId></td>
								<td headers="header5" ><ycommerce:testId
								code="orderHistory_orderDate_label">
								<c:out value="${uploadDataList.uom}" default="Not Available"
									escapeXml="false"></c:out>
								</ycommerce:testId></td>
								<td headers="header6" cellspacing="10" colspan="1"><ycommerce:testId
								code="orderHistory_orderDate_label">
								<%-- <c:out value="${uploadDataList.quantity}" default="Not Available"
									escapeXml="false"></c:out> --%>
										
								<input type="hidden" name="productCode" value="${uploadDataList.materialId}"/>
								<!-- Added for Ordering quantity limit to 7 digits for EMEA - START -->
								<c:choose>
									<c:when test="${siteUid eq 'personalCareEMEA'}">
										<input type="text" name="qty" maxlength="7" size="4" class="excelOrderModifiedQuantity" value="${uploadDataList.quantity}"  />
									</c:when>
									<c:otherwise>
										<input type="text" name="qty" size="4" class="excelOrderModifiedQuantity" value="${uploadDataList.quantity}"  />
									</c:otherwise>
								</c:choose>     
								<!-- Added for Ordering quantity limit to 7 digits for EMEA - END -->
								<input type="hidden" name="moq" class="minorderQuantity" value="${uploadDataList.orderingUnit}" />	
								</ycommerce:testId></td>
								
													
									
						</tr>
							</c:forEach>
					</tr>
				</c:forEach>
				
			</tbody>
			
		</table>
		
		<c:if test="${not empty shipmentData}">
		<div id="addToCartform_button_panel" class="button positive">
				<button class="excelOrderSubmitButton button" id='continueButton' > <spring:theme code="text.addToCart" text="Add To cart"/></button>
			</div>		
		</c:if>
			

		
	</form:form>		
	
</template:page>

