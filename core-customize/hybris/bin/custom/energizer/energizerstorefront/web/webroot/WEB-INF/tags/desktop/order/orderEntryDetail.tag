<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="order" required="true" type="de.hybris.platform.commercefacades.order.data.OrderData" %>
<%@ attribute name="entry" required="true" type="de.hybris.platform.commercefacades.order.data.OrderEntryData" %>
<%@ attribute name="isOrderDetailsPage" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/desktop/product" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format" %>

<c:url value="${entry.product.url}" var="productUrl"/>
<c:choose>
	<c:when test="${entry.rejectedStatus == 'Yes'}">
		<tr class="item item_order_cls show_err_cls">
	</c:when>
<c:otherwise>
	     <c:choose>
	       <c:when test="${entry.isNewEntry == 'Y' }">
	      <tr class="item item_order_cls show_prd_cls">
	       </c:when>
	       <c:otherwise>
		     <tr class="item item_order_cls">
		   </c:otherwise>
		 </c:choose>
</c:otherwise>
</c:choose>

	<td headers="header2" class="thumb">
	<c:if test="${not isSalesRepUserLogin}">
		<a class="orderImageFix" href="${productUrl}">
			<product:productPrimaryImage product="${entry.product}" format="thumbnail"/>
		</a>
	</c:if>
		<a href="${entry.product.purchasable ? productUrl : ''}">${entry.product.name}</a>
	</td>
	<td headers="header2" class="details">				
		<ycommerce:testId code="orderDetails_productName_link">
			<!--<div class="itemName"><a href="${entry.product.purchasable ? productUrl : ''}">${entry.product.name}</a></div>-->
		</ycommerce:testId>
						
		<c:forEach items="${entry.product.baseOptions}" var="option">
			<c:if test="${not empty option.selected and option.selected.url eq entry.product.url}">
				<c:forEach items="${option.selected.variantOptionQualifiers}" var="selectedOption">
					<dl>
						<dt>${selectedOption.name}:</dt>
						<dd>${selectedOption.value}</dd>
					</dl>
				</c:forEach>
			</c:if>
		</c:forEach>
  
		<c:if test="${not empty order.appliedProductPromotions}">
			<ul>
				<c:forEach items="${order.appliedProductPromotions}" var="promotion">
					<c:set var="displayed" value="false"/>
					<c:forEach items="${promotion.consumedEntries}" var="consumedEntry">
						<c:if test="${not displayed && consumedEntry.orderEntryNumber == entry.entryNumber}">
							<c:set var="displayed" value="true"/>
							<li><span>${promotion.description}</span></li>
						</c:if>
					</c:forEach>
				</c:forEach>
			</ul>
		</c:if> 
	</td>
	<td headers="header14" class="rejected">
	<ycommerce:testId code="orderDetails_MaterialId_label">${entry.product.code}</ycommerce:testId>
</td>
<td headers="header15" class="rejected">
	<c:if test="${not empty entry.customerMaterialId}">
	    <ycommerce:testId code="orderDetails_CMIRId_label">${entry.customerMaterialId}</ycommerce:testId>
     </c:if>
</td>

<c:if test="${siteUid eq 'personalCare'and  not isSalesRepUserLogin}">
	<td headers="header16" class="itemEachPrice" id="itemEachPrice_${entry.entryNumber}">
		<c:choose>
			<c:when
				test="${entry.product.multidimensional and (entry.product.priceRange.minPrice.value ne entry.product.priceRange.maxPrice.value)}">
				<format:price priceData="${entry.product.priceRange.minPrice}"
					displayFreeForZero="true" />
				-
				<format:price priceData="${entry.product.priceRange.maxPrice}"
					displayFreeForZero="true" />
			</c:when>
			<c:otherwise>
				<format:price priceData="${entry.eachUnitPrice}"
					displayFreeForZero="true" />
					<c:choose>
		            	<c:when test="${empty entry.product.priceUOM}">
		            		<c:out value="/EA" />
		            	</c:when>
		            	<c:otherwise>
		            		<c:out value="/${entry.product.priceUOM}" />
		            	</c:otherwise>
	            	</c:choose>
			</c:otherwise>
		</c:choose> 				 
	</td>
</c:if>
					
	<td headers="header4" class="quantity" >
		<ycommerce:testId code="orderDetails_productQuantity_label">${entry.quantity}</ycommerce:testId>
	</td>
	<td headers="header19" class="uom">
					 ${entry.product.uom}
	</td>
	
	<c:if test="${isSalesRepUserLogin}">
	<td headers="header21" class="unitsPeruom">
		 ${entry.product.baseUOM}
	</td> 
	</c:if> 
	
	<c:choose>		
		<c:when test="${isSalesRepUserLogin}">
			<td headers="header20" class="currency">
				${entry.basePrice.currencyIso}
				<c:set var="currencyISO" value="${entry.basePrice.currencyIso}" />
			</td>
		</c:when>
		<c:otherwise>	
		</c:otherwise>
	</c:choose>	
	
	<td headers="header5">
		<ycommerce:testId code="orderDetails_productItemPrice_label">
			<c:choose>
				<c:when test="${entry.product.multidimensional and (entry.product.priceRange.minPrice.value ne entry.product.priceRange.maxPrice.value)}">
					<format:price priceData="${entry.product.priceRange.minPrice}"
									displayFreeForZero="true" />
					  -
					<format:price priceData="${entry.product.priceRange.maxPrice}"
									displayFreeForZero="true" />
				</c:when>
			    <c:otherwise>
					<c:choose>
						<c:when test="${not isSalesRepUserLogin}">
							<format:price priceData="${entry.basePrice}" displayFreeForZero="true"/>
						</c:when>
						<c:when test="${isSalesRepUserLogin}">
							<format:price priceData="${entry.basePrice}" displayFreeForZero="true" displayOnlyValue="true" currencyISO="${currencyISO}"/>
						</c:when>
						<c:otherwise>
						</c:otherwise>
					</c:choose>
			</c:otherwise>
		 </c:choose> 
		</ycommerce:testId>
	</td>
	
	<c:if test="${siteUid ne 'personalCareEMEA' and not isSalesRepUserLogin}">
	<td headers="header6" class="unitPrice" >
		<ycommerce:testId code="orderDetails_expectedUnitPrice_label">
			<c:choose>
				<c:when test="${empty entry.expectedUnitPrice }" >					 
					${entry.expectedUnitPrice}
					<c:choose>
		            	<c:when test="${empty entry.product.priceUOM}">
		            		<c:out value="/EA" />
		            	</c:when>
		            	<c:otherwise>
		            		<c:out value="/${entry.product.priceUOM}" />
		            	</c:otherwise>
	            	</c:choose>	 			
				</c:when>
				<c:otherwise>
					${symbol}${entry.expectedUnitPrice}
					<c:choose>
		            	<c:when test="${empty entry.product.priceUOM}">
		            		<c:out value="/EA" />
		            	</c:when>
		            	<c:otherwise>
		            		<c:out value="/${entry.product.priceUOM}" />
		            	</c:otherwise>
	            	</c:choose>
				</c:otherwise>
			</c:choose>
	  	</ycommerce:testId>
	</td>
	</c:if>
	
	<td headers="header7" class="total" >
		<ycommerce:testId code="orderDetails_productTotalPrice_label">
			<c:choose>
			<c:when test="${not isSalesRepUserLogin}">
				<format:price priceData="${entry.totalPrice}" displayFreeForZero="false"/>
			</c:when>
			<c:when test="${isSalesRepUserLogin}">
				<format:price priceData="${entry.totalPrice}" displayFreeForZero="true" displayOnlyValue="true" currencyISO="${currencyISO}"/>
			</c:when>
			<c:otherwise>
			</c:otherwise>
			</c:choose>
	   </ycommerce:testId>
	</td>
	
	<c:if test="${adjustedQtyFlag > 0}">
		<c:choose>
			<c:when test="${entry.adjustedQty eq entry.quantity}">
			<td headers="header8" class="adjustedquantity">
				<ycommerce:testId code="orderDetails_productQuantity_label">
					<c:choose>
						<c:when test="${entry.rejectedStatus == 'Yes'}">
							${entry.adjustedQty}
						</c:when>
						<c:otherwise>
							<c:if test="${entry.adjustedQty > 0}">
								${entry.adjustedQty}
							</c:if>
						</c:otherwise>
					</c:choose>
				</ycommerce:testId>
			</td>
			</c:when>
			<c:otherwise>
			<td headers="header8" class="adjustedquantity textHighlight">
			<ycommerce:testId code="orderDetails_productQuantity_label">
				<c:choose>
					<c:when test="${entry.rejectedStatus == 'Yes'}">
						${entry.adjustedQty}
					</c:when>
					<c:otherwise>
						<c:if test="${entry.adjustedQty > 0}">
							${entry.adjustedQty}
						</c:if>
					</c:otherwise>
				</c:choose>
			</ycommerce:testId>
			</td>
			</c:otherwise>
		</c:choose>
	</c:if>
	
	<c:if test="${adjustedPriceFlag > 0}">
		<c:choose>
			<c:when test="${not entry.product.multidimensional or (entry.product.priceRange.minPrice.value eq entry.product.priceRange.maxPrice.value) and !(entry.adjustedItemPrice == 0)}">
			<td headers="header9" class="adjustedPrice">
				<ycommerce:testId code="orderDetails_productItemPrice_label">
					<c:choose>
						<c:when test="${not entry.product.multidimensional or (entry.product.priceRange.minPrice.value eq entry.product.priceRange.maxPrice.value)}">
							<c:choose>
								<c:when test="${entry.rejectedStatus == 'Yes'}">
									<format:price priceData="${entry.adjustedItemPrice}" displayFreeForZero="flase"/>
								</c:when>
								<c:otherwise>
									<c:if test="${entry.adjustedItemPrice.value > 0}">
										<format:price priceData="${entry.adjustedItemPrice}" displayFreeForZero="flase"/>
									</c:if>
								</c:otherwise>
							</c:choose>
						</c:when>
						<c:otherwise>
							<format:price priceData="${entry.product.priceRange.minPrice}" displayFreeForZero="false"/>
							
							<format:price priceData="${entry.product.priceRange.maxPrice}" displayFreeForZero="false"/>
						</c:otherwise>
					</c:choose>
				</ycommerce:testId>
			</td>
			</c:when>
			<c:otherwise>
				<td headers="header9" class="adjustedPrice textHighlight">
				<ycommerce:testId code="orderDetails_productItemPrice_label">
					<c:choose>
						<c:when test="${not entry.product.multidimensional or (entry.product.priceRange.minPrice.value eq entry.product.priceRange.maxPrice.value)}">
							<c:choose>
								<c:when test="${entry.rejectedStatus == 'Yes'}">
									<format:price priceData="${entry.adjustedItemPrice}" displayFreeForZero="flase"/>
								</c:when>
								<c:otherwise>
									<c:if test="${entry.adjustedItemPrice.value > 0}">
										<format:price priceData="${entry.adjustedItemPrice}" displayFreeForZero="flase"/>
									</c:if>
								</c:otherwise>
							</c:choose>
						</c:when>
						<c:otherwise>
							<format:price priceData="${entry.product.priceRange.minPrice}" displayFreeForZero="false"/>
							<format:price priceData="${entry.product.priceRange.maxPrice}" displayFreeForZero="false"/>
						</c:otherwise>
					</c:choose>
				</ycommerce:testId>
				</td>
			</c:otherwise>
			</c:choose>
	</c:if>
	
	<c:if test="${adjustedTotalFlag > 0}">
		<td headers="header10" class="adjustedTotalPrice">
			<%-- <ycommerce:testId code="orderDetails_productQuantity_label" ><fmt:formatNumber value="${entry.adjustedLinePrice}" maxFractionDigits="2"/></ycommerce:testId> --%>
			<ycommerce:testId code="orderDetails_productQuantity_label" >
				<c:choose>
					<c:when test="${entry.rejectedStatus == 'Yes'}">
						<format:price priceData="${entry.adjustedLinePrice}" displayFreeForZero="flase"/>
					</c:when>
					<c:otherwise>
						<c:if test="${entry.adjustedLinePrice.value > 0}">
							<format:price priceData="${entry.adjustedLinePrice}" displayFreeForZero="flase"/>
						</c:if>
					</c:otherwise>
				</c:choose>
				
			</ycommerce:testId>
		</td>
	</c:if>
	
	<td headers="header11" class="rejected">
		<ycommerce:testId code="orderDetails_productQuantity_label">${entry.rejectedStatus}</ycommerce:testId>
	</td>
	
	<c:if test="${siteUid eq 'personalCare' and not isSalesRepUserLogin}">
		<td headers="header13" class="agreeEdgewellPrice"> 				
			<c:choose>
				<c:when test="${entry.agreeEdgewellUnitPrice}">
					<input type="checkbox" checked="checked" disabled="disabled" name="agreeEdgewellUnitPrice" />
				</c:when>
				<c:otherwise>
					<input type="checkbox" disabled="disabled" name="agreeEdgewellUnitPrice" />
				</c:otherwise>
			</c:choose>
		</td>  
	</c:if>
					
	
	
	
<!-- 	<td headers="header16" class="rejected">
		<ycommerce:testId code="orderDetails_custProductName_label">${entry.product.customerProductName}</ycommerce:testId>
	</td>
	<td headers="header17" class="rejected">
		<ycommerce:testId code="orderDetails_CustSpecificPrice_label">${entry.product.customerProductPrice}</ycommerce:testId>
	</td>
	<td headers="header18" class="rejected">
		<ycommerce:testId code="orderDetails_ShipFrom_label">${entry.product.shippingPoint}</ycommerce:testId>
	</td>
	<td headers="header19" class="rejected">
		<ycommerce:testId code="orderDetails_MOU_label">${entry.product.moq}</ycommerce:testId>
	</td>
	<td headers="header20" class="rejected">
		<ycommerce:testId code="orderDetails_UOM_label">${entry.product.uom}</ycommerce:testId>
	</td>
	 -->
</tr>


			
