<%@ page trimDirectiveWhitespaces="true" contentType="application/json" %>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/desktop/template" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format" %>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/desktop/product" %>
<%@ taglib prefix="json" uri="jakarta.tags.core" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/desktop/common" %>
<%@ taglib prefix="grid" tagdir="/WEB-INF/tags/desktop/grid" %>

<c:set var="skuIndex" scope="session" value="${skuIndex}" />

{
	"pagination" : {
		"currentPage":	"${searchResultsData.pagination.currentPage}",
		"numberOfPages": "${searchResultsData.pagination.numberOfPages}",
		"totalNumberOfResults" : "${searchResultsData.pagination.totalNumberOfResults}",
		"searchResultsType" : "${searchResultType}"
	},
	"cartGlobalMessagesHtml" : "<spring:escapeBody javaScriptEscape="true"><common:globalMessages/></spring:escapeBody>"
	,
			
	"productListerHtml":  "<spring:escapeBody javaScriptEscape="true">																			
										<c:forEach items="${searchResultsData.results}" var="product" varStatus="status">
											<c:choose>
												<c:when test="${not empty product.variantMatrix && isOrderForm}">
													<product:productListerOrderForm product="${product}" />
												</c:when>
												<c:when test="${searchResultType eq 'create-order-form'}">
													<product:productFilterOrderForm product="${product}" />
												</c:when>
												<c:otherwise>
													<product:productListerItem product="${product}" isOrderForm="${isOrderForm}"/>
												</c:otherwise>
											</c:choose>
										</c:forEach>
						  </spring:escapeBody>",
	"skuIndex" : "${sessionScope.skuIndex}"
}