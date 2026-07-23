<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/desktop/template" %>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/desktop/product" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>


<div id="productTabs">
	<div class="tabHead"><spring:theme code="product.product.details" /></div>
	<div class="tabBody"><product:productDetailsTab product="${product}"/></div>
	<div class="tabHead" id="tab-reviews"><spring:theme code="review.reviews" /></div>
	<div class="tabBody" ><product:productPageReviewsTab product="${product}"/></div>
	<cms:pageSlot position="Tabs" var="tabs">
		<cms:component component="${tabs}"/>
	</cms:pageSlot>
</div>