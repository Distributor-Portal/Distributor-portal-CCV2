<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="hideHeaderLinks" required="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="header" tagdir="/WEB-INF/tags/desktop/common/header"  %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>

<%-- Test if the UiExperience is currently overriden and we should show the UiExperience prompt --%>
<c:if test="${uiExperienceOverride and not sessionScope.hideUiExperienceLevelOverridePrompt}">
	<c:url value="/_s/ui-experience?level=" var="clearUiExperienceLevelOverrideUrl"/>
	<c:url value="/_s/ui-experience-level-prompt?hide=true" var="stayOnDesktopStoreUrl"/>
	<div class="backToMobileStore">
		<a href="${clearUiExperienceLevelOverrideUrl}"><span class="greyDot">&lt;</span><spring:theme code="text.swithToMobileStore" /></a>
		<span class="greyDot closeDot"><a href="${stayOnDesktopStoreUrl}">x</a></span>
	</div>
</c:if>

<input type="hidden" id="siteUid" value="${siteUid}" />
<input type="hidden" id="isSalesRepLoggedIn" value="${isSalesRepUserLogin}"/>
<input type="hidden" id="gotPriceFromSAP" value="${gotPriceFromSAP}"/>
<input type="hidden" id="showTaxForSalesRep" value="${showTaxForSalesRep}">
<%-- <c:set var="isSalesRepLoggedIn" value="${isSalesRepUserLogin}"/> --%>

<div id="header" class="clearfix">
	<cms:pageSlot position="TopHeaderSlot" var="component">
		<cms:component component="${component}"/>
	</cms:pageSlot>
	
	<div class="headerContent ">
		<ul class="nav clearfix">
			<cms:pageSlot position="HeaderLinks" var="links">
				<cms:component component="${links}"/>
			</cms:pageSlot>

			<cms:pageSlot position="MiniCart" var="cart" limit="1">
				<cms:component component="${cart}" element="li" class="miniCart" />
			</cms:pageSlot>
		</ul>
	</div>

	<cms:pageSlot position="SearchBox" var="component" element="div" class="headerContent secondRow">
		<cms:component component="${component}" element="div" />
	</cms:pageSlot>
	
	
	<%-- <c:if test="${contentPageId ne null and contentPageId eq 'login'}" >
		<cms:pageSlot position="csn_RegionBasedSiteDropdownSelector" var="feature">
			<cms:component component="${feature}" />
		</cms:pageSlot>
	</c:if> --%>
	
	<!-- Display the Region Based Site Selector Dropdown only for Login Page - START -->
	<c:if test="${contentPageId ne null and contentPageId eq 'login'}" >
		<form id="redirectSiteURLForm" action="/login/redirectSiteURL" method="post" >
			<input type="hidden" id="siteId" name="siteId" value="" />
			<input type="hidden" id="currency" name="currency" value="" />
			<input type="hidden" id="region" name="region" value="" />
			<input type="hidden" id="loginUrl" name="loginUrl" value="/login" />
		</form>
		
		<div class="headerContent secondRow">
			<span class="select-region">
				<b><spring:theme code="login.select.region"/></b>
			</span>
			<br/>
			<select id="siteIdentifier" class="regionSiteSelectionDropdown">
			  	<c:choose>
				  <c:when test="${siteUid eq 'personalCare'}" >
				  	<option value="personalCare_USD" selected="selected">LATAM</option>
				  </c:when>
				  <c:otherwise>
				  	<option value="personalCare_USD">LATAM</option>
				  </c:otherwise>
				</c:choose>
			 	<c:choose>
			 		<c:when test="${siteUid eq 'personalCareEMEA'}" >
			 			<option value="personalCareEMEA_EUR" selected="selected">EMEA</option>
			 		</c:when>
			 		<c:otherwise>
			 			<option value="personalCareEMEA_EUR">EMEA</option>
			 		</c:otherwise>	
			 	</c:choose>  	
			</select>
		</div>
	</c:if>
	<!-- Display the Region Based Site Selector Dropdown only for Login Page - END -->
		
	<cms:pageSlot position="SiteLogo" var="logo" limit="1">
		<cms:component component="${logo}" class="siteLogo"  element="div"/>
	</cms:pageSlot>
</div>
