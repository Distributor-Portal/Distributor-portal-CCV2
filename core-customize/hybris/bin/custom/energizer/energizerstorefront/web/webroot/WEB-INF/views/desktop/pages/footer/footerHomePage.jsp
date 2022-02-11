<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/desktop/template"%>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme"%>
<%@ taglib prefix="nav" tagdir="/WEB-INF/tags/desktop/nav"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags"%>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/desktop/common"%>
<%@ taglib prefix="breadcrumb"
	tagdir="/WEB-INF/tags/desktop/nav/breadcrumb"%>
<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>

<template:page pageTitle="${pageTitle}">

	<div id="breadcrumb" class="breadcrumb">
		<breadcrumb:breadcrumb breadcrumbs="${breadcrumbs}" />
	</div>
	<div id="globalMessages">
		<common:globalMessages />
	</div>

	<nav:footerNav />

	<div class="span-20 last customAccount">
		<cms:pageSlot position="TopContent" var="feature" element="div"
			class="span-20 wide-content-slot cms_disp-img_slot">
			<cms:component component="${feature}" />
		</cms:pageSlot>
				
		<c:if test="${not empty currentFooterPage}" >
			<cms:pageSlot position="${currentFooterPage}Content" var="feature" element="div"
				class="span-20 wide-content-slot cms_disp-img_slot">
				<cms:component component="${feature}" />
			</cms:pageSlot>	
		</c:if>
	</div>
	
</template:page>
