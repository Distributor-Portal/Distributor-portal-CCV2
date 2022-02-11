<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="product" required="true" type="de.hybris.platform.commercefacades.product.data.ProductData" %>
<%@ attribute name="allowAddToCart" required="true" type="java.lang.Boolean" %>
<%@ attribute name="isMain" required="true" type="java.lang.Boolean" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/desktop/product" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

 <form name="selecteduomform_${product.code }" id="selectedUomForm" method="POST" action="/validateUOM">
			<input type="hidden" id="productCodePDP" name="productCode" value="" />
			<input type="hidden" id="selectedUomPDP" name="uom" value="" />
			<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
</form>

<form name="validatePBGForm_${product.code}" id="validatePBGForm_${product.code}" method="POST" action="/validatePBG">
	<input type="hidden" id="productCode_${product.code }" name="productCode" value="" />
	<input type="hidden" id="isPBGPDP_${product.code}" name="isPBG" value="" />
</form>

<input type="hidden" id="siteIdInPDP" value="${siteUid}" /> 
<div class="qty">
	<c:if test="${product.purchasable}">
		<label for="qtyInput"><spring:theme code="basket.page.quantity"/></label>
		<!-- Added for Ordering quantity limit to 7 digits for EMEA - START -->
		<c:choose>
			<c:when test="${siteUid eq 'personalCareEMEA'}">
				<input type="text" maxlength="7" size="3" id="qtyInput" name="qtyInput" class="qty" value="${fn:escapeXml(product.moq)}">&nbsp; ${fn:escapeXml(product.uom)}
			</c:when>
		<c:otherwise>
			<input type="text" maxlength="5" style="width: 14%;" id="qtyInput" name="qtyInput" class="qty" value="${fn:escapeXml(product.moq)}">&nbsp; 
						<%-- ${fn:escapeXml(product.uom)} --%>
               <div class="uomOuter">
                <label for="uomlists">UOM</label>
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
						<input type="text" maxlength="5" style="width:14%;"  id="uomSelector_${product.code}" name="uom" class="uom" value="${fn:escapeXml(product.uom)}" readonly="readonly" disabled="disabled">
					</c:otherwise>
		     	</c:choose>
               </div>   
		</c:otherwise>
		</c:choose>   
		<!-- Added for Ordering quantity limit to 7 digits for EMEA - END -->
	</c:if>

	<c:if test="${product.stock.stockLevel gt 0}">
		<c:set var="productStockLevel">${product.stock.stockLevel}&nbsp;<spring:theme code="product.variants.in.stock"/></c:set>
	</c:if>
	<c:if test="${product.stock.stockLevelStatus.code eq 'inStock' and empty product.stock.stockLevel}">
		<c:set var="productStockLevel"><spring:theme code="product.variants.available"/></c:set>
	</c:if>

	<ycommerce:testId code="productDetails_productInStock_label">
		<p class="stock_message">${productStockLevel}</p>
	</ycommerce:testId>
</div>

<product:productFutureAvailability product="${product}" futureStockEnabled="${futureStockEnabled}" />

<div class="productAddToCartPanel clearfix">
<div style="color: red;margin-top: 5px" class="errormsgclass" id="error${product.code}" ></div>
	<div class="obsolete_error">
            	   <c:if test="${product.obsolete}">            	   		
            	   		<spring:theme code="basket.page.obsolete.error"/>
	               </c:if>            	
     </div>
     <div class="moq_error">          	
            		<c:if test="${(product.moq == 0) || (empty product.moq)}">
	               		<spring:theme code="basket.page.moq.notexists"/>
	               	</c:if>
	 </div>    	
	           
	           
	<c:if test="${multiDimensionalProduct}" >
					<%-- <sec:authorize ifAnyGranted="ROLE_CUSTOMERGROUP"> --%>
					<sec:authorize access="hasAnyRole('ROLE_CUSTOMERGROUP')">
						<c:url value="${product.url}/orderForm" var="productOrderFormUrl"/>
						<a href="${productOrderFormUrl}" class="button negative" id="productOrderButton" ><spring:theme code="order.form" /></a>
					</sec:authorize>
				</c:if>
	<form id="addToCartForm" class="add_to_cart_form" action_data="<c:url value="/cart/add"/>" method="post">
	<c:if test="${product.purchasable}">
		<input type="hidden" maxlength="3" size="1" id="qty" name="qty" class="qty" value="${fn:escapeXml(product.moq)}"/>
	</c:if>
	<input type="hidden" id="selectedUom_${product.code}" name="UOM" value="" />
	<input type="hidden" name="productCodePost" value="${product.code}"/>

	<c:if test="${allowAddToCart}">
		<c:set var="buttonType">button</c:set>

		<c:if test="${product.purchasable and product.stock.stockLevelStatus.code ne 'outOfStock' }">
			<c:set var="buttonType">submit</c:set>
		</c:if>

		<c:choose>
			<c:when test="${fn:contains(buttonType, 'button')}">
				<button id="${product.code}" type="${buttonType}" class="addToCartButton addToCartButtonPDP outOfStock" disabled="disabled">
					<spring:theme code="product.variants.out.of.stock"/>
				</button>
			</c:when>
			<c:when test="${product.obsolete eq 'true'}">
				<button id="${product.code}"  type="button"  disabled="disabled" >
					<spring:theme code="product.variants.obsolete"/>
				</button>			
			</c:when>	
			<c:when test="${product.moq eq '0'}">
				<button id="${product.code}"  type="button"  disabled="disabled" >
					<spring:theme code="basket.add.to.basket"/>
				</button>			
			</c:when>
			<c:when test="${empty product.uom }">
				<button id="${product.code}"  type="button"  disabled="disabled" >
					<spring:theme code="product.variants.out.of.stock"/>
				</button>			
			</c:when>					
			<c:otherwise>
				<button id="${product.code}" type="${buttonType}" class="addToCartButton addToCartButtonPDP display_none">
					<spring:theme code="basket.add.to.basket"/>
				</button>
			</c:otherwise>
		</c:choose>
	</c:if>
	</form>

</div>
