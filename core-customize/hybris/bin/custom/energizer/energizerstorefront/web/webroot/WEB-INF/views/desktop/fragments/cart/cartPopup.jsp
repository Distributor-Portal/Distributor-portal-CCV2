<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/desktop/template"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme"%>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/desktop/product"%>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format"%>

<script>
	var isSalesRepLoggedInUser = $('#isSalesRepLoggedIn').val();
	var gotPriceFromSAP = $('#gotPriceFromSAP').val();
	var showTaxForSalesRep = $('#showTaxForSalesRep').val();
	if (isSalesRepLoggedInUser == "true" && gotPriceFromSAP == 'false') {
		$('#cartPopUpTotalPrice').remove();
		$('.minicartItemPrice').remove();
		$('.pallet').remove();
		$('.itemThumb').remove();
		$('.minicartItem').removeClass('itemDesc');
		//$('.totalPriceWithTax').remove();
	}
	if (isSalesRepLoggedInUser == "true" && gotPriceFromSAP == 'true') {
		$('.itemThumb').remove();
		//$('.totalPriceWithTax').remove();
	}
	if (isSalesRepLoggedInUser == "true" && gotPriceFromSAP == 'true' && showTaxForSalesRep == 'true') {
		$('#cartPopUpTotalPrice').hide();
		$('.totalPriceWithTax').show();
	}
</script>

<spring:theme code="text.addToCart" var="addToCartText"/>
<spring:theme code="text.popupCartTitle" var="popupCartTitleText"/>
<c:url value="/cart" var="cartUrl"/>
<c:url value="/cart/checkout" var="checkoutUrl"/>
<c:set var="currencyISO" value="${cartData.totalPrice.currencyIso}" />

<c:if test="${numberShowing > 0 }">
	<div class="legend">
		<spring:theme code="popup.cart.showing" arguments="${numberShowing},${numberItemsInCart}"/>
		<c:if test="${numberItemsInCart > numberShowing}">
			<a href="${cartUrl}">Show All</a>
		</c:if>
	</div>
</c:if>


<c:if test="${empty numberItemsInCart or numberItemsInCart eq 0}">
	<div class="cart_modal_popup empty-popup-cart">
		<spring:theme code="popup.cart.empty"/>
	</div>
</c:if>
<c:if test="${numberShowing > 0 }">
	<ul class="itemList">
	<c:forEach items="${entries}" var="entry" end="${numberShowing - 1}">
		<c:url value="${entry.product.url}" var="entryProductUrl"/>
		<li class="popupCartItem">
			<div class="itemThumb">
				<a href="${entryProductUrl}">
					<product:productPrimaryImage product="${entry.product}" format="cartIcon"/>
				</a>
			</div>
			<div class="minicartItem itemDesc">
				<a class="itemName" href="${entryProductUrl}">${entry.product.name}</a>
				<div class="itemQuantity"><span class="label"><spring:theme code="popup.cart.quantity"/>&nbsp;</span>${entry.quantity}</div>
				
				<c:forEach items="${entry.product.baseOptions}" var="baseOptions">
					<c:forEach items="${baseOptions.selected.variantOptionQualifiers}" var="baseOptionQualifier">
						<c:if test="${baseOptionQualifier.qualifier eq 'style' and not empty baseOptionQualifier.image.url}">
							<div class="itemColor">
								<span class="label"><spring:theme code="product.variants.colour"/></span>
								<img src="${baseOptionQualifier.image.url}" alt="${baseOptionQualifier.value}" title="${baseOptionQualifier.value}"/>
							</div>
						</c:if>
						<c:if test="${baseOptionQualifier.qualifier eq 'size'}">
							<div class="itemSize">
								<span class="label"><spring:theme code="product.variants.size"/></span>
								${baseOptionQualifier.value}
							</div>
						</c:if>
					</c:forEach>
				</c:forEach>
				
				<c:if test="${not empty entry.deliveryPointOfService.name}">
					<div class="itemPickup"><span class="itemPickupLabel"><spring:theme code="popup.cart.pickup"/></span>${entry.deliveryPointOfService.name}</div>
				</c:if>
				
				<c:choose>
					<c:when test="${not entry.product.multidimensional or (entry.product.priceRange.minPrice.value eq entry.product.priceRange.maxPrice.value)}" >
						<div class="itemPrice minicartItemPrice">
							<format:price priceData="${entry.basePrice}" isSalesRepUserCart="${cartData.placedBySalesRep}" displayOnlyValue="true" currencyISO="${currencyISO}" cartPopUpItems="true"/>
						</div>
					</c:when>
					<c:otherwise>
						<div class="itemPrice minicartItemPrice">
							<format:price priceData="${entry.product.priceRange.minPrice}"/>
							-
							<format:price priceData="${entry.product.priceRange.maxPrice}"/>
						</div>
					</c:otherwise>
				</c:choose>
			</div>
		</li>
	</c:forEach>
	</ul>
</c:if>

<div  class="total" id="cartPopUpTotalPrice">
	<%-- <c:choose>
		<c:when test="${gotPriceFromSAP}">
			<spring:theme code="popup.cart.total"/>&nbsp;<span class="right"><format:price priceData="${cartData.totalPriceWithTax}"/></span>
		</c:when>
		<c:otherwise> --%>
			<spring:theme code="popup.cart.total"/>&nbsp;<span class="right">
				<format:price priceData="${cartData.totalPrice}" isSalesRepUserCart="${cartData.placedBySalesRep}" displayOnlyValue="true" currencyISO="${currencyISO}" totals="true"/>
			</span>
		<%-- </c:otherwise>
	</c:choose> --%>
</div>

<div class="totalPriceWithTax" style="display: none;border-top: 1px dotted #a5a5a5; font-weight: bold;">
	<spring:theme code="popup.cart.total"/>&nbsp;<span class="right"><format:price priceData="${cartData.totalPriceWithTax}" isSalesRepUserCart="${cartData.placedBySalesRep}" displayOnlyValue="true" currencyISO="${currencyISO}" totals="true"/></span>
</div>

<div  class="banner">
	<c:if test="${not empty lightboxBannerComponent && lightboxBannerComponent.visible}">
			<cms:component component="${lightboxBannerComponent}" evaluateRestriction="true"  />
	</c:if>
</div>
<div class="links" >
	<div class="pallet">
	    <c:if test="${not empty FullPallet}">
	       <spring:theme code="Full Pallet : "/>${FullPallet}<br/>
	    </c:if>
		<c:if test="${not empty MixedPallet}">
	       <spring:theme code="Mixed Pallet : "/>${MixedPallet}<br/>
	    </c:if>
	 </div>
	<a href="${cartUrl}" class="button positive"><spring:theme code="checkout.checkout" /></a>
</div>
