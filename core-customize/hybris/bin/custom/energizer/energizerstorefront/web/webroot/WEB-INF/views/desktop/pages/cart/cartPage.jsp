<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/desktop/template" %>
<%@ taglib prefix="cart" tagdir="/WEB-INF/tags/desktop/cart" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/desktop/common" %>
<%@ taglib prefix="breadcrumb" tagdir="/WEB-INF/tags/desktop/nav/breadcrumb" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<spring:theme text="Your Shopping Cart" var="title" code="cart.page.title"/>

<c:url value="/checkout/single" var="checkoutUrl"/>
<c:url value="/cart/clearCart" var="clearCartUrl"/>
<c:url value="/cart/getPrice" var="getPriceUrl"/>
<input type="hidden" id="checkoutUrl" name="checkoutUrl" value="${checkoutUrl}" />

<template:page pageTitle="${pageTitle}">
<c:set var="currencyISO" value="${cartData.totalPrice.currencyIso}" />	
	<c:if test="${siteUid eq 'personalCare'}">
		<form id="checkoutButtonForm" method="GET" action="${checkoutUrl}"></form>
	</c:if>
	
	<spring:theme code="basket.add.to.cart" var="basketAddToCart"/>
	<spring:theme code="cart.page.checkout" var="checkoutText"/>
	<div id="breadcrumb" class="breadcrumb">
		<breadcrumb:breadcrumb breadcrumbs="${breadcrumbs}"/>
	</div>
	
	<div id="businesRuleErrors">
	</div>
	
	<div id="globalMessages">
		<common:globalMessages/>
	</div>
	
	<%-- Error Message includes validation for Expected Unit Price & Agree Edgewell Unit Price - ONLY for LATAM - START --%>
	<c:if test="${siteUid eq 'personalCare'}">
		<div class="alert negative" id="showErrorMessageForAgreeEdgewellPrice" style="display: none;">
			<spring:theme code="${fn:escapeXml('agree.negotiate.edgewell.unit.price')}"/>
		</div>
	</c:if>
	<c:if test="${isUpdateQtyError}">
	<div class="alert negative" id="showErrorMessageForItemQtyUpdate" style="border-color: #c90400;color: #c90400;">
			<spring:theme code="${fn:escapeXml('cart.item.quantity.update.error.message')}"/>
	</div>
	</c:if>
	<input type="hidden" id="freightType" name="freightType" value="${freightType}" />
	<input type="hidden" id="palletType" name="palletType" value="${palletType}" />
	
	
	<div class="alert negative" id="showErrorMessageForEdgewellPriceValidation" style="display: none;">
		<spring:theme code="${fn:escapeXml('validation.edgewell.unit.price')}"/>
	</div>
	
	<%-- Error Message includes validation for Expected Unit Price & Agree Edgewell Unit Price - ONLY for LATAM - END --%>
					
 	<c:if test="${not empty cartData.productsNotAddedToCart}">
	<div id="productsNotAddedToCart" class="alert negative"><spring:theme code="product.notadded.container"/></div>
	<div >
	<table style="padding: 0px; border-collapse: collapse;" class="productsNotAdded alert negative" border="1">
		<thead class="alert negative">
			<tr>
			<th class="alert negative" id="header1" colspan="2"><spring:theme code="errorMessages.erpmaterialid"/></th>

				<th id="header2" colspan="2"> <spring:theme code="errorMessages.Quantity"/></th>
			</tr>
			</thead>
			<tbody class="alert negative">
				<c:forEach items="${cartData.productsNotAddedToCart}" var="entry">
				<tr><td class="alert negative prod_det" colspan="2" headers="header1">${entry.key}</td>
				 <td headers="header2" colspan="2">&nbsp;&nbsp;&nbsp;${entry.value}</td></tr>
			 </c:forEach>
			</tbody>
			</table>
	
	</div>	 
	</c:if> 
	
	<cart:cartRestoration/>
	<cart:cartValidation/>
	
	<input name="isOrderBlocked" type="hidden" id="isOrderBlocked" value="${cartData.isOrderBlocked }"><br/>	

	
	<cms:pageSlot position="TopContent" var="feature" element="div" class="span-24">
		<cms:component component="${feature}"/>
	</cms:pageSlot>
	
	<c:if test="${not empty cartData.entries}">
		<spring:url value="${continueUrl}" var="continueShoppingUrl" htmlEscape="true"/>
		
		 <!--  Adding container optimization button and 2 radio buttons -->
            
		<br/>                 
        <div>
               <c:if test="${enableTruck}">
		<%-- <form:form  name="truckform" >
		 
			  <label > Truck </label> 
			   <form:select id="containerHght" name="containerHght" path="containerHeight" onChange="getPackingOptionChange(this);">
                      <form:options items="${containerHeightList}" selected='' />
                </form:select>
					
				 &nbsp;&nbsp;<label ><spring:theme code="packing.type" /></label>
				 <form:select id="packingTypeForm"  path="packingType" disabled="${isEmeaUser}">
				        <form:options id="packingOptionsId" items="${packingOptionList}" />
   			     </form:select>
				
			  <!-- <button id ="container_Optamization"  style="position:absolute;left:900px;" class="positive"  type="submit"  ><spring:theme code="basket.your.shopping.container.optimization" /></button> --> 
					<br>
					<div class="form-actions">
					<ycommerce:testId code="update_button" >
						<button class="positive" type="submit" style="margin-top:20px;font-size: 130%;"  >
							<spring:theme code="basket.your.shopping.container.optimization" />
						</button>
					</ycommerce:testId>
				      </div>
					</form:form> --%>
					
					<!-- Truck information -->
				</c:if>
                 
              <%--  <c:if test="${enableButton}"> --%>
               <c:if test="${enableButton}">
			<form:form  name="containerform" action="cart" id="containeroptimization" method="post" modelAttribute ="containerUtilizationForm" >
		 		
			 	<c:choose>
			 		<c:when test="${siteUid eq 'personalCareEMEA' and freightType eq 'Container'}">
			 			<label ><spring:theme code="container.height"/></label> 
					  <%-- <form:select id="containerHght" disabled="${isEmeaUser}" name="containerHght" path="containerHeight" onChange="getPackingOptionChange(this);"> --%>
					   <form:select id="containerHght" name="containerHght" path="containerHeight" onChange="getPackingOptionChange(this);">
		                      <form:options items="${containerHeightList}" selected='' />
		                </form:select>
		          <!--		WeSell Implementation -  Added Code Changes for container optimization for Sales Rep login - by Venkat -->
			 		</c:when>
			 		<c:when test="${siteUid eq 'personalCare' and isSalesRepUserLogin eq 'false'}">
			<!--		WeSell Implementation -  Added Code Changes for container optimization for Sales Rep login - by Venkat -->
			 		<!--  Always freight Type is 'Container' for LATAM -->
			 			<label ><spring:theme code="container.height"/></label> 
					  <%-- <form:select id="containerHght" disabled="${isEmeaUser}" name="containerHght" path="containerHeight" onChange="getPackingOptionChange(this);"> --%>
					   <form:select id="containerHght" name="containerHght" path="containerHeight" onChange="getPackingOptionChange(this);">
		                      <form:options items="${containerHeightList}" selected='' />
		                </form:select>
			 		</c:when>
			 		<c:otherwise>
			 		</c:otherwise>
			 	</c:choose>
			<!--		WeSell Implementation -  Added Code Changes for container optimization for Sales Rep login - by Venkat -->	
				<c:choose>
				
					<c:when test="${siteUid eq 'personalCare' and isSalesRepUserLogin eq 'false'}">
					<!--		WeSell Implementation -  Added Code Changes for container optimization for Sales Rep login - by Venkat -->
						&nbsp;&nbsp;<label ><spring:theme code="packing.type" /></label>
						 <%-- <form:select id="packingTypeForm"  path="packingType" disabled="${isEmeaUser}"> --%>
						  <form:select id="packingTypeForm" path="packingType">
						        <form:options id="packingOptionsId" items="${packingOptionList}" />
		   			     </form:select>
					</c:when>
					<c:otherwise>
						&nbsp;&nbsp;<label style="display:none;"><spring:theme code="packing.type" /></label>
					 <%-- <form:select id="packingTypeForm"  path="packingType" disabled="${isEmeaUser}"> --%>
					  <form:select id="packingTypeForm" path="packingType" style="display:none;">
					        <form:options id="packingOptionsId" items="${packingOptionList}" />
	   			     </form:select>
					</c:otherwise>
				</c:choose>
					 
				  
			  <!-- <button id ="container_Optamization"  style="position:absolute;left:900px;" class="positive"  type="submit"  ><spring:theme code="basket.your.shopping.container.optimization" /></button> -->
			 	<c:choose>
			 		<c:when test="${siteUid eq 'personalCare' }">
			 			<br>
						<div class="form-actions">
							<ycommerce:testId code="update_button" >
								<button class="positive" type="submit" style="margin-top:20px;font-size: 130%;"  >
									<spring:theme code="basket.your.shopping.container.optimization" />
								</button>
							</ycommerce:testId>
					     </div>
			 		</c:when>
			 		<c:when test="${siteUid eq 'personalCareEMEA' and freightType eq 'Container'}">
			 			<br>
						<div class="form-actions">
							<ycommerce:testId code="update_button" >
								<button class="positive" type="submit" style="margin-top:20px;font-size: 130%;"  >
									<spring:theme code="basket.your.shopping.container.optimization" />
								</button>
							</ycommerce:testId>
					     </div>
			 		</c:when>
			 		<c:otherwise>
			 		</c:otherwise>
			 	</c:choose>	 
					
			</form:form>
		</c:if>
	</div>
		<c:if test="${isSalesRepUserLogin and gotPriceFromSAP}" >			   
		<div>
			<table class="account-profile-data">
				<tr>
					<td><spring:theme code="basket.page.credit.limit" text="Credit Limit"/>: </td>
					<td>
					<c:if test="${not empty cartData.b2bUnit.creditLimit}">
					<c:choose>
						<c:when test="${currencyISO ne 'USD' and fn:contains(cartData.b2bUnit.creditLimit, currencyISO)}">
							<c:set var="creditLimitValue" value="${fn:replace(cartData.b2bUnit.creditLimit, currencyISO, '')}" />
							<c:set var="currencyIsoCreditLimitValue" value="${currencyISO}&nbsp;${creditLimitValue}" />
							 ${currencyIsoCreditLimitValue}
						</c:when>
						<c:otherwise>
							<c:set var="creditLimitValue" value="${fn:replace(cartData.b2bUnit.creditLimit, '$', '')}" />
							<c:set var="currencyIsoCreditLimitValue" value="${currencyISO}&nbsp;${creditLimitValue}" />
							 	${currencyIsoCreditLimitValue}
						</c:otherwise>
						</c:choose>
						</c:if>
					</td>
				</tr>
				<tr>
					<td><spring:theme code="basket.page.customer.balance" text="Customer Balance"/>: </td>
					<td>
					<c:if test="${not empty cartData.b2bUnit.customerBalance}">
						<c:choose>
						<c:when test="${currencyISO ne 'USD' and fn:contains(cartData.b2bUnit.customerBalance, currencyISO)}">
							<c:set var="customerBalanceValue" value="${fn:replace(cartData.b2bUnit.customerBalance, currencyISO, '')}" />
							<c:set var="currencyIsoCustomerBalance" value="${currencyISO}&nbsp;${customerBalanceValue}" />
							 ${currencyIsoCustomerBalance}
						</c:when>
						<c:otherwise>
							<c:set var="customerBalanceValue" value="${fn:replace(cartData.b2bUnit.customerBalance, '$', '')}" />
							<c:set var="currencyIsoCustomerBalance" value="${currencyISO}&nbsp;${customerBalanceValue}" />
							 ${currencyIsoCustomerBalance}
						</c:otherwise>
						</c:choose>
					</c:if>
					</td>
				</tr>
				<tr>
					<td><spring:theme code="basket.page.credit.available" text="Credit Available"/>: </td>
					<td>
					<c:if test="${not empty cartData.b2bUnit.creditAvailable}">
					<c:choose>
						<c:when test="${currencyISO ne 'USD' and not empty cartData.b2bUnit.creditAvailable and fn:contains(cartData.b2bUnit.creditAvailable, currencyISO)}">
							<c:set var="creditAvailableValue" value="${fn:replace(cartData.b2bUnit.creditAvailable, currencyISO, '')}" />
							<c:set var="currencyIsoCreditAvailable" value="${currencyISO}&nbsp;${creditAvailableValue}" />
							 ${currencyIsoCreditAvailable}
						</c:when>
						<c:otherwise>
							<c:set var="creditAvailableValue" value="${fn:replace(cartData.b2bUnit.creditAvailable, '$', '')}" />
							<c:set var="currencyIsoCreditAvailable" value="${currencyISO}&nbsp;${creditAvailableValue}" />
							 ${currencyIsoCreditAvailable}
						</c:otherwise>
						</c:choose>
					</c:if>
					</td>
				</tr>
			</table>
		</div>
	</c:if>		
				
		<div> 		
			<!--   Start Code changes for order flag check -->
			<c:if test="${siteUid eq 'personalCare'}">
				<c:choose>
					<c:when test="${not isSalesRepUserLogin and productPriceNotUpdated }">
						<button id ="checkoutButton_top" class="checkoutButton positive right" type="button" data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
					</c:when>
					<c:when test="${cartData.isOrderBlocked} ">
						<button id ="checkoutButton_top" class="checkoutButtonRed positive right" type="button" data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.blocked.order" /></button>
					</c:when>
					<c:when test="${ not empty cartData.productsNotAddedToCart}">
						<button id ="checkoutButton_top" class="checkoutButton positive right" type="button" data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
					</c:when>
					<c:when test="${cartData.isFloorSpaceFull}">
						<button id ="checkoutButton_top" class="checkoutButton positive right" type="button" data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
					</c:when>
					<c:when test="${cartData.errorMessage}">
						<button id ="checkoutButton_top" class="checkoutButton positive right" type="button"  data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
					</c:when>
					<c:when test="${not gotPriceFromSAP and isSalesRepUserLogin}">
						<button id ="checkoutButton_top" class="checkoutButton positive right" type="button" data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
					</c:when>
					<c:when test="${gotPriceFromSAP and isSalesRepUserLogin and cmirNotAvailable}">
						<button id ="checkoutButton_top" class="checkoutButton positive right" type="button" data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
					</c:when>
					<c:when test="${not empty entryPriceIsZero and isSalesRepUserLogin}">
						<button id ="checkoutButton_top" class="checkoutButton positive right" type="button" data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
					</c:when>
					<c:otherwise>
						<button id ="checkoutButton_top" class="checkoutButton positive right" type="button" data-checkout-url="${checkoutUrl}"><spring:theme code="checkout.checkout" /></button>
					</c:otherwise>
				</c:choose>
			</c:if>
			<c:if test="${siteUid eq 'personalCareEMEA'}">
				<c:choose>
					<c:when test="${cartData.isOrderBlocked} ">
						<button id ="checkoutButton_top" class="checkoutButtonRed positive right" type="button" data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.blocked.order" /></button>
					</c:when>
					<c:when test="${ not empty cartData.productsNotAddedToCart}">
						<button id ="checkoutButton_top" class="checkoutButton positive right" type="button"  data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
					</c:when>
					<c:when test="${cartData.isFloorSpaceFull}">
						<button id ="checkoutButton_top" class="checkoutButton positive right" type="button"  data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
					</c:when>
					<c:when test="${cartData.errorMessage}">
						<button id ="checkoutButton_top" class="checkoutButton positive right" type="button"  data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
					</c:when>
					<c:otherwise>
						<button id ="checkoutButton_top" class="checkoutButton positive right" type="button" data-checkout-url="${checkoutUrl}"><spring:theme code="checkout.checkout" /></button>
					</c:otherwise>
				</c:choose>
			</c:if>
			
			
			
	
			<c:if test="${isSalesRepUserLogin}">
				 <form:form action="/cart/getPrice" method="get">
				 	<c:choose>
				 		<c:when test="${cartEntriesSizeExceed}">
				 			<button id="getPriceButton" class="checkoutButton positive right" style="font-size: 140%;" type="submit" disabled="disabled"><spring:theme code="basket.your.shopping.getPrice" /></button>
				 		</c:when>
				 		<c:otherwise>
				 			<button id="getPriceButton" class="checkoutButton positive right" style="font-size: 140%;" type="submit" ><spring:theme code="basket.your.shopping.getPrice" /></button>
				 		</c:otherwise>
				 	</c:choose>
	         		 
				 </form:form>
				 
				 <form:form action="/downloadCart"  method="get">
         		 <button id="downloadCartButton" class="checkoutButton positive right" style="font-size: 140%;" type="submit" ><spring:theme code="basket.your.shopping.downloadCart" /></button>
			</form:form>
			</c:if>
			
			<form:form action="/cart/clearCart"  method="get">
         		 <button id="clearCartButton" class="checkoutButton positive right" style="font-size: 140%;" type="submit" ><spring:theme code="basket.your.shopping.clearCart" /></button>
			</form:form>
		</div>	
				<!--   Start Code changes for order flag check -->	
				
					<cart:cartItems cartData="${cartData}" />
			
			<div class="clearfix fixthis_row_cls">
					<div class="span-16">
					
					<!-- Display Volume & Weight Container utilization graphics for LATAM and EMEA if enableContainerOptimization is enabled - START -->
						<%-- <c:if test="${not enableTruck or siteUid eq 'personalCare'}"> --%>
						<!--  enableForB2BUnit is a flag for EMEA  -->
						<c:if test="${enableForB2BUnit}">
						<!--		WeSell Implementation -  Added Code Changes for container optimization for Sales Rep login - by Venkat -->
						<c:if test="${isSalesRepUserLogin eq 'false'}">
				<!--		WeSell Implementation -  Added Code Changes for container optimization for Sales Rep login - by Venkat -->
							<div class="cntutil_wrapper_cls">
					                  	<div id="volume_cont">
					                  		<%-- <c:if test="${siteUid eq 'personalCare'}" > --%>
					                  		<c:if test="${freightType eq 'Container'}">
												<div class="divider_20">
													<span id="containerHeightLine" class="span_cls">
														<c:if test="${not empty cartData.containerHeight and cartData.containerHeight ne null and freightType eq 'Container'}">
															${cartData.containerHeight}
														</c:if>
													</span>		
												</div> 
										   	</c:if>
					                       	<div class="cnt_utlvolfill_cls">
						                       	<span id="utl_vol">
							                       	<c:if test="${not empty cartData.availableVolume}">
							                       		${cartData.availableVolume}
							                       	</c:if>
						                       	</span>
					                       		<span id="volumePercentageSign">%</span>
					                       		<c:if test="${cartData.availableVolume >= 0}">
					                       			<span>Available</span>
					                       		</c:if>
					                       	</div>
					                    	<div class="cnt_utllbl_cls"><spring:theme code="basket.your.shopping.container.utilization.volume"/></div>
					                        <div style="height: 1px;" id="volume_utilization"></div>
					                    </div>                                                   
								
										<div id="weight_cont">
						                       <div class="cnt_utlwilfill_cls">
						                       	<span id="utl_wt">
						                       		<c:if test="${not empty cartData.availableWeight}">
						                       			${cartData.availableWeight}
						                       		</c:if>
						                       	</span>
						                       	<span id="weightPercentageSign">%</span>
						                       	<c:if test="${cartData.availableWeight >= 0}">
						                       		<span>Available</span>
						                       	</c:if>
						                       	</div>
						                       <div class="cnt_utllbl_cls"><spring:theme code="basket.your.shopping.container.utilization.weight"/></div>                                                                             
						       				   <div style="height: 1px;" id="weight_utilization"></div>
					                    </div>
							</div>
							</c:if>	
							<div class="clearfix"><!-- --></div>
							
						</c:if>
						<!-- Display Container utilization graphics for LATAM and EMEA if enableContainerOptimization is enabled - END -->
						<div>
							<cart:cartPromotions cartData="${cartData}"/>
							
							<cart:cartPotentialPromotions cartData="${cartData}"/>
							&nbsp;
						</div>
					</div>
					<div class="span-8 last">
					 	<cart:ajaxCartTotals/>
					 	<c:choose>
					 		<c:when test="${isSalesRepUserLogin}">
					 			<cart:cartTotals cartData="${cartData}" showTaxEstimate="true" showTax="false"/> 
					 		</c:when>
					 		<c:otherwise>
					 			<cart:cartTotals cartData="${cartData}" showTaxEstimate="true"/>
					 		</c:otherwise>
					 	</c:choose>
					</div>
				</div>
			
				<!-- Display Container utilization Floorplan graphics only for LATAM , not for EMEA as of now - START -->
				<%-- <c:set var="enableButton" value="true" /> --%>
				<%-- ${cartData.floorSpaceCount}
				${cartData.floorSpaceProductsMap}
				${cartData.nonPalletFloorSpaceProductsMap}
				${cartData.isFloorSpaceFull}
				${cartData.isContainerFull} --%>
				<%-- <input type="hidden" id="freightType" name="freightType" value="${cartData.freightType}" />
				<input type="hidden" id="palletType" name="palletType" value="${cartData.palletType}" /> --%>
				
				<!--  enableButton is a flag for LATAM -->
				<c:if test="${enableButton}">
					<div class="clearfix fixthis_row_cls">
						<div class="span-16">
							<div class="cntutil_wrapper_cls">
								
						         <c:if test="${not empty cartData.enableFloorSpaceGraphics and cartData.enableFloorSpaceGraphics}">
						         <div id="floorSpace_cont"> 
						         	<%-- <c:choose>
						         		<c:when test="${siteUid eq 'personalCare'}">
						         		</c:when>
						         		<c:otherwise>
						         			<c:set var="graphicsWidth" value="${cartData.floorSpaceCount * 31.6}" />
						         			<div id="floorSpace_cont" style="width: ${graphicsWidth}">
						         		</c:otherwise>
						         	</c:choose>	 --%>
						         		<c:if test="${siteUid eq 'personalCare' or freightType eq 'Container'}" >
											<div class="divider_40">
												<span id="containerHeightLine" class="span_cls">
													<c:if test="${not empty cartData.containerHeight and cartData.containerHeight ne null}">
														${cartData.containerHeight}
													</c:if>
												</span>
											</div>
										</c:if>  
				                	   	<div class="cnt_utlvolfill_cls" id="floorSpaceCount" style="display:none;">
				                	   		<span id="utl_vol">
				                	   			<c:if test="${not empty cartData.floorSpaceCount}">
				                	   				${cartData.floorSpaceCount}
				                	   			</c:if>
				                	   		</span>
				                	   	</div>
				                	   	<div id="cnt_floorSpaceProducts" style="display:none">
				                	   		<span id="utl_vol">
				                	   			<c:if test="${not empty cartData.floorSpaceProductsMap and cartData.floorSpaceProductsMap ne null}">
				                	   				${cartData.floorSpaceProductsMap}
				                	   			</c:if>
				                	   		</span>
				                	   	</div>
				                	   	<div id="cnt_nonPalletFloorSpaceProducts" style="display:none">
				                	   		<span id="utl_vol">
				                	   			<c:if test="${not empty cartData.nonPalletFloorSpaceProductsMap and cartData.nonPalletFloorSpaceProductsMap ne null}">
				                	   				${cartData.nonPalletFloorSpaceProductsMap}
				                	   			</c:if>
				                	   		</span>
				                	   	</div>
				                	   	<!-- <div class="clearfix"> -->
				                	   	
				                    	<div id="floorSpaceFull" style="display:none">
				                    		<span id="floor_Space_Full">
				                    			<c:if test="${not empty cartData.isFloorSpaceFull}">
				                    				${cartData.isFloorSpaceFull}
				                    			</c:if>	
				                    		</span>
				                    	</div>
				                    	
			                    		<div class="cnt_fs_utllbl_cls"><spring:theme code="basket.your.shopping.container.utilization.floorSpace"/></div>
			                    	
				                    	<div style="margin-top: 10px;">
					                    	<table>
						                    	<tr>
							                    	<td>
							                    		<div id="colorBox1" style="float: left;width: 20px;height: 20px;margin: 5px;border: 1px solid rgba(0, 0, 0, .2);background: #33cc33;"></div>
							                    	</td>
							                    	<td><spring:theme code="box.pallets"/></div></td>
							                    	<td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td>
							                    	<td><div id="colorBox2" style="float: left;width: 20px;height: 20px;margin: 5px;border: 1px solid rgba(0, 0, 0, .2);background: #87CEEB;"></div></td>
							                    	<td><spring:theme code="box.nonpallets"/></td>
							                    	<td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td>
							                    	<td><div id="colorBox2" style="float: left;width: 20px;height: 20px;margin: 5px;border: 1px solid rgba(0, 0, 0, .2);background: #ffd799;"></div></td>
							                    	<td><spring:theme code="box.emptySpace" text="Empty Space"/></td>
						                    	</tr>
					                    	</table>
				                    	</div>
				                    	
			                      </div>   
						 		 </c:if>
							</div>
						</div>
						
						<%-- <c:if test="${not empty cartData.enableFloorSpaceGraphics  and cartData.enableFloorSpaceGraphics }"> --%>
							
							<c:choose>
								<c:when test="${not empty cartData.isFloorSpaceFull  and cartData.isFloorSpaceFull}">
									<div id="palletsCountInfo" style="text-align: center;color: red; "><br/><br/>
								</c:when>
								<c:otherwise>
									<div id="palletsCountInfo" style="text-align: center;color: blue; "><br/><br/>
								</c:otherwise>
							</c:choose>
							
		                   <!-- <div style="text-align: center;color: blue; "><br/><br/> -->
		                   
		                      <b><spring:theme code="text.account.orderHistory.fullPallets"/>&nbsp;:&nbsp;
                                    <c:if test="${not empty cartData.totalPalletCount }">
                                         <span id="totalPalletCount">${cartData.totalPalletCount}</span>
                                    </c:if></b><br/><br/>
                                    
                                  <b><spring:theme code="text.account.orderHistory.partialPallets"/>&nbsp;:&nbsp;
                                      <c:if test="${not empty cartData.partialPalletCount }">
                                          <span id="partialPalletCount">${cartData.partialPalletCount}</span>
                                     </c:if></b>
                               </div> 
                         <%-- </c:if> --%>
					</div>
				
				
					<div class="clearfix fixthis_row_cls">
						<c:if test="${not empty cartData.enableFloorSpaceGraphics and not cartData.enableFloorSpaceGraphics}"> 	
							<div style="font-weight: bold;font-size: 1.2em;">
								<span style="padding-left:127px">
									<c:choose>
										<c:when test="${freightType eq 'Truck'}">
											<spring:theme code="basket.your.shopping.truck.utilization" />
										</c:when>
										<c:otherwise>
											<spring:theme code="basket.your.shopping.container.utilization"/>
										</c:otherwise>
									</c:choose>
									<%-- <spring:theme code="basket.your.shopping.container.utilization"/> --%>
									 </span>
									<div align="left" style="font-size:11px;color: blue; ">
										<spring:theme code="basket.your.shopping.container.utilization1"/>
									</div>
							</div>
						</c:if>
						<c:if test="${not empty cartData.enableFloorSpaceGraphics and cartData.enableFloorSpaceGraphics}"> 	
							<div style="padding-top:30px;font-weight: bold;font-size: 1.2em;">
								<span style="padding-left:248px">
									<c:choose>
										<c:when test="${freightType eq 'Truck'}">
											<spring:theme code="basket.your.shopping.truck.utilization" />
											<c:if test="${siteUid eq 'personalCareEMEA'}">
												<c:out value=" in ${palletType} pallets" />
											</c:if>
										</c:when>
										<c:otherwise>
											<spring:theme code="basket.your.shopping.container.utilization"/>
											<c:if test="${siteUid eq 'personalCareEMEA'}">
												<c:out value=" in ${palletType} pallets" />
											</c:if>
										</c:otherwise>
									</c:choose>
									<%-- <spring:theme code="basket.your.shopping.container.utilization"/> --%>
									</span>
									<div align="left" style="font-size:11px;color: blue; ">
										<spring:theme code="basket.your.shopping.container.utilization1"/>
									</div>
							</div>
						</c:if>
					</div>
				</c:if>
			<!-- Display Container utilization graphics only from LATAM , not for EMEA  - END -->	
		
			<!--   Start Code changes for order flag check  for continueShop button -->
			<c:choose>
				<c:when test="${not empty cartData.isOrderBlocked  and cartData.isOrderBlocked }">
					<!-- a class="button continueShop-button" disabled="disabled" style="height: 30px;padding-top: 8px;font-size: 140%;margin-top: 20px;border-color: #169e08;" href="${continueShoppingUrl}"><spring:theme text="Continue Shopping" code="cart.page.continue"/></a-->
					<button id ="continueButton_bottom" class="checkoutButton positive left" type="button" data-checkout-url="${continueShoppingUrl}" disabled="disabled"><spring:theme text="Continue Shopping" code="cart.page.continue"/></button>
				</c:when>
				<c:when test="${not empty cartData.isFloorSpaceFull and cartData.isFloorSpaceFull}">
				     <button id ="continueButton_bottom" class="checkoutButton positive left" type="button" data-checkout-url="${continueShoppingUrl}" disabled="disabled"><spring:theme text="Continue Shopping" code="cart.page.continue"/></button>
				</c:when>
				<c:otherwise>
					<!-- a class="button continueShop-button"  style="height: 30px;padding-top: 8px;font-size: 140%;margin-top: 20px;border-color: #169e08;" href="${continueShoppingUrl}"><spring:theme text="Continue Shopping" code="cart.page.continue"/></a>-->
					<button id ="continueButton_bottom" class="checkoutButton positive left" type="button" data-checkout-url="${continueShoppingUrl}" ><spring:theme text="Continue Shopping" code="cart.page.continue"/></button>
				</c:otherwise>
			</c:choose>
			<!--   End Code changes for order flag check  for continueShop button -->	
				
				
			<!--   Start Code changes for order flag check -->
			<c:if test="${siteUid eq 'personalCare'}">
				<c:choose>
					<c:when test="${not isSalesRepUserLogin and productPriceNotUpdated }">
						<button id ="checkoutButton_bottom" class="checkoutButton positive right" type="button" data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
					</c:when>
					<c:when test="${not empty cartData.isOrderBlocked  and cartData.isOrderBlocked }">
						<button id ="checkoutButton_bottom" class="checkoutButtonRed positive right" type="button" data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.blocked.order" /></button>			
					</c:when>
					<c:when test="${ not empty cartData.productsNotAddedToCart}">
						<button id ="checkoutButton_bottom" class="checkoutButton positive right" type="button" data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
					</c:when>
					<c:when test="${not empty cartData.isFloorSpaceFull and cartData.isFloorSpaceFull}">
						<button id ="checkoutButton_bottom" class="checkoutButton positive right" type="button" data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
					</c:when>
					<c:when test="${not empty cartData.errorMessage and cartData.errorMessage}">
						<button id ="checkoutButton_bottom" class="checkoutButton positive right" type="button" data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
					</c:when>
					<c:when test="${not gotPriceFromSAP and isSalesRepUserLogin}">
						<button id ="checkoutButton_bottom" class="checkoutButton positive right" type="button" data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
					</c:when>
					<c:when test="${gotPriceFromSAP and isSalesRepUserLogin and cmirNotAvailable}">
						<button id ="checkoutButton_bottom" class="checkoutButton positive right" type="button" data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
					</c:when>
					<c:when test="${not empty entryPriceIsZero and isSalesRepUserLogin}">
						<button id ="checkoutButton_bottom" class="checkoutButton positive right" type="button" data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
					 </c:when>
					<c:otherwise>
						<button id ="checkoutButton_bottom" class="checkoutButton positive right" type="button" data-checkout-url="${checkoutUrl}"><spring:theme code="checkout.checkout" /></button>
					</c:otherwise>
				</c:choose>
			</c:if>
			<c:if test="${siteUid eq 'personalCareEMEA'}">
				<c:choose>
					<c:when test="${not empty cartData.isOrderBlocked  and cartData.isOrderBlocked }">
						<button id ="checkoutButton_bottom" class="checkoutButtonRed positive right" type="button" data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.blocked.order" /></button>			
					</c:when>
					<c:when test="${ not empty cartData.productsNotAddedToCart}">
						<button id ="checkoutButton_bottom" class="checkoutButton positive right" type="button"  data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
					</c:when>
					<c:when test="${not empty cartData.isFloorSpaceFull and cartData.isFloorSpaceFull}">
						<button id ="checkoutButton_bottom" class="checkoutButton positive right" type="button"  data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
					</c:when>
					<c:when test="${not empty cartData.errorMessage and cartData.errorMessage}">
						<button id ="checkoutButton_bottom" class="checkoutButton positive right" type="button"  data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
					</c:when>
					<c:otherwise>
						<button id ="checkoutButton_bottom" class="checkoutButton positive right" type="button" data-checkout-url="${checkoutUrl}"><spring:theme code="checkout.checkout" /></button>
					</c:otherwise>
				</c:choose>
			</c:if>
			
			<!--   Start Code changes for order flag check -->	
				
		
	</c:if>
	
	<c:if test="${empty cartData.entries}">
		<div class="span-24">
			<div class="span-24 wide-content-slot cms_disp-img_slot">
				<cms:pageSlot position="MiddleContent" var="feature" element="div">
					<cms:component component="${feature}"/>
				</cms:pageSlot>

				<cms:pageSlot position="BottomContent" var="feature" element="div">
					<cms:component component="${feature}"/>
				</cms:pageSlot>
			</div>
		</div>
	</c:if>

	<c:if test="${not empty cartData.entries}" >
		<cms:pageSlot position="Suggestions" var="feature" element="div" class="span-24">
			<cms:component component="${feature}"/>
		</cms:pageSlot>
	</c:if>
	
	<cms:pageSlot position="BottomContent" var="feature" element="div" class="span-24">
		<cms:component component="${feature}"/>
	</cms:pageSlot>
</template:page>
