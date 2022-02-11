<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="priceData" required="true" type="de.hybris.platform.commercefacades.product.data.PriceData" %>
<%@ attribute name="displayFreeForZero" required="false" type="java.lang.Boolean" %>
<%@ attribute name="displayOnlyValue" required="false" type="java.lang.Boolean" %>
<%@ attribute name="currencyISO" required="false" type="java.lang.String" %>
<%@ attribute name="totals" required="false" type="java.lang.Boolean" %>
<%@ attribute name="cartPopUpItems" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<%@ attribute name="isSalesRepUserCart" required="false" type="java.lang.Boolean" %>
<%--
 Tag to render a currency formatted price.
 Includes the currency symbol for the specific currency.
--%>

<c:choose>
	<c:when test="${not isSalesRepUserLogin and not isSalesRepUserCart and priceData.value > 0}">
		${priceData.formattedValue}
	</c:when>
	<c:when test="${isSalesRepUserLogin and (empty priceData || empty priceData.value || priceData.value <= 0)}">
		<spring:theme code="wesell.price.zero.text"/>
	</c:when>
	<c:when test="${(isSalesRepUserLogin or (not empty isSalesRepUserCart and isSalesRepUserCart)) and priceData.value > 0 and displayOnlyValue and not empty currencyISO}">
		<c:set var="originalFormattedVal" value="${priceData.formattedValue}" /> 
		<c:set var="priceValue" value="" />
		<c:set var="currencyIsoPriceValue" value="" />
		<c:choose>
			<c:when test="${currencyISO ne 'USD' and totals}">
			<!-- cart/checkout/order confirmation page - total/subtotal price currency other than USD  -->
				<c:choose>
					<c:when test="${fn:contains(originalFormattedVal, currencyISO)}">
						<c:set var="priceValue" value="${fn:replace(originalFormattedVal, currencyISO, '')}" />
						<c:set var="currencyIsoPriceValue" value="${currencyISO}&nbsp;${priceValue}" />
						 ${currencyIsoPriceValue}
					</c:when>
					<c:otherwise>
						${originalFormattedVal}
					</c:otherwise>
				</c:choose>
			</c:when>
			<c:when test="${currencyISO eq 'USD' and (empty totals or not totals) and empty cartPopUpItems}">
				<!-- miniCartPopUp Item level price with currency  USD  -->
				<c:set var="priceValue" value="${fn:replace(originalFormattedVal, '$', '')}" />
				${priceValue}
			</c:when>
			<c:when test="${currencyISO eq 'USD' and totals}">
			<!-- cart/checkout/order confirmation page - total/subtotal price with currency USD  -->
				<c:set var="priceValue" value="${fn:replace(originalFormattedVal, '$', '')}" />
				<c:set var="currencyIsoPriceValue" value="${currencyISO}&nbsp;${priceValue}" />
				${currencyIsoPriceValue}
			</c:when>
			<c:when test="${currencyISO eq 'USD' and cartPopUpItems}">
			<!-- miniCartPopUp entry level price with currency USD  -->
				<c:set var="priceValue" value="${fn:replace(originalFormattedVal, '$', '')}" />
				<c:set var="currencyIsoPriceValue" value="${currencyISO}&nbsp;${priceValue}" />
				 ${currencyIsoPriceValue}
			</c:when>
			<c:when test="${currencyISO eq 'USD' and (empty totals or not totals) and (not empty cartPopUpItems and cartPopUpItems)}">
				<!-- miniCartPopUp entry level price with currency  USD  -->
				<c:set var="priceValue" value="${fn:replace(originalFormattedVal, '$', '')}" />
				<c:set var="currencyIsoPriceValue" value="${currencyISO}&nbsp;${priceValue}" />
				${currencyIsoPriceValue}
			</c:when>
			<c:when test="${currencyISO ne 'USD' and (empty totals or not totals) and (not empty cartPopUpItems and cartPopUpItems)}">
				<!-- miniCartPopUp entry level price currency other than USD  -->
				 <c:choose>
					<c:when test="${fn:contains(originalFormattedVal, currencyISO)}">
						<c:set var="priceValue" value="${fn:replace(originalFormattedVal, currencyISO, '')}" />
						<c:set var="currencyIsoPriceValue" value="${currencyISO}&nbsp;${priceValue}" />
						 ${currencyIsoPriceValue}
					</c:when>
					<c:otherwise>
						${originalFormattedVal}
					</c:otherwise>
				</c:choose>
			</c:when>
			<c:when test="${fn:contains(originalFormattedVal, currencyISO)}">
			<!-- cart/checkout/order confirmation page - item level price currency other than USD  -->
				${fn:replace(originalFormattedVal, currencyISO, '')}
			</c:when>
			<c:otherwise>
				${originalFormattedVal}
			</c:otherwise>
		</c:choose>
	</c:when>
	<c:otherwise>
		<c:if test="${displayFreeForZero}">
			<spring:theme code="text.free" text="FREE"/>
		</c:if>
		<c:if test="${not displayFreeForZero}">
			${priceData.formattedValue}
		</c:if>
	</c:otherwise>
</c:choose>
