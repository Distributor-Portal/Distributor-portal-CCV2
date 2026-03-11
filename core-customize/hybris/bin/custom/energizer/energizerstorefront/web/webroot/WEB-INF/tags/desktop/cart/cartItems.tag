<%@ tag body-content="empty" trimDirectiveWhitespaces="true"%>
<%@ attribute name="cartData" required="true"
	type="de.hybris.platform.commercefacades.order.data.CartData"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/desktop/template"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme"%>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format"%>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/desktop/common"%>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/desktop/product"%>
<%@ taglib prefix="grid" tagdir="/WEB-INF/tags/desktop/grid"%>
<c:url var="getDoubleStackProducts" value="/search/getDoubleStackProducts" />

<div id="cartItems" class="clear">
	<div class="headline">
		<spring:theme code="basket.page.title.yourItems" />
		<span class="cartId"> <spring:theme code="basket.page.cartId" />&nbsp;<span
			class="cartIdNr">${cartData.code}</span>
		</span>
	</div>
	
	<c:set var="cartEntriesSize" value="${fn:length(cartData.entries)}" />
	<input type="hidden" name="entriesSize" id="cartEntriesSize" value="${cartEntriesSize}" />
	<c:set var="currencySymbol" value="${currencySymbol}" />
	<input type="hidden" name="currencySymbol" class="currencySymbol" value="${currencySymbol}" />
	<input type="hidden" name="agreeValidationCounter" value="0" />
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
			  
			
				<th id="header3"><spring:theme code="basket.page.prdCode" /></th>
				<th id="header5"><spring:theme code="basket.page.cmirId" /></th>
				 <c:if test="${not isSalesRepUserLogin}">
					<th id="header6"><spring:theme code="basket.page.shipFrom" /></th>
				</c:if>
				<c:if test="${not isSalesRepUserLogin and siteUid eq 'personalCare'}">
					 <th id="header14"><spring:theme code="basket.page.unitPrice" /></th> 
				</c:if>
			
					
			 
				<c:if test="${not isSalesRepUserLogin and siteUid eq 'personalCare'}">
					<th id="header9"><spring:theme code="basket.page.expectedUnitPrice" text="Customer Expected Price"/></th>
				</c:if>
		 
				<th id="header7"><spring:theme code="basket.page.uom" /></th>
				
				<c:if test="${isSalesRepUserLogin}">
				<th id="header16"><spring:theme code="basket.page.unitsPeruom" /></th>
				<th id="header20"><spring:theme code="basket.page.invertoryAvailable" /></th>
				</c:if>
				
				<c:if test="${isSalesRepUserLogin}">
					<th id="header15"><spring:theme code="basket.page.currency" /></th>
				</c:if>
				
				<th id="header8"><spring:theme code="basket.page.uomPrice" /></th>
				<c:if test="${isSalesRepUserLogin}">
				<%-- <th id="header21"><spring:theme code="basket.page.discountAmount" /></th> --%>
				<th id="header21"><spring:theme code="basket.page.discountPercent" /></th>
				</c:if>     
				<th id="header10"><spring:theme code="basket.page.quantity" /></th>
				<th id="header11"><spring:theme code="basket.page.total" /></th>
				<c:if test="${siteUid eq 'personalCare' and not isSalesRepUserLogin and enableButton and  cartData.enableFloorSpaceGraphics}">
					<th id="header12"><spring:theme
							code="basket.page.doubleStackProduct" />
					</th>
				</c:if>
				<c:if test="${not isSalesRepUserLogin and siteUid eq 'personalCare'}">
					<th id="header13" class="agreeEdgewellPriceForAllProducts"><spring:theme code="basket.page.agree.edgewell.price" />
						<c:url value="/cart/update/agreeEdgewellUnitPriceForAllProducts" var="cartUpdateAgreeEdgewellUnitPriceForAllProductsFormAction" />
						<form:form id="updateCartAgreeEdgewellUnitPriceForAllProductsForm" action="${cartUpdateAgreeEdgewellUnitPriceForAllProductsFormAction}"
								   method="post" modelAttribute="UpdateExpectedUnitPriceForm">
							<input type="hidden" name="agreeEdgewellUnitPriceForAllProducts" id="initialAgreeEdgewellUnitPriceForAllProducts" value="${cartData.agreeEdgewellUnitPriceForAllProducts}" />
							<input type="hidden" name="agreeEdgewellPriceForAllProducts" id="agreeEdgewellUnitEachPriceForAllProducts" value="${cartData.agreeEdgewellUnitPriceForAllProducts}"/>
							<span class="agreeEdgewellPriceAll">
								<%-- ${cartData.agreeEdgewellUnitPriceForAllProducts} --%>
								<c:choose>
									<c:when test="${cartData.agreeEdgewellUnitPriceForAllProducts}">
										<form:checkbox name="agreeEdgewellUnitPriceForAllProducts" checked="checked" id="agreeEdgewellPriceAll" path="agreeEdgewellUnitPriceForAllProducts" value="${cartData.agreeEdgewellUnitPriceForAllProducts}" />
									</c:when>
									<c:otherwise>
										<form:checkbox name="agreeEdgewellUnitPriceForAllProducts" id="agreeEdgewellPriceAll" path="agreeEdgewellUnitPriceForAllProducts" value="${cartData.agreeEdgewellUnitPriceForAllProducts}" />
									</c:otherwise>
								</c:choose>
								<%-- <form:checkbox name="agreeEdgewellUnitPriceForAllProducts" id="agreeEdgewellPriceAll" path="agreeEdgewellUnitPriceForAllProducts" value="${cartData.agreeEdgewellUnitPriceForAllProducts}" /> --%>
							</span>
						</form:form>	
					</th>
				</c:if>
			</tr>
		</thead>
		<tbody>
			<c:forEach items="${cartData.entries}" var="entry">
				<c:url value="${entry.product.url}" var="productUrl" />
				<tr class="cartItem">
					<c:if test="${not isSalesRepUserLogin}">
						<td headers="header2" class="thumb"><a href="${productUrl}"><product:productPrimaryImage
									product="${entry.product}" format="thumbnail" /></a></td>
					</c:if>
					<td headers="header2" class="details"><ycommerce:testId
							code="cart_product_name">
							<div class="itemName">
								<a href="${productUrl}">${entry.product.name}</a>
							</div>
							
							
						</ycommerce:testId> <c:set var="entryStock"
							value="${entry.product.stock.stockLevelStatus.code}" /> <c:forEach
							items="${entry.product.baseOptions}" var="option">
							<c:if
								test="${not empty option.selected and option.selected.url eq entry.product.url}">
								<c:forEach items="${option.selected.variantOptionQualifiers}"
									var="selectedOption">
									<div>
										<strong>${selectedOption.name}:</strong> <span>${selectedOption.value}</span>
									</div>
									<c:set var="entryStock"
										value="${option.selected.stock.stockLevelStatus.code}" />
									<div class="clear"></div>
								</c:forEach>
							</c:if>
						</c:forEach> <c:if
							test="${ycommerce:doesPotentialPromotionExistForOrderEntry(cartData, entry.entryNumber)}">
							<ul class="potentialPromotions">
								<c:forEach items="${cartData.potentialProductPromotions}"
									var="promotion">
									<c:set var="displayed" value="false" />
									<c:forEach items="${promotion.consumedEntries}"
										var="consumedEntry">
										<c:if
											test="${not displayed && consumedEntry.orderEntryNumber == entry.entryNumber && not empty promotion.description}">
											<c:set var="displayed" value="true" />
											<li><ycommerce:testId
													code="cart_potentialPromotion_label">
													${promotion.description}
												</ycommerce:testId></li>
										</c:if>
									</c:forEach>
								</c:forEach>
							</ul>
						</c:if> <c:if
							test="${ycommerce:doesAppliedPromotionExistForOrderEntry(cartData, entry.entryNumber)}">
							<ul class="appliedPromotions">
								<c:forEach items="${cartData.appliedProductPromotions}"
									var="promotion">
									<c:set var="displayed" value="false" />
									<c:forEach items="${promotion.consumedEntries}"
										var="consumedEntry">
										<c:if
											test="${not displayed && consumedEntry.orderEntryNumber == entry.entryNumber}">
											<c:set var="displayed" value="true" />
											<li><ycommerce:testId code="cart_appliedPromotion_label">
													${promotion.description}
												</ycommerce:testId></li>
										</c:if>
									</c:forEach>
								</c:forEach>
							</ul>
						</c:if>
						</td>
		

					<td headers="header3" class="prdCode">
						<c:choose>
						<c:when test="${(isSalesRepUserLogin and cmirNotAvailable and entry.product.removeFromCart)}">
							<span style="color: #c90400">${entry.product.erpMaterialID}</span>
						</c:when>
						<c:when test="${isSalesRepUserLogin and gotPriceFromSAP and (empty entry.basePrice.value or entry.basePrice.value == '0.01' or entry.basePrice.value == '0.00')}"> 
							<span style="color: #c90400">${entry.product.erpMaterialID}</span>
						</c:when>	 
						<c:otherwise>  
							${entry.product.erpMaterialID}
						</c:otherwise>
						</c:choose>
					</td>

					<td headers="header5" class="cmirId">
						${entry.product.customerMaterialId}</td>

					<c:if test="${not isSalesRepUserLogin}">
					<td headers="header6" class="shipFrom">
						${entry.product.shippingPointName}</td>
						</c:if>
					
					
					  <%-- <c:choose> --%>
						<c:if test="${not isSalesRepUserLogin and siteUid eq 'personalCare' }">
							<%-- <c:if test="${siteUid eq 'personalCare' and not isSalesRepUserLogin}"> --%>
								<td headers="header14" class="itemEachPrice"
									id="itemEachPrice_${entry.entryNumber}">
									<c:choose>
										<c:when
											test="${entry.product.multidimensional and (entry.product.priceRange.minPrice.value ne entry.product.priceRange.maxPrice.value)}">
											<format:price
												priceData="${entry.product.priceRange.minPrice}"
												displayFreeForZero="true" />
									-
									<format:price priceData="${entry.product.priceRange.maxPrice}"
												displayFreeForZero="true" />
										</c:when>
										<c:otherwise>
											<format:price priceData="${entry.eachUnitPrice}"
												displayFreeForZero="true" />
												<%-- <c:out value="/EA" /> --%>
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
						<%-- <c:otherwise>
						 <td headers="header14" class="itemEachPrice" id="itemEachPrice_${entry.entryNumber}" ></td>
		  
						</c:otherwise>
			  </c:choose> --%>
					
					
					
			
					<c:if test="${not isSalesRepUserLogin and siteUid eq 'personalCare'}">
					 	<td headers="header9" class="expectedPrice" style="padding: 15px 0px 0px 15px !important;"> <c:url
							value="/cart/update/expectedUnitPrice" var="cartUpdateExpectedPriceFormAction" /> <form:form
							id="updateCartExpectedPriceForm${entry.entryNumber}"
							action="${cartUpdateExpectedPriceFormAction}" method="post"
							modelAttribute="UpdateExpectedUnitPriceForm${entry.entryNumber}" cssStyle="width: 80px;">							
							<input type="hidden" name="entryNumber"
								value="${entry.entryNumber}" />
							<input type="hidden" name="productCode"
								value="${entry.product.code}" />
							<input type="hidden" name="initialExpectedPrice"
								value="" />
								
								<%--  <c:choose>
									<c:when test="${empty entry.expectedUnitPrice or entry.expectedUnitPrice eq 0}">
										<c:set var="expectedUnitPrice" value="" />
									</c:when>
									<c:otherwise>
										<c:set var="expectedUnitPrice" value="${entry.expectedUnitPrice}" />
									</c:otherwise>
								</c:choose>  --%>
								
								<input type="hidden" name="initialExpectedUnitPrice" id="initialExpectedUnitPrice_${entry.entryNumber}"
								value="${entry.expectedUnitPrice}" />
									<div style="float:left;">${currencySymbol}</div>
									
									<c:set var="isCustomerExpectedPriceEmpty" value="false" />
									<c:forEach var="orderEntryErrorItem" items="${orderEntryDataErrors}">
										<c:if test="${orderEntryErrorItem.product.erpMaterialID eq entry.product.erpMaterialID}">
											<c:set var="isCustomerExpectedPriceEmpty" value="true" />
										</c:if>
									</c:forEach>
                         
									<c:choose>
										<c:when test="${entry.agreeEdgewellUnitPrice}">
											<form:input type="text" class="unitPrice" disabled="true" cssStyle="float:left;width:40px;" id="expectedPrice_${entry.entryNumber}" path="expectedUnitPrice" />
										</c:when>
										<c:otherwise>
											<c:choose>
												<c:when test="${isCustomerExpectedPriceEmpty eq 'true'}">
													<form:input type="text" class="unitPrice highlightCartEntryErrorInput" cssStyle="float:left;width:40px;"  id="expectedPrice_${entry.entryNumber}" path="expectedUnitPrice" />
												</c:when>
												<c:otherwise>
													<form:input type="text" class="unitPrice" cssStyle="float:left;width:40px;"  id="expectedPrice_${entry.entryNumber}" path="expectedUnitPrice" />
												</c:otherwise>
											</c:choose>
									
											<%-- <form:input type="text" class="unitPrice" cssStyle="float:left;width:40px;"  id="expectedPrice_${entry.entryNumber}" path="expectedUnitPrice" /> --%>
										</c:otherwise>
										<%-- <c:otherwise>
											<form:input type="text" class="unitPrice" cssStyle="float:left;width:40px;"  id="expectedPrice_${entry.entryNumber}" path="expectedUnitPrice" />
										</c:otherwise> --%>
							
									</c:choose>
									<c:choose>
						            	<c:when test="${empty entry.product.priceUOM}">
						            		<div style="float:left;width: 27px;margin-top: -4px;">
												<span id="slash" style="font-size: 23px;vertical-align: sub;">/</span><span style="vertical-align: text-bottom;font-size: 14px;">EA</span>
											</div>
						            	</c:when>
						            	<c:otherwise>
							            	<div style="float:left;width: 27px;margin-top: -4px;">
												<span id="slash" style="font-size: 23px;vertical-align: sub;">/</span><span style="vertical-align: text-bottom;font-size: 14px;">${entry.product.priceUOM}</span>
											</div>
						            	</c:otherwise>
					            	</c:choose>
									
									<!-- <input style="float:left;width:40px;" id="expectedPrice_1" name="expectedUnitPrice" type="text" class="unitPrice" disabled="disabled" value="10.00"/> -->
								
								
								<!-- </div> -->
								<!-- </div> -->
								<%-- <form:input type="text" class="unitPrice" id="expectedPrice_${entry.entryNumber}" path="expectedUnitPrice" /> --%>
								<%--  <div style="border:1px solid red;float:left"> <c:out value="/EA"/> </div> --%>
								
							
							</form:form> 
 						</td>  
 						</c:if>
		
					
			<td headers="header7" class="uom">
                    <c:choose>
                <c:when test="${siteUid eq 'personalCareEMEA'}">${entry.product.uom}</c:when>
                <c:otherwise>
                   <form id="getSelectedUomCartPage${entry.product.code}" name="getSelectedUomCart" action="/updateUOM" method="POST" >
                      <input type="hidden" id="productCodeCart_${entry.product.code}" name="productCode" value="${entry.product.code}" />
                      <input type="hidden" id="selectedUomCart_${entry.product.code}" name="uom" value="${entry.product.uom}" />
                      <input type="hidden" id="selectedUomQty_${entry.product.code}" name="uomQty" value="${entry.quantity}" />
                   </form>
         	 	   	<%-- <select name="uomListInCart" id="uomInCart_${entry.product.code}_${entry.entryNumber}" class="uomSelectorCart" onchange='onUOMChange(this.id,this.value)'> --%>
                     <c:choose>
                        <c:when test="${not isSalesRepUserLogin and not product.isWeSellProduct}">
                        	<select name="uomListInCart" id="uomInCart_${entry.product.code}_${entry.entryNumber}" class="uomSelectorCart" onchange='onUOMChange(this.id,this.value)'>
			                     <c:choose>
			                        <c:when test="${entry.product.uom eq 'PAL'}">
			                           <option value="PAL" selected="selected" >PAL</option>
			                        </c:when>
			                        <c:otherwise>
			                            <option value="PAL" >PAL</option>
			                        </c:otherwise>
			                     </c:choose>
			                     <c:choose>
			                        <c:when test="${entry.product.uom eq 'CS'}">
			                          <option value="CS" selected="selected" >CS</option>
			                        </c:when>
			                        <c:otherwise>
			                           <option value="CS" >CS</option>
			                        </c:otherwise>
			                     </c:choose>
			                     <c:choose>
			                          <c:when test="${entry.product.uom eq 'LAY'}">
			                            <option value="LAY" selected="selected" >LAY</option>
			                          </c:when>
			                          <c:otherwise>
			                            <option value="LAY" >LAY</option>
			                          </c:otherwise>
			                     </c:choose>
		                    </select>
                        </c:when>
	                    <c:otherwise>
	                          <%-- <option value="${entry.product.uom}" selected="selected" >${entry.product.uom}</option> --%>
	                          ${entry.product.uom}
		                </c:otherwise>
	                 </c:choose>
	          		<!-- </select> -->
	           </c:otherwise>
	        </c:choose>
		</td>
		
		<c:if test="${isSalesRepUserLogin}">
		  <td headers="header16" class="unitsPeruom">
			${entry.product.baseUOM}
		  </td>
		   <td headers="header20" style="text-align: center" class="invertoryAvailable">
		   <c:if test="${gotPriceFromSAP}" >
				${entry.inventoryAvailable}
			</c:if>
		  </td>
		</c:if>
			
		<c:choose>		
			<c:when test="${isSalesRepUserLogin and gotPriceFromSAP}">
				<td headers="header15" class="currency">
					${entry.basePrice.currencyIso}
					<c:set var="currencyISO" value="${entry.basePrice.currencyIso}" />
					</td>
			</c:when>
			<c:when test="${isSalesRepUserLogin and not gotPriceFromSAP}">
				<td headers="header15" class="currency"> </td>
			</c:when>
			<c:otherwise>	
			</c:otherwise>
		</c:choose>			
		
			 
					 
					  <%-- <c:choose> --%>
			  <%-- <c:if test="${not isSalesRepUserLogin}"> --%>
				  <td headers="header8" class="itemPrice" id="itemPrice_${entry.entryNumber}">
					  <div id="itemPriceDiv_${entry.entryNumber}">
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
								<c:when test="${isSalesRepUserLogin and gotPriceFromSAP}">
									<c:choose>
										<c:when test="${empty entry.basePrice.value or entry.basePrice.value == '0.01' or entry.basePrice.value == '0.00'}">
											<span style="color: #c90400">  
												<format:price priceData="${entry.basePrice}" displayFreeForZero="true" displayOnlyValue="true" currencyISO="${currencyISO}"/>
											</span>
										</c:when>
										<c:otherwise>
											<format:price priceData="${entry.basePrice}" displayFreeForZero="true" displayOnlyValue="true" currencyISO="${currencyISO}"/>
										</c:otherwise>
									</c:choose>
								</c:when>
								<c:otherwise>
								
								</c:otherwise>
								</c:choose>
							</c:otherwise>
						</c:choose> 	
				      </div>			 
				  </td>
				  
				<%-- </c:if> --%>
				<%-- <c:otherwise>
				 <td headers="header8" class="itemPrice" id="itemPrice_${entry.entryNumber}"> </td>
		  
				</c:otherwise>
			</c:choose> --%>
			<%-- <c:if test="${isSalesRepUserLogin}">
				 <td headers="header21" style="text-align: center" class="discountAmount">
				   <c:if test="${gotPriceFromSAP}" >
				   <c:choose>
				   	<c:when test="${empty entry.discountAmount}">
				   	<c:out value="0.00"></c:out>
				   	</c:when>
				   	<c:otherwise>
				   		${entry.discountAmount}
				   	</c:otherwise>
				   </c:choose>
					</c:if>
				  </td>	 
			</c:if> --%>
			<c:if test="${isSalesRepUserLogin}">
				 <td headers="header21" style="text-align: center" class="discountPercent">
				   <c:if test="${gotPriceFromSAP}" >
				   <c:choose>
				   	<c:when test="${empty entry.discountPercent}">
				   		<c:out value="0.00"></c:out>
				   	</c:when>
				   	<c:when test="${not empty entry.discountPercent and entry.discountPercent eq 'null'}">
				   		<c:out value=""/> 
				   	</c:when>
				   	<c:otherwise>
				   		${entry.discountPercent}
				   	</c:otherwise>
				   </c:choose>
					</c:if>
				  </td>	 
			</c:if>
					
					<td headers="header10" class="quantity"><c:url
							value="/cart/update/" var="cartUpdateFormAction" /> <form:form
							id="updateCartForm${entry.entryNumber}"
							action="${cartUpdateFormAction}" method="post"
							modelAttribute="updateQuantityForm${entry.entryNumber}">
							<input type="hidden" name="entryNumber"
								value="${entry.entryNumber}" />
							<input type="hidden" name="productCode"
								value="${entry.product.code}" />
							<input type="hidden" name="initialQuantity"
								value="${entry.quantity}" />
							<input type="hidden" name="moq" value="${entry.product.moq}" />

						

							<c:choose>
								<c:when test="${not entry.product.multidimensional}">
									<ycommerce:testId code="cart_product_quantity">
										<form:label cssClass="skip" path="quantity"
											for="quantity${entry.entryNumber}">
											<spring:theme code="basket.page.quantity" />
										</form:label>
										<c:if test="${entry.updateable}">
										<!-- Added for Ordering quantity limit to 7 digits for EMEA - START -->
												<c:choose>
													<c:when test="${siteUid eq 'personalCareEMEA'}">
															<form:input disabled="${not entry.updateable}" type="text"
															maxlength="7" size="3" id="quantity_${entry.entryNumber}" class="qty"
															path="quantity" />
													</c:when>
													<c:otherwise>
														<form:input disabled="${not entry.updateable}" type="text"
															maxlength="5" size="3" id="quantity_${entry.entryNumber}" class="qty"
															path="quantity" />
													</c:otherwise>
												</c:choose>     
										<!-- Added for Ordering quantity limit to 7 digits for EMEA - END -->
										</c:if>
									</ycommerce:testId>

									<!--  
									<c:if test="${entry.updateable}" >
										<ycommerce:testId code="cart_product_updateQuantity">
											<a href="#" id="QuantityProduct_${entry.entryNumber}" class="updateQuantityProduct"><spring:theme code="basket.page.update"/></a>
										</ycommerce:testId>
									</c:if> -->
								</c:when>
								<c:otherwise>
									<span class="qty"> <c:out value="${entry.quantity}" />
									</span>
									<input type="hidden" name="quantity" value="0" />
									<ycommerce:testId code="cart_product_updateQuantity">
										<a href="#" id="QuantityProduct_${entry.product.code}"
											class="updateQuantityProduct"><spring:theme
												code="basket.page.updateMultiD" /></a>
									</ycommerce:testId>
								</c:otherwise>
							</c:choose>

						</form:form> 
						<c:if test="${entry.updateable}">
							<ycommerce:testId code="cart_product_removeProduct">
							<c:choose>
								<c:when test="${isSalesRepUserLogin and cmirNotAvailable and entry.product.removeFromCart}">
										<spring:theme code="text.iconCartRemove" var="iconCartRemove" />
											<a style="color: #c90400" href="#" id="RemoveProduct_${entry.entryNumber}"
										class="submitRemoveProduct">${iconCartRemove}</a>
								</c:when>
								<c:otherwise>
									<spring:theme code="text.iconCartRemove" var="iconCartRemove" />
									<a href="#" id="RemoveProduct_${entry.entryNumber}"
									class="submitRemoveProduct">${iconCartRemove}</a>
								</c:otherwise>
							</c:choose>
								
							</ycommerce:testId>
						</c:if></td>



					 <%-- <c:choose>
						<c:when test="${not isSalesRepUserLogin}"> --%>
						<c:choose>
							<c:when test="${not isSalesRepUserLogin}">
								<td headers="header11" class="total">
									<div id="totalPriceDiv_${entry.entryNumber}">
										<ycommerce:testId code="cart_totalProductPrice_label">
											<format:price priceData="${entry.totalPrice}" displayFreeForZero="true" />
										</ycommerce:testId>
									</div>
								</td>
							</c:when>
							<c:when test="${isSalesRepUserLogin and gotPriceFromSAP}">
								<td headers="header11" class="total test" id="totalPrice_${entry.entryNumber}">
									<div id="totalPriceDiv_${entry.entryNumber}">
										<ycommerce:testId code="cart_totalProductPrice_label">
										<c:choose>
											<c:when test="${empty entry.basePrice.value or entry.basePrice.value == '0.01' or entry.basePrice.value == '0.00'}">
												<span style="color: #c90400">  
													<format:price priceData="${entry.totalPrice}" displayFreeForZero="true" displayOnlyValue="true" currencyISO="${currencyISO}"/>
												</span>
											</c:when>
											<c:otherwise>
													<format:price priceData="${entry.totalPrice}" displayFreeForZero="true" displayOnlyValue="true" currencyISO="${currencyISO}"/>
											</c:otherwise>
										</c:choose>											
										</ycommerce:testId>
									</div>
								</td>
							</c:when>
							<c:otherwise>
								<td headers="header11" class="total">
									<div id="totalPriceDiv_${entry.entryNumber}">
									<%-- <ycommerce:testId code="cart_totalProductPrice_label">
										<format:price priceData="${entry.totalPrice}" displayFreeForZero="true" />
									</ycommerce:testId> --%>
									</div>
								</td>
							</c:otherwise>
						</c:choose>
						<%-- <c:if test="${gotPriceFromSAP}">
							<td headers="header11" class="total">
								<ycommerce:testId code="cart_totalProductPrice_label">
									<format:price priceData="${entry.totalPrice}" displayFreeForZero="true" />
								</ycommerce:testId>
							</td>
						</c:if>	 --%>	
						<%-- </c:when>
						<c:otherwise>
							<td headers="header11" class="total">
								<ycommerce:testId code="cart_totalProductPrice_label">
									<format:price priceData="${entry.totalPrice}" displayFreeForZero="true" />
								</ycommerce:testId>
							</td>
						</c:otherwise>
					</c:choose>  --%> 


					<%-- 	<td headers="header11" class="total"><ycommerce:testId
							code="cart_totalProductPrice_label">
							<format:price priceData="${entry.totalPrice}"
								displayFreeForZero="true" />
						</ycommerce:testId></td> --%>
					
  				<c:if test="${siteUid eq 'personalCare' and not isSalesRepUserLogin}">
                 <c:if test="${enableButton && cartData.enableFloorSpaceGraphics}">
					<td headers="header12" class="doubleStackProduct">
				
					<c:set var="erpMaterialId" value="${entry.product.erpMaterialID}"> </c:set>
					
					<c:set var="contains" value="false" />
                 <c:forEach var="item" items="${cartData.productsNotDoubleStacked}">
                <c:if test="${item.key eq erpMaterialId}">
                   <c:set var="contains" value="true" />
                   
                     </c:if>
                      </c:forEach>
                      
                      <c:choose>
					
					<c:when test="${contains == true}">
					                     
                      <c:choose>
                                            
                         <c:when test="${empty cartData.productsNotDoubleStacked.get(erpMaterialId)}">
                          <spring:theme code="product.doublestacking.notavailable" />
                          </c:when>
                      
                     
                      <c:otherwise><a style="color:#228b22;" href="${getDoubleStackProducts}?productID=${cartData.productsNotDoubleStacked.get(erpMaterialId)}"> <spring:theme code="product.doubleStacked.clickhere" /></a></c:otherwise>
                      
                      </c:choose>
                      					
				        			
						</c:when> 
						<c:otherwise><spring:theme code="product.doublestacking.notrequired" /></c:otherwise>
						</c:choose>
						</td>
						</c:if>
						</c:if>
				<c:if test="${not isSalesRepUserLogin and siteUid eq 'personalCare'}">
					 <td headers="header13" class="agreeEdgewellPrice"> <c:url
							value="/cart/update/agreeEdgewellUnitPrice" var="cartUpdateAgreeEdgewellUnitPriceFormAction" /> <form:form
							id="updateCartAgreeEdgewellUnitPriceForm${entry.entryNumber}"
							action="${cartUpdateAgreeEdgewellUnitPriceFormAction}" method="post"
							modelAttribute="UpdateExpectedUnitPriceForm${entry.entryNumber}">							
							<input type="hidden" name="entryNumber"
								value="${entry.entryNumber}" />
							<input type="hidden" name="productCode" id="productCode_${entry.entryNumber}"
								value="${entry.product.code}" />
							<input type="hidden" name="eachUnitPrice" id="eachUnitPrice_${entry.entryNumber}" value="${entry.eachUnitPrice.value}" />	
							<input type="hidden" name="expectedUnitPrice" id="expectedUnitPrice_${entry.entryNumber}"
								value="${entry.expectedUnitPrice}" />
								
								<%--  <c:choose>
									<c:when test="${empty entry.expectedUnitPrice or entry.expectedUnitPrice eq 0}">
										<c:set var="expectedUnitPrice" value="" />
									</c:when>
									<c:otherwise>
										<c:set var="expectedUnitPrice" value="${entry.expectedUnitPrice}" />
									</c:otherwise>
								</c:choose>  --%>
								
								<input type="hidden" name="initialAgreeEdgewellUnitPrice" id="initialAgreeEdgewellUnitPrice_${entry.entryNumber}"
								value="${entry.agreeEdgewellUnitPrice}" />
								
								<input type="hidden" name="agreeEdgewellPriceForAllProducts" id="agreeEdgewellPriceForAllProducts_${entry.entryNumber}" value="" />
								 
								<%-- ${entry.agreeEdgewellUnitPrice} --%>
								
								<c:set var="isAgreeEdgewellPriceFalse" value="false" />
								<c:forEach var="orderEntryErrorItem" items="${orderEntryDataErrors}">
									<c:if test="${orderEntryErrorItem.product.erpMaterialID eq entry.product.erpMaterialID}">
										<c:set var="isAgreeEdgewellPriceFalse" value="true" />
									</c:if>
								</c:forEach>
									
								<c:choose>
									<c:when test="${entry.agreeEdgewellUnitPrice}">
										<form:checkbox class="agreeEdgewellUnitPrice" checked="checked" id="agreeEdgewellUnitPrice_${entry.entryNumber}" path="agreeEdgewellUnitPrice" />
									</c:when>
									<c:otherwise>
										<c:choose>
											<c:when test="${isAgreeEdgewellPriceFalse eq true}">
												<form:checkbox class="agreeEdgewellUnitPrice highlightCartEntryErrorCheckbox" id="agreeEdgewellUnitPrice_${entry.entryNumber}" path="agreeEdgewellUnitPrice" />													
											</c:when>
											<c:otherwise>
												<form:checkbox class="agreeEdgewellUnitPrice" id="agreeEdgewellUnitPrice_${entry.entryNumber}" path="agreeEdgewellUnitPrice" />
											</c:otherwise>
										</c:choose>
									</c:otherwise>
									<%-- <c:otherwise>
										<form:checkbox class="agreeEdgewellUnitPrice" id="agreeEdgewellUnitPrice_${entry.entryNumber}" path="agreeEdgewellUnitPrice" />
									</c:otherwise> --%>
								</c:choose>
								<%-- <form:checkbox class="agreeEdgewellUnitPrice" checked="checked" id="agreeEdgewellUnitPrice_${entry.entryNumber}" path="agreeEdgewellUnitPrice" /> --%>
							</form:form> 
 					</td>  
					</c:if>
				</tr>

				<c:if test="${entry.product.multidimensional}">
					<tr>
						<th colspan="5"><c:forEach items="${entry.entries}"
								var="currentEntry" varStatus="stat">
								<c:set var="subEntries"
									value="${stat.first ? '' : subEntries}${currentEntry.product.code}:${currentEntry.quantity}," />
							</c:forEach>

							<div style="display: none" id="grid_${entry.product.code}"
								data-sub-entries="${subEntries}"></div></th>
					</tr>
				</c:if>
			</c:forEach>


			<%-- place holder for Container Utilization Start --%>

			<!-- Display Container utilization Floorplan graphics only for LATAM , not for EMEA as of now - START -->
			<%-- <c:if test="${enableButton}"> --%>
				<tr class="cartItem">
					<th colspan="3"><input name="volume_txt" type="hidden"
						id="volume_txt" value="${ cartData.totalProductVolumeInPercent}"><br />
						<input name="weight_txt" type="hidden" id="weight_txt"
						value="${ cartData.totalProductWeightInPercent}"><br />
	                    <input name="availableVolume_txt" type="hidden"
						id="availableVolume_txt" value="${ cartData.availableVolume}"><br />
						<input name="availableWeight_txt" type="hidden"
						id="availableWeight_txt" value="${ cartData.availableWeight}"><br />
						<input name="isContainerFull" type="hidden" id="isContainerFull"
						value="${ cartData.isContainerFull}"><br /></th>
				</tr>
			<%-- </c:if> --%>
			<!-- Display Container utilization Floorplan graphics only for LATAM , not for EMEA as of now  - END -->
			
			<%-- place holder for Container Utilization End --%>

		</tbody>
	</table>

	<product:productOrderFormJQueryTemplates />

</div>

