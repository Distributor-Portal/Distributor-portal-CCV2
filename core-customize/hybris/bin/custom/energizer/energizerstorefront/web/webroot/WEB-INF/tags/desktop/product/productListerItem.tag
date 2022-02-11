<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="product" required="true" type="de.hybris.platform.commercefacades.product.data.ProductData" %>
<%@ attribute name="isOrderForm" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/desktop/product" %>
<%@ taglib prefix="cart" tagdir="/WEB-INF/tags/desktop/cart" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

 <form name="selecteduomform_${product.code }" id="selectedUomForm_${product.code }" method="post" action="/validateUOM">
			<input type="hidden" id="productCodePLP_${product.code }" name="productCode" value="" />
			<input type="hidden" id="selectedUomPLP_${product.code }" name="uom" value="" />
</form>

<form name="validatePBGForm_${product.code}" id="validatePBGForm_${product.code}" method="post" action="/validatePBG">
	<input type="hidden" id="productCode_${product.code }" name="productCode" value="" />
	<input type="hidden" id="isPBGPLP_${product.code}" name="isPBG" value="" />
</form>

<input type="hidden" id="siteIdInPLP" value="${siteUid}" /> 

<spring:theme code="text.addToCart" var="addToCartText"/>
<c:url var="productUrl" value="${product.multidimensional and not empty product.firstVariantUrl ? product.firstVariantUrl : product.url}" />

<c:set value="${not empty product.potentialPromotions}" var="hasPromotion"/>

<div class="productListItem${hasPromotion ? ' productListItemPromotion' : ''}">
    <ycommerce:testId code="test_searchPage_wholeProduct">
        <a href="${productUrl}" title="${fn:escapeXml(product.name)}" class="productMainLink">

 <c:if test="${not isSalesRepUserLogin}">
        <div class="thumb">
            <product:productPrimaryImage product="${product}" format="thumbnail"/>
            <c:if test="${not empty product.potentialPromotions and not empty product.potentialPromotions[0].productBanner}">
                <img class="promo" src="${product.potentialPromotions[0].productBanner.url}" alt="${product.potentialPromotions[0].description}" title="${product.potentialPromotions[0].description}"/>
            </c:if>
        </div>
        </c:if>
		
        <ycommerce:testId code="searchPage_productName_link_${product.code}">
            <div class="head">
          <!-- <c:out value="${fn:escapeXml(product.name)}" /> -->
           <c:out value="${product.name}" escapeXml="true"/>
           <c:if test="${not isSalesRepUserLogin}">
            	<c:choose>
					<c:when test="${ empty product.customerProductPrice }">
				        <ycommerce:testId code="searchPage_price_label_${product.code}">
				            <div class="price"><product:productListerItemPrice product="${product}" />
				            	<c:if test="${siteUid eq 'personalCare'}">
									<c:choose>
						            	<c:when test="${empty product.priceUOM}">
						            		/EA
						            	</c:when>
						            	<c:otherwise>
						            		/${product.priceUOM}
						            	</c:otherwise>
					            	</c:choose>
								</c:if>
							</div>
				        </ycommerce:testId>
					</c:when>
					<c:otherwise>
						 <%--   Energizer customer Price --%> 
						 <p class="big-price right" style="font-weight: normal;color: #0b4f04;">
							 ${product.customerPriceCurrency} ${product.customerProductPrice} 
							 	<c:if test="${siteUid eq 'personalCare'}">
									<c:choose>
						            	<c:when test="${empty product.priceUOM}">
						            		/EA
						            	</c:when>
						            	<c:otherwise>
						            		/${product.priceUOM}
						            	</c:otherwise>
					            	</c:choose>
								</c:if>
						</p> 
					 </c:otherwise>
				</c:choose>
				</c:if>
				
				<c:if test="${siteUid eq 'personalCare'}">
					<input type="hidden" id="isPBG_${product.code}" value="${product.isPBG}" />
					
					<c:if test="${product.isPBG}">
						<div class="pbgWrapper" title="This is a PBG Product">
							<span class="pbgLabel">PBG</span>
						</div>
					</c:if>
				</c:if>
            </div>
        </ycommerce:testId>
        
        </a>
        
        <c:if test="${not empty product.averageRating}">
            <product:productStars rating="${product.averageRating}" />
        </c:if>
        <div class="details">
	        <c:if test="${not empty product.summary}">
	            ${fn:escapeXml(product.summary)}	            
	        </c:if>
            <br/>            
            <spring:theme code="basket.page.MaterialId"/> : ${fn:escapeXml(product.code)}<br>
        	<spring:theme code="basket.page.customerMaterialId"/> : ${fn:escapeXml(product.customerMaterialId)}<br>
        	<c:choose>
	        	<c:when test="${siteUid eq 'personalCare'}">
	        		<spring:theme code="basket.page.customerProductName"/>  : ${fn:escapeXml(product.customerProductName)}<br>
	        	</c:when>
	        	<c:otherwise>
	        		<c:set value="${fn:trim(product.customerProductName)}" var="customerProductName" />
	        		<c:choose>
		        		<c:when test="${not empty product.customerProductName and (fn:length(customerProductName) gt 0)}">
		        			<spring:theme code="basket.page.customerProductName"/>  : ${fn:escapeXml(product.customerProductName)}<br>
		        		</c:when>
		        		<c:otherwise>
		        		</c:otherwise>
	        		</c:choose>
	        	</c:otherwise>
        	</c:choose>
        	<c:if test="${not empty product.ean}">
        		<spring:theme code="basket.page.ean"/> : ${fn:escapeXml(product.ean)}<br>
        	</c:if>
        	<spring:theme code="basket.page.shipFrom"/> : ${fn:escapeXml( product.shippingPointName)}  <br>
			<c:if test="${siteUid ne 'personalCare' and not isSalesRepUserLogin}">
			<spring:theme code="basket.page.shipFromLocation"/>:${fn:escapeXml(product.shippingPointLocation)}<br>
				<spring:theme code="basket.page.moq"/> : ${fn:escapeXml(product.moq)}<br>
				<spring:theme code="basket.page.uom"/> : ${fn:escapeXml(product.uom)}<br>
			</c:if>
			<c:if test="${not isSalesRepUserLogin and not product.isWeSellProduct}">
				<spring:theme code="basket.page.layersPerPallet"/> : ${fn:escapeXml(product.numberOfLayersPerPallet)}<br>
	            <spring:theme code="basket.page.casesPerPallet"/> : ${fn:escapeXml(product.numberOfCasesPerPallet)}<br> 
				<spring:theme code="basket.page.casesPerLayer"/> : ${fn:escapeXml(product.numberOfCasesPerLayer)}<br>
			</c:if>	
			<c:if test="${siteUid eq 'personalCareEMEA' and not empty product.numberOfCUsPerSU and not empty product.numberOfSUsPerPallet}">
				<spring:theme code="basket.page.CUsPerSU"/> : ${fn:escapeXml(product.numberOfCUsPerSU)}<br>
				<spring:theme code="basket.page.SUsPerPallet"/> : ${fn:escapeXml(product.numberOfSUsPerPallet)}<br>		
			</c:if>	
																		
			<%-- obsolete : ${product.obsolete}<br> --%>
		</div>			
        
        <div style="color: red;margin-top: 25px;padding-left: 110px;margin-bottom: -38px;" class="errormsgclass" id="error${product.code}"></div>
        
        <product:productListerClassifications product="${product}"/>

        <ycommerce:testId code="searchPage_addToCart_button_${product.code}">
            <c:set var="buttonType">submit</c:set>
            <c:choose>
                <c:when test="${product.stock.stockLevelStatus.code eq 'outOfStock' }">
                    <c:set var="buttonType">button</c:set>
                    <spring:theme code="text.addToCart.outOfStock" var="addToCartText"/>
                </c:when>
            </c:choose>

            
            <div class="cart clearfix">  
            	<div class="moq_error">          	
            		<c:if test="${(product.moq == 0) || (empty product.moq)}">
	               		<spring:theme code="basket.page.moq.notexists"/>
	               	</c:if>
	           </div>    	
	               	<br/> 
	            <div class="obsolete_error">   	
            	   	<c:if test="${product.obsolete}">            	   		
            	   		<spring:theme code="basket.page.obsolete.error"/>
	               	</c:if>  	
            	</div>
                <c:choose>
                    <%-- Verify if products is a multidimensional product --%>
                    <c:when test="${product.multidimensional}">
                        <c:choose>
                            <c:when test="${not empty product.firstVariantUrl}">
                                <c:url var="backToProductUrl" value="${product.firstVariantUrl}" />
                                <c:url var="productOrderFormUrl" value="${product.firstVariantUrl}/orderForm"/>
                            </c:when>
                            <c:otherwise>
                                <c:url var="backToProductUrl" value="${productUrl}" />
                                <c:url var="productOrderFormUrl" value="${product.url}/orderForm"/>
                            </c:otherwise>
                        </c:choose>
                        <a href="${backToProductUrl}" class="button right" ><spring:theme code="product.view.details" /></a>


                        <%-- <sec:authorize ifAnyGranted="ROLE_CUSTOMERGROUP"> --%>
                        <sec:authorize access="hasAnyRole('ROLE_CUSTOMERGROUP')">
                            <a href="${productOrderFormUrl}"  class="button right"><spring:theme code="order.form" /></a>

                        </sec:authorize>

                    </c:when>
                    <c:otherwise>
                        <c:set var="buttonType">submit</c:set>                                           
                        
                        <c:if test="${product.stock.stockLevelStatus.code eq 'outOfStock' }">
                            <c:set var="buttonType">button</c:set>
                            <spring:theme code="text.addToCart.outOfStock" var="addToCartText"/>
                        </c:if>
                        <!-- Added Code changes for WeSell Implementation - START -->
                       <c:if test="${not isB2BViewergroup}">
                       <!-- Added Code changes for WeSell Implementation - END -->
                       <sec:authorize  access="hasAnyRole('ROLE_B2BCUSTOMERGROUP','ROLE_B2BADMINGROUP','ROLE_EMPLOYEEGROUP')">
                        <c:if test="${empty isOrderForm || not isOrderForm}">
                            <form id="addToCartForm${product.code}" action_data="<c:url value="/cart/add"/>" method="post" class="add_to_cart_form">
								<label for="qtyInput"><spring:theme code="basket.page.quantity"/></label>
								<input type="hidden" id="selectedUom_${product.code}" name="UOM" value="" />
								<!-- Added for Ordering quantity limit to 7 digits for EMEA - START -->
								<c:choose>
									<c:when test="${siteUid eq 'personalCareEMEA'}">
										<input type="text" maxlength="7" size="3" id="qty" name="qty" class="qty" value="${fn:escapeXml(product.moq)}"> ${fn:escapeXml(product.uom)}
									</c:when>
									<c:otherwise>
										<input type="text" maxlength="5" style="width:32%;"  id="qty" name="qty" class="qty" value="${fn:escapeXml(product.moq)}">
									<div class="uomOuter">
	 									<label>UOM</label>
	 									<c:choose>
	 										<c:when test="${not isSalesRepUserLogin and not product.isWeSellProduct}">
	 											<select class="uomSelector" id="uomSelector_${product.code}" >
													<option selected="selected">Select</option>
													<option value="PAL">PAL</option>
													<option value="CS">CS</option>
													<option value="LAY">LAY</option>
												</select>	
	 										</c:when>
	 										<c:otherwise>
	 											<%-- <select class="uomSelector" id="uomSelector_${product.code}" >
													<option value="${product.uom}">${product.uom}</option>
												</select> --%>	 
												 <input type="text" maxlength="5" style="width:32%;"  id="uomSelector_${product.code}" name="uom" class="uom" value="${fn:escapeXml(product.uom)}" readonly="readonly" disabled="disabled"> 
	 										</c:otherwise>
	 									</c:choose>
									</div>
									</c:otherwise>
								</c:choose>
								<!-- Added for Ordering quantity limit to 7 digits for EMEA - END -->                           
                                <input type="hidden" name="productCodePost" value="${product.code}"/>
                                   <c:if test="${siteUid eq 'personalCare'}">
                                	 <c:set var="buttonType">button</c:set>
                                 </c:if>
                                <c:choose>
	                               <c:when test="${!product.obsolete && not empty product.uom && not empty product.customerProductPrice }">
		                                <button type="${buttonType}" id="${product.code}" class="addToCartButton  addToCartButtonPLP display_none <c:if test="${product.stock.stockLevelStatus.code eq 'outOfStock' }">out-of-stock</c:if>" 
		                                	<c:if test="${product.stock.stockLevelStatus.code eq 'outOfStock' }"> disabled="disabled" aria-disabled="true" style="display:none;"</c:if>>${addToCartText}
		                                </button>
	                                </c:when>	                                 
	                                <c:when test="${product.obsolete}">
		                                <button type="${buttonType}" class="addToCartButton addToCartButtonPLP" disabled="disabled" aria-disabled="true">
		                                	<spring:theme code="product.variants.obsolete" />
		                                </button>                                
	                                </c:when>	
	                                 <c:when test="${not empty product.uom }">
		                                <button type="${buttonType}" class="addToCartButton addToCartButtonPLP" disabled="disabled" aria-disabled="true">
		                                	<spring:theme code="product.variants.obsolete" />
		                                </button>                                
	                                </c:when>	 
	                                <c:otherwise >
		                                <button type="${buttonType}" id="${product.code}" class="addToCartButton addToCartButtonPLP display_none" disabled="disabled" aria-disabled="true">
		                                	${addToCartText}
		                                </button>                                
	                                </c:otherwise>
                                
                                </c:choose>
   
   
                                <%--  
                               <c:if test="${!product.obsolete && not empty product.uom && not empty product.customerProductPrice }">
	                                <button type="${buttonType}" class="addToCartButton <c:if test="${product.stock.stockLevelStatus.code eq 'outOfStock' }">out-of-stock</c:if>" 
	                                	<c:if test="${product.stock.stockLevelStatus.code eq 'outOfStock' }"> disabled="disabled" aria-disabled="true"</c:if>>${addToCartText}
	                                </button>
                                </c:if>
                                <c:if test="${product.obsolete && not empty product.uom }">
	                                <button type="${buttonType}" class="addToCartButton" disabled="disabled" aria-disabled="true">
	                                	<spring:theme code="product.variants.obsolete" />
	                                </button>                                
                                </c:if>
                          		<c:if test="${empty product.uom }">
	                                <button type="${buttonType}" class="addToCartButton" disabled="disabled" aria-disabled="true">
	                                	${addToCartText}
	                                </button>                                
                                </c:if>    
                                 
                                
                                
                                                            
                            </form>
                            	<%-- <div style="color: red;margin-top: 25px" class="errormsgclass" id="error${product.code}" ></div> --%>
                        </c:if>
                        </sec:authorize>
                       <!-- Added Code changes for WeSell Implementation - START -->
						</c:if>
						<!-- Added Code changes for WeSell Implementation - END --> 
                        <c:if test="${not empty isOrderForm && isOrderForm}">
                            <label for="qty"><spring:theme code="basket.page.quantity" /></label>
                            <input type=hidden id="productPrice[${sessionScope.skuIndex}]" value="${product.price.value}" />
                            <input type="hidden" class="${product.code} sku"  name="cartEntries[${sessionScope.skuIndex}].sku" id="cartEntries[${sessionScope.skuIndex}].sku" value="${product.code}" />
                            <input type="text" maxlength="3"  size="1" id="cartEntries[${sessionScope.skuIndex}].quantity" name="cartEntries[${sessionScope.skuIndex}].quantity" class="sku-quantity" value="0">

                            <c:set var="skuIndex" scope="session" value="${sessionScope.skuIndex + 1}"/>
                        </c:if>
                    </c:otherwise>
                </c:choose>

                <c:if test="${isOrderForm and !product.multidimensional}">
                    <div class="productFutureAvailability">
                        <product:productFutureAvailability product="${product}" futureStockEnabled="${futureStockEnabled}" />
                    </div>
                </c:if>

            </div>
        </ycommerce:testId>
    </ycommerce:testId>
</div>
