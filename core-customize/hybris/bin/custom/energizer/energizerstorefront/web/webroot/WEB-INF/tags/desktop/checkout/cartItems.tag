<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="cartData" required="true" type="de.hybris.platform.commercefacades.order.data.CartData" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/desktop/template" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/desktop/common" %>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/desktop/product" %>
<%@ taglib prefix="grid" tagdir="/WEB-INF/tags/desktop/grid" %>


<div id="cartItems" class="clear">
	<div class="headline">
		<spring:theme code="basket.page.title.yourItems"/>
		<span class="cartId">
			<spring:theme code="basket.page.cartId"/>&nbsp;<span class="cartIdNr">${cartData.code}</span>
		</span>
	</div>
	<table class="cart">
		<thead>
			<tr>
			 <c:choose>
			  <c:when test="${isSalesRepUserLogin}">
			  <th id="header2"><spring:theme
						code="basket.page.title" /></th>
			  </c:when>
			  <c:otherwise>
			  <th id="header2" colspan="2"><spring:theme
						code="basket.page.title" /></th>
			  </c:otherwise>
			  </c:choose>
			
				<th id="header3"><spring:theme code="basket.page.prdCode"/></th>				
				<th id="header5"><spring:theme code="basket.page.cmirId"/></th>
				<c:if test="${not isSalesRepUserLogin}">
					<th id="header6"><spring:theme code="basket.page.shipFrom" /></th>
				</c:if>
				<c:if test="${siteUid eq 'personalCare' and not isSalesRepUserLogin}">
					<th id="header12"><spring:theme code="basket.page.unitPrice" /></th>
				</c:if>
				<th id="header7"><spring:theme code="basket.page.uom"/></th>
					
				<c:if test="${isSalesRepUserLogin}">
				<th id="header15"><spring:theme code="basket.page.unitsPeruom" /></th>
				</c:if>
				<c:if test="${isSalesRepUserLogin}">
					<th id="header14"><spring:theme code="basket.page.currency" /></th>
				</c:if>
										
				<th id="header8"><spring:theme code="basket.page.uomPrice"/></th>
				<c:if test="${siteUid ne 'personalCareEMEA' and not isSalesRepUserLogin}">
				<th id="header9"><spring:theme code="basket.page.expectedUnitPrice" text="Customer Expected Price"/></th>	
				</c:if>			
				<th id="header10"><spring:theme code="basket.page.quantity"/></th>
				<th id="header11"><spring:theme code="basket.page.total"/></th>
				
				<c:if test="${siteUid eq 'personalCare' and not isSalesRepUserLogin}">
					<th id="header13" class="agreeEdgewellPriceForAllProducts"><spring:theme code="basket.page.agree.edgewell.price" />
						<c:choose>
							<c:when test="${cartData.agreeEdgewellUnitPriceForAllProducts}">
								<input type="checkbox" checked="checked" disabled="disabled" name="agreeEdgewellUnitPriceForAllProducts" />
							</c:when>
							<c:otherwise>
								<input type="checkbox" disabled="disabled" name="agreeEdgewellUnitPriceForAllProducts" />
							</c:otherwise>
						</c:choose>
					</th>
				</c:if>
			</tr>
		</thead>
		<tbody>
			<c:forEach items="${cartData.entries}" var="entry">
				<c:url value="${entry.product.url}" var="productUrl"/>
				<tr class="cartItem">
					<c:if test="${not isSalesRepUserLogin}">
						<td headers="header2" class="thumb"><a href="${productUrl}"><product:productPrimaryImage
									product="${entry.product}" format="thumbnail" /></a>
									</td>
					</c:if>

					<td headers="header2" class="details">
						<ycommerce:testId code="cart_product_name">
							<div class="itemName"><a href="${productUrl}">${entry.product.name}</a></div>
						</ycommerce:testId>
						
						<c:set var="entryStock" value="${entry.product.stock.stockLevelStatus.code}"/>
						
						<c:forEach items="${entry.product.baseOptions}" var="option">
							<c:if test="${not empty option.selected and option.selected.url eq entry.product.url}">
								<c:forEach items="${option.selected.variantOptionQualifiers}" var="selectedOption">
									<div>
										<strong>${selectedOption.name}:</strong>
										<span>${selectedOption.value}</span>
									</div>
									<c:set var="entryStock" value="${option.selected.stock.stockLevelStatus.code}"/>
									<div class="clear"></div>
								</c:forEach>
							</c:if>
						</c:forEach>
						
						<c:if test="${ycommerce:doesPotentialPromotionExistForOrderEntry(cartData, entry.entryNumber)}">
							<ul class="potentialPromotions">
								<c:forEach items="${cartData.potentialProductPromotions}" var="promotion">
									<c:set var="displayed" value="false"/>
									<c:forEach items="${promotion.consumedEntries}" var="consumedEntry">
										<c:if test="${not displayed && consumedEntry.orderEntryNumber == entry.entryNumber && not empty promotion.description}">
											<c:set var="displayed" value="true"/>
											<li>
												<ycommerce:testId code="cart_potentialPromotion_label">
													${promotion.description}
												</ycommerce:testId>
											</li>
										</c:if>
									</c:forEach>
								</c:forEach>
							</ul>
						</c:if>
								
						<c:if test="${ycommerce:doesAppliedPromotionExistForOrderEntry(cartData, entry.entryNumber)}">
							<ul class="appliedPromotions">
								<c:forEach items="${cartData.appliedProductPromotions}" var="promotion">
									<c:set var="displayed" value="false"/>
									<c:forEach items="${promotion.consumedEntries}" var="consumedEntry">
										<c:if test="${not displayed && consumedEntry.orderEntryNumber == entry.entryNumber}">
											<c:set var="displayed" value="true"/>
											<li>
												<ycommerce:testId code="cart_appliedPromotion_label">
													${promotion.description}
												</ycommerce:testId>
											</li>
										</c:if>
									</c:forEach>
								</c:forEach>
							</ul>
						</c:if>
					</td>					
					<td headers="header3" class="prdCode">
					 ${entry.product.erpMaterialID}
					</td>					
					
					<td headers="header5" class="cmirId">
					  ${entry.product.customerMaterialId}
					</td>
					
					<c:if test="${not isSalesRepUserLogin}">
						<td headers="header6" class="shipFrom">
						 ${entry.product.shippingPointName}
						</td>
					</c:if>
					
					<c:if test="${siteUid eq 'personalCare' and not isSalesRepUserLogin}">
						<td headers="header12" class="itemEachPrice" id="itemEachPrice_${entry.entryNumber}">
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
					
					<td headers="header7" class="uom">
					 ${entry.product.uom}
					</td>
				
				    <c:if test="${isSalesRepUserLogin}">
					<td headers="header15" class="unitsPeruom">
					${entry.product.baseUOM}
					</td>
					</c:if>
					<c:choose>		
						<c:when test="${isSalesRepUserLogin and gotPriceFromSAP}">
							<td headers="header14" class="currency">
								${entry.basePrice.currencyIso}
								<c:set var="currencyISO" value="${entry.basePrice.currencyIso}" />
								</td>
						</c:when>
						<c:when test="${isSalesRepUserLogin and not gotPriceFromSAP}">
							<td headers="header14" class="currency"> </td>
						</c:when>
						<c:otherwise>	
						</c:otherwise>
					</c:choose>	
					
					<td headers="header8" class="itemPrice">
							<c:choose>
								<c:when test="${entry.product.multidimensional and (entry.product.priceRange.minPrice.value ne entry.product.priceRange.maxPrice.value)}" >
									<format:price priceData="${entry.product.priceRange.minPrice}" displayFreeForZero="true"/>
									-
									<format:price priceData="${entry.product.priceRange.maxPrice}" displayFreeForZero="true"/>
								</c:when>
								<c:otherwise>
						    <c:choose>
								<c:when test="${not isSalesRepUserLogin}">
								    <format:price priceData="${entry.basePrice}" displayFreeForZero="true"/> 
								</c:when>
								<c:when test="${isSalesRepUserLogin and gotPriceFromSAP}">
									<format:price priceData="${entry.basePrice}" displayFreeForZero="true" displayOnlyValue="true" currencyISO="${currencyISO}"/>
								</c:when>
								<c:otherwise>
								</c:otherwise>
							</c:choose>
							    </c:otherwise>
							</c:choose>
				   </td>
				
					
				<c:if test="${siteUid ne 'personalCareEMEA' and not isSalesRepUserLogin}">
					<td headers="header9" class="expectedPrice">
					<input type="hidden" name="entryNumber" value="${entry.entryNumber}" /> 
					<input type="hidden" name="productCode" value="${entry.product.code}" />
					<input type="hidden" name="expectedPrice"	value="${entry.expectedUnitPrice}" />
					 <span class="unitPrice"> 	 
					  <c:choose>
							<c:when test="${empty entry.expectedUnitPrice}" >					 
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
								${symbol} ${entry.expectedUnitPrice}
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
					 	</span>
					</td>
				  </c:if>
				  
					<td headers="header10" class="quantity"><input type="hidden"
						name="entryNumber" value="${entry.entryNumber}" /> <input
						type="hidden" name="productCode" value="${entry.product.code}" />
						<input type="hidden" name="initialQuantity"	value="${entry.quantity}" />
						 <span class="qty"> <c:out	value="${entry.quantity}" /></span>
					</td>
					
					
   					<c:choose>
			  	<c:when test="${not isSalesRepUserLogin}">
					<td headers="header11" class="total">
						<ycommerce:testId code="cart_totalProductPrice_label">
							<format:price priceData="${entry.totalPrice}" displayFreeForZero="true"/>
						</ycommerce:testId>
					</td>	
				</c:when>
                <c:when test="${isSalesRepUserLogin and gotPriceFromSAP}">
					<td headers="header11" class="total">
						<ycommerce:testId code="cart_totalProductPrice_label">
							<format:price priceData="${entry.totalPrice}" displayFreeForZero="true" displayOnlyValue="true" currencyISO="${currencyISO}"/>
						</ycommerce:testId>
					</td>	
				</c:when>
				<c:otherwise>
				</c:otherwise>
			</c:choose>
					
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
				</tr>
				 
				<c:if test="${entry.product.multidimensional}" >
					<tr><th colspan="5">
						<c:forEach items="${entry.entries}" var="currentEntry" varStatus="stat">
							<c:set var="subEntries" value="${stat.first ? '' : subEntries}${currentEntry.product.code}:${currentEntry.quantity},"/>
						</c:forEach>

						<div style="display:none" id="grid_${entry.product.code}" data-sub-entries="${subEntries}"> </div>
					</th></tr>		
				</c:if>
				 
			</c:forEach>
		</tbody>
	</table>
	
	<product:productOrderFormJQueryTemplates />
	
</div>

