<%@ tag body-content="empty" trimDirectiveWhitespaces="true"%>
<%@ attribute name="product" required="true"
	type="de.hybris.platform.commercefacades.product.data.ProductData"%>
<%@ attribute name="galleryImages" required="true" type="java.util.List"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/desktop/template"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme"%>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format"%>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags"%>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/desktop/product"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<spring:theme code="text.addToCart" var="addToCartText" />
	<div class="alert negative" id="showErrorMessageForaddToCart" style="display: none; border-color: #c90400;color: #c90400;">
			<spring:theme code="${fn:escapeXml('addtocart.page.error.message')}"/>
	</div>
<div class="productDetailsPanel">
<input id="errorMsg" type="hidden" value="<spring:theme code="product.uom.addtocart.error" />">
<input id="emptyUomErrorMsg" type="hidden" value="<spring:theme code="product.uom.notselected.addtocart.error" />">
<product:productImagePanel product="${product}"
			galleryImages="${galleryImages}" />
			
<c:choose>
	<c:when test="${not isSalesRepUserLogin}">
	<!--  Non Sales rep user - so no change in the alignment -->
		<div class="span-10 productDescription last">
	</c:when>
	<c:otherwise>
	<!-- Removing the image for WeSell, so aligning it to float right -->
		<div class="span-10 productDescription last" style="float: right;">
	</c:otherwise>
</c:choose>			

	
	  <c:if test="${not isSalesRepUserLogin}">
		<c:choose>
			<c:when test="${ empty product.customerProductPrice }">
				<ycommerce:testId
					code="productDetails_productNamePrice_label_${product.erpMaterialID}">
					<product:productPricePanel product="${product}" table="false" />
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
				</ycommerce:testId>
			</c:when>
			<c:otherwise>
				<%--   Energizer customer Price --%>
				<p class="big-price right">${product.customerPriceCurrency}
					${product.customerProductPrice}
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

		<ycommerce:testId
			code="productDetails_productNamePrice_label_${product.code}">
			<h1>${fn:escapeXml(product.name)}</h1>
			<c:if test="${siteUid eq 'personalCare'}">
				<input type="hidden" id="isPBG_${product.code}" value="${product.isPBG}" />
				<c:if test="${product.isPBG}">
					<div class="pbgWrapper" title="This is a PBG Product" style="width: 50px !important;">
						<a href="#">
							<span class="pbgLabel" style="vertical-align: middle;font-size: 1em;">PBG</span>
						</a>
					</div>
				</c:if>
			</c:if>
		</ycommerce:testId>

		<%-- product:productReviewSummary product="${product}"/--%>


		<div class="summary">
			${fn:escapeXml(product.summary)}<br>
			<spring:theme code="basket.page.MaterialId" />
			: ${fn:escapeXml(product.code)}<br>
			<spring:theme code="basket.page.customerMaterialId" />
			: ${fn:escapeXml(product.customerMaterialId)}<br>
			<c:choose>
				<c:when test="${siteUid eq 'personalCare'}">
					<spring:theme code="basket.page.customerProductName" />
					: ${fn:escapeXml(product.customerProductName)}<br>
				</c:when>
				<c:otherwise>
					<c:set value="${fn:trim(product.customerProductName)}" var="customerProductName" />
					<c:choose>
						<c:when test="${not empty product.customerProductName and (fn:length(customerProductName) gt 0)}">
							<spring:theme code="basket.page.customerProductName" />
							: ${fn:escapeXml(product.customerProductName)}<br>
						</c:when>
						<c:otherwise>
						</c:otherwise>
					</c:choose>
				</c:otherwise>
			</c:choose>
			<c:if test="${not empty product.ean}">
				<spring:theme code="basket.page.ean" />
				: ${fn:escapeXml(product.ean)}<br>
			</c:if>
			<spring:theme code="basket.page.shipFrom" />
			: ${fn:escapeXml( product.shippingPointName)} <br>
			<c:if test="${siteUid ne 'personalCare'}">
			<spring:theme code="basket.page.shipFromLocation"/>:${fn:escapeXml(product.shippingPointLocation)}<br>
			<spring:theme code="basket.page.moq" />
			: ${fn:escapeXml(product.moq)}<br>
			<spring:theme code="basket.page.uom" />
			: ${fn:escapeXml(product.uom)}<br>
			<spring:theme code="basket.page.baseuom.convertion" />
			: ${fn:escapeXml(product.baseUOM)}<br>
			</c:if>
			<c:if test="${not isSalesRepUserLogin and not product.isWeSellProduct}">
				<spring:theme code="basket.page.layersPerPallet"/> : ${fn:escapeXml(product.numberOfLayersPerPallet)}<br>
				<spring:theme code="basket.page.casesPerPallet" /> : ${fn:escapeXml(product.numberOfCasesPerPallet)}<br>
				<spring:theme code="basket.page.casesPerLayer" />  : ${fn:escapeXml(product.numberOfCasesPerLayer)}<br>
			</c:if>
			<c:if test="${siteUid eq 'personalCareEMEA' and not empty product.numberOfCUsPerSU and not empty product.numberOfSUsPerPallet}">
				<spring:theme code="basket.page.CUsPerSU" />   : ${fn:escapeXml(product.numberOfCUsPerSU)}<br>
				<spring:theme code="basket.page.SUsPerPallet" /> : ${fn:escapeXml(product.numberOfSUsPerPallet)}<br>
			</c:if>
			
			<%-- <spring:theme code="basket.page.segmentName" />
			: ${fn:escapeXml(product.segmentName)}<br>
			<spring:theme code="basket.page.familyName" />
			: ${fn:escapeXml(product.familyName)}<br>
			<spring:theme code="basket.page.groupName" />
			: ${fn:escapeXml(product.groupName)}<br> --%>
			<%-- 
			spring:theme code="basket.page.Weight"/> : ${fn:escapeXml(product.weight)}<br>
			<spring:theme code="basket.page.weightUom"/> : ${fn:escapeXml(product.weightUom)}<br>
			<spring:theme code="basket.page.volume"/> : ${fn:escapeXml(product.volume)}<br>
			<spring:theme code="basket.page.volumeUom"/> : ${fn:escapeXml(product.volumeUom)}<br>
			--%>
			<%-- obsolete : ${product.obsolete}<br>	  --%>

		</div>

		<product:productPromotionSection product="${product}" />

		<cms:pageSlot position="VariantSelector" var="component" element="div">
			<cms:component component="${component}" />
		</cms:pageSlot>
		<!-- Added Code changes for WeSell Implementation - START -->
		<c:if test="${not isB2BViewergroup}">
		<!-- Added Code changes for WeSell Implementation -END -->
		<%-- <sec:authorize ifAnyGranted="ROLE_B2BCUSTOMERGROUP,ROLE_B2BADMINGROUP ,ROLE_EMPLOYEEGROUP"> --%>
		<sec:authorize access="hasAnyRole('ROLE_B2BCUSTOMERGROUP','ROLE_B2BADMINGROUP','ROLE_EMPLOYEEGROUP')">
			<cms:pageSlot position="AddToCart" var="component" element="div">
				<cms:component component="${component}" />
			</cms:pageSlot>
		</sec:authorize>
		<!-- Added Code changes for WeSell Implementation - START -->
		</c:if>
		<!-- Added Code changes for WeSell Implementation - END -->
		<%-- <product:productShareTag/> --%>
	</div>

	<cms:pageSlot position="Section2" var="feature" element="div"
		class="span-8 section2 cms_disp-img_slot last">
		<cms:component component="${feature}" />
	</cms:pageSlot>
</div>
