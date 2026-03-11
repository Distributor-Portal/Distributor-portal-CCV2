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

<template:page pageTitle="${pageTitle}">

	<div id="breadcrumb" class="breadcrumb">
		<breadcrumb:breadcrumb breadcrumbs="${breadcrumbs}"/>
	</div>
	
	<div id="globalMessages">
		<common:globalMessages/>
	</div>
	
	<nav:accountNav selected="quickorder" />
	
	<script>
	 var enterWholeNumber='<spring:theme code="text.account.quickorder.enterwholenumber"/>';
	 var enterPositiveNumber='<spring:theme code="text.account.quickorder.enterpositivenumber"/>';
	 var enterMOQMultiples='<spring:theme code="text.account.quickorder.entermoqmultiples"/>';
	 var inputValidNumber='<spring:theme code="text.account.quickorder.inputvalidnumber"/>';
	</script>
	
	<div class="column accountContentPane clearfix orderList">
		<div class="headline"><spring:theme code="text.account.quickorder.pageHeading"/></div>
		
		<div>
		<c:if test="${not empty cartData and not empty cartData.shippingPoint}">
		<p><spring:theme code="text.account.quickorder.currentShippingPointInCart"/>${cartData.shippingPoint}</p>
		
		</c:if>
		</div>
			<div class="new_item_quick_order_form">
			<c:url value="/my-account/quickorder/addItem" var="quickOrderAddItemURL"></c:url>
			<form action="${quickOrderAddItemURL}" method="post">
				<table>
					<tr>
						<td headers="header1">
							<!-- <input type="text" name="energizerMaterialID" size="20" placeholder="Edgewell Product Code"/> -->
							<input type="text" name="energizerMaterialID" size="20" placeholder="<spring:theme code="text.account.quickorder.productCode"/>"/>
						</td>
						<td headers="header1">
							<div class="quickOrder_text_btw_txt">(or)</div>
						</td>
						<c:choose>
							<c:when test="${isSalesRepUserLogin}">
								<td headers="header2">
									 <!-- <input type="text" name="distributorMaterialID" size="20" placeholder="Customer Product Code"/> -->
									 <input type="text" name="distributorMaterialID" size="20" placeholder="UPC/EAN"/>
								</td>
							</c:when>
							<c:otherwise>
								<td headers="header2">
									 <input type="text" name="distributorMaterialID" size="20" placeholder="Distributor Product Code"/>
								</td>
							</c:otherwise>
						</c:choose>
						
						<td headers="header3">
							<input type="submit" class="button" value="<spring:theme code="quickorder.add.product"/>">
						</td>
						<td headers="header4">
							
						</td>
						<td headers="header5">
							
						</td>
					</tr>
				</table>
		 </form>
		</div>
		
		
		<c:if test="${not empty orderform}">
			

			<table class="orderListTable orderListTblFix">
				<thead>
					<tr>
						<th id="header2"><spring:theme code="text.account.quickorder.productCode" text="Edgewell Product Code"/></th>
					<!--  Added product description in quick order page for both LATAM & EMEA start -->
						<th id="header2"><spring:theme code="text.account.quickorder.productDescription" text="Product Description"/></th>											
		     		<!--  Added product description in quick order page for both LATAM & EMEA End-->			
			     		<c:choose>
			     			<c:when test="${isSalesRepUserLogin}">
			     				<%-- <th id="header2"><spring:theme code="text.account.quickorder.customerProductCode" text="Customer Product Code"/></th> --%>
			     				<th id="header2"><spring:theme code="text.account.quickorder.upc.ean" text="UPC/EAN"/></th>  
			     			</c:when>
			     			<c:otherwise>
			     				<th id="header2"><spring:theme code="text.account.quickorder.distributorProductCode" text="Distributor Product Code"/></th>
			     			</c:otherwise>
			     		</c:choose>		
						<th id="header2"><spring:theme code="text.account.quickorder.unitOfMeasure" text="Unit Of Measure"/></th>
						<th id="header4"><spring:theme code="text.account.quickorder.quantity" text="Quantity"/></th>
						<th id="header5"><spring:theme code="text.account.quickorder.actions" text="Actions"/></th>
					</tr>
				</thead>
				<tbody>
					<c:forEach items="${orderform.lineItems}" var="entry">

						<tr>
							<td headers="header2">
								${entry.product.code}
							</td>
					<!--  Added product description in quick order page for both LATAM & EMEA start -->							
							<td headers="header2">
								${entry.product.name}
							</td>
					<!--  Added product description in quick order page for both LATAM & EMEA start -->					
							<td headers="header2">
								${entry.product.customerMaterialId}
							</td>
							<td headers="header2">
								${entry.product.uom}
							</td>
							<td headers="header4" class="header4_cls">
								<input type="hidden" name="productCode" value="${entry.product.code}"/>
								<!-- Added for Ordering quantity limit to 7 digits for EMEA - START -->
								<c:choose>
									<c:when test="${siteUid eq 'personalCareEMEA'}">
										<input type="text" name="qty" maxlength="7" size="4" class="orderEntryQuantity" id="${entry.product.code}" value="${entry.quantity}"  />
									</c:when>
									<c:otherwise>
										<input type="text" name="qty" size="4" class="orderEntryQuantity" id="${entry.product.code}" value="${entry.quantity}"  />
									</c:otherwise>
								</c:choose>  
								<!-- Added for Ordering quantity limit to 7 digits for EMEA - END -->
								<input type="hidden" name="moq" value="${entry.product.moq}"/>
								<span class="quickorder_errormsg display_none"></span>
							</td>
							<td headers="header5">
							<c:url value="/my-account/quickorder/removeItem" var="quickOrderRemoveItemURL"></c:url>
							<form action="${quickOrderRemoveItemURL}" method="post">
							<input type="hidden" name="energizerMaterialID" value="${entry.product.code}"/>
							<input type="submit" class="button_neg_cls" value="<spring:theme code="quickorder.remove.product"/>"/>
							</form>
							</td>
							
							
						</tr>
					</c:forEach>
				
				</tbody>
			</table>
			<c:if test="${not empty orderform.lineItems}">
			<div class="new_item_quick_order_form">
			<c:url value="/my-account/quickorder/addToCartAndContinue" var="quickOrderAddToCartURL"></c:url>
				<form action="${quickOrderAddToCartURL}" method="get">
							<input type="submit" class="quickOrderSubmitButton button" value="<spring:theme code="text.account.quickorder.continueToCheckout"/>"/>
							</form>
				<c:url value="/my-account/quickorder/resetQuickOrder" var="quickOrderResetCartURL"></c:url>
				<form action="${quickOrderResetCartURL}" method="get">
							<input type="submit" class="button" value="<spring:theme code="text.account.quickorder.resetOrderForm"/>"/>
							</form>
			</div>
			</c:if>
			
			
		</c:if>
		
		
		<c:if test="${empty orderform}">
			
			<table class="orderListTable">
				<thead>
					<tr>
						<th id="header2"><spring:theme code="text.account.quickorder.productCode" text="Edgewell Product Code"/></th>
				<!--  Added product description in quick order page for both LATAM & EMEA start -->
						<th id="header2"><spring:theme code="text.account.quickorder.productDescription" text="Product Description"/></th>						
				<!--  Added product description in quick order page for both LATAM & EMEA start -->
						<%-- <th id="header2"><spring:theme code="text.account.quickorder.customerProductCode" text="Distributor Product Code"/></th> --%>
						<c:choose>
			     			<c:when test="${isSalesRepUserLogin}">
			     				<%-- <th id="header2"><spring:theme code="text.account.quickorder.customerProductCode" text="Customer Product Code"/></th> --%>
			     				<th id="header2"><spring:theme code="text.account.quickorder.upc.ean" text="UPC/EAN"/></th>  
			     			</c:when>
			     			<c:otherwise>
			     				<th id="header2"><spring:theme code="text.account.quickorder.distributorProductCode" text="Distributor Product Code"/></th>
			     			</c:otherwise>
			     		</c:choose>
						<th id="header2"><spring:theme code="text.account.quickorder.unitOfMeasure" text="Unit Of Measure"/></th>
						<th id="header4"><spring:theme code="text.account.quickorder.quantity" text="Quantity"/></th>
						<th id="header5"><spring:theme code="text.account.quickorder.actions" text="Actions"/></th>
					</tr>
				</thead>
				<tbody>
						<tr>
							<td headers="header2">
								
							</td>
							<td headers="header2">

							</td>
							<td headers="header2">

							</td>
							<td headers="header2">
			
							</td>
							<td headers="header4">
							
							</td>
							<td headers="header5">

							</td>
					</tr>
				</tbody>
			</table>
			
		</c:if>
	
		
	</div>
	

</template:page>

