<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="selected" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<div class="span-4 footerNavColumn">
	<div class="span-4 nav_column">
		<%-- <div class="title_holder">
			<div class="title">
				<div class="title-top">
					<span></span>
				</div>
			</div>
			<div class="accountNav">
			<div class="headline"><spring:theme code="text.account.aboutUs" text="About Us"/></div></div>
		</div> --%>
		<div class="item footerNav">
			<ul class="facet_block indent">
				<c:forEach items="${footerLinksList}" var="footerLink">
					<c:set var="selectedClass" value="" />
					<c:if test="${footerLink eq currentFooterPage}">	
						<c:set var="selectedClass" value="nav_selected" />
					</c:if>
					<li class='${selectedClass}'>
						<c:url value="/content/${footerLink}" var="encodedUrl" />
						<ycommerce:testId code="myAccount_profile_navLink">
							<c:choose>
								<c:when test="${footerLink eq 'aboutus'}">
									<a href="${aboutUsURL}" target="_blank"><spring:theme code="header.link.${footerLink}" text="header.link.${footerLink}"/></a>
								</c:when>
								<c:when test="${footerLink eq 'legal'}">
									<a href="${legalURL}" target="_blank"><spring:theme code="header.link.${footerLink}" text="header.link.${footerLink}"/></a>
								</c:when>
								<c:otherwise>
									<a href="${encodedUrl}"><spring:theme code="header.link.${footerLink}" text="header.link.${footerLink}"/></a>
								</c:otherwise>
							</c:choose>
						</ycommerce:testId>
					</li>
				</c:forEach>
				<!-- End -->
			</ul>
		</div>
	</div>
	<cms:pageSlot position="SideContent" var="feature" element="div" class="span-4 side-content-slot cms_disp-img_slot">
		<cms:component component="${feature}"/>
	</cms:pageSlot>	
</div>
