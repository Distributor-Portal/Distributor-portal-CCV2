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

	<nav:accountNav />

	<div class="span-20 last customAccount">
		<cms:pageSlot position="TopContent" var="feature" element="div"
			class="span-20 wide-content-slot cms_disp-img_slot">
			<cms:component component="${feature}" />
		</cms:pageSlot>

		<div class="tile column profile">
			<c:url value="/my-account/profile" var="encodedUrl" />
			<div class="headline">
				<a href="${encodedUrl}"><spring:theme
						code="text.account.profile" text="Profile" /></a>
			</div>
			<ul>
				<ycommerce:testId code="myAccount_options_profile_groupbox">
					<li><a href="${encodedUrl}"><spring:theme
								code="text.account.profile.updatePersonalDetails"
								text="Update personal details" /></a></li>
					
					<c:if test="${not isSalesRepUserLogin}">
						<c:url value="/my-account/update-password" var="encodedUrl" />
						<li><a href="${encodedUrl}"><spring:theme
									code="text.account.profile.changePassword"
									text="Change your password" /></a></li>
					</c:if>
				</ycommerce:testId>
			</ul>
		</div>

		<%-- This section has been commented as per the review comments.
        <sec:authorize ifAllGranted="ROLE_B2BCUSTOMERGROUP">

            <div class="tile column addressBook">
                <c:url value="/my-account/address-book" var="encodedUrl" />
                <div class="headline"><a href="${encodedUrl}"><spring:theme code="text.account.addressBook" text="Address Book"/></a></div>
                <ul>
                    <ycommerce:testId code="myAccount_options_addressBook_groupbox">
                        <li><a href="${encodedUrl}"><spring:theme code="text.account.addressBook.manageDeliveryAddresses" text="Manage your delivery addresses"/></a></li>
                    </ycommerce:testId>
                </ul>
            </div>
            --%>

		<%-- This section has been commented as per the review comments.
            <div class="tile column paymentDetails">
                <c:url value="/my-account/payment-details" var="encodedUrl" />
                <div class="headline"><a href="${encodedUrl}"><spring:theme code="text.account.paymentDetails" text="Payment Details"/></a></div>
                <ul>
                    <ycommerce:testId code="myAccount_options_paymentDetails_groupbox">
                        <li><a href="${encodedUrl}"><spring:theme code="text.account.paymentDetails.managePaymentDetails" text="Manage your payment details"/></a></li>
                    </ycommerce:testId>
                </ul>
            </div>
            --%>
		<%--    
         	This section has been commented due to not in scope.
         <div class="tile column quotes-dasshboard">
                <c:url value="/my-account/my-quotes" var="encodedUrl" />
                <div class="headline"><a href="${encodedUrl}"><spring:theme code="text.account.quotes" text="Quotes"/></a></div>
                <ul>
                    <ycommerce:testId code="myAccount_options_paymentDetails_groupbox">
                        <li><a href="${encodedUrl}"><spring:theme code="text.account.viewQuotes" text="View my quotes"/></a></li>
                    </ycommerce:testId>
                </ul>
            </div> --%>
		<%-- </sec:authorize> --%>

		<%-- <sec:authorize ifAnyGranted="ROLE_B2BCUSTOMERGROUP,ROLE_B2BADMINGROUP, ROLE_EMPLOYEEGROUP"> --%>
		<sec:authorize access="hasAnyRole('ROLE_B2BCUSTOMERGROUP','ROLE_B2BADMINGROUP','ROLE_EMPLOYEEGROUP')">
			<div class="tile column QuickOrders">
				<c:url value="/my-account/quickorder" var="encodedUrl" />
				<div class="headline">
					<a href="${encodedUrl}"> <spring:theme
							code="text.account.profile.QuickOrders" text="Quick Order" />
					</a>
				</div>
				<ul>
					<ycommerce:testId code="myAccount_options_profile_groupbox">
						<li><a href="${encodedUrl}"> <spring:theme
									code="text.account.profile.QuickOrderForm"
									text="Quick Order Form" />
						</a></li>
					</ycommerce:testId>
				</ul>
			</div>
			<div class="tile column FileUpload">
				<c:url value="/my-account/excelfileupload" var="encodedUrl" />
				<div class="headline">
					<a href="${encodedUrl}"> <spring:theme
							code="text.account.profile.QuickOrders" text="Excel Order" />
					</a>
				</div>
				<ul>
					<ycommerce:testId code="myAccount_options_profile_groupbox">
						<c:url value="/my-account/excelfileupload" var="encodedUrl" />
						<li><a href="${encodedUrl}"> <spring:theme
									code="text.account.excelUpload.FileUpload" text="Excel Order" />
						</a></li>
					</ycommerce:testId>
				</ul>
			</div>
			
			<div class="tile column CatalogDownload">
				<c:url value="/my_account/catalogDownload" var="encodedUrl" />
				<div class="headline">
					<a href="${encodedUrl}"> <spring:theme
							code="text.account.profile.CatalogDownload" text="Catalog Download" />
					</a>
				</div>
				<ul>
					<ycommerce:testId code="myAccount_options_profile_groupbox">
						<c:url value="/my_account/catalogDownload" var="encodedUrl" />
						<li><a href="${encodedUrl}"> <spring:theme
									code="text.account.catalogDownload" text="Catalog Download" />
						</a></li>
					</ycommerce:testId>
				</ul>
				
			</div>
		</sec:authorize>
		<%--
		Order history is requried for everyone in the group/unit 
		<sec:authorize ifAnyGranted="ROLE_B2BCUSTOMERGROUP,ROLE_B2BADMINGROUP,ROLE_B2BAPPROVERGROUP,ROLE_B2BVIEWERGROUP">
            --%>
		<div class="tile column orderHistory">
			<c:url value="/my-account/orders" var="encodedUrl" />
			<div class="headline">
				<a href="${encodedUrl}"><spring:theme
						code="text.account.orderHistory" text="Order History" /></a>
			</div>
			<ul>
				<ycommerce:testId code="myAccount_options_orderHistory_groupbox">
					<li><a href="${encodedUrl}"><spring:theme
								code="text.account.viewOrderHistory" text="View order history" /></a></li>
					<%--
                       This  replenishment link has been commented. as of this is not in scope - selva
                       <c:url value="/my-account/my-replenishment" var="encodedUrl" />
                        <li><a href="${encodedUrl}"><spring:theme code="text.account.myReplenishment" text="Change your password"/></a></li> --%>
				</ycommerce:testId>
			</ul>
		</div>
		<%-- </sec:authorize>
 --%>
 		<%-- <sec:authorize ifAnyGranted="ROLE_B2BCUSTOMERGROUP,ROLE_B2BADMINGROUP, ROLE_EMPLOYEEGROUP"> --%>
 		<sec:authorize access="hasAnyRole('ROLE_B2BCUSTOMERGROUP','ROLE_B2BADMINGROUP','ROLE_EMPLOYEEGROUP')">

 			<div class="tile column CatalogDownload">
				<c:url value="/my_account/orderHistoryDownload" var="encodedUrl" />
				<div class="headline">
					<a href="${encodedUrl}"> <spring:theme
							code="text.account.profile.orderHistoryDownload" text="Download OrderHistory" />
					</a>
				</div>
				<ul>
					<ycommerce:testId code="myAccount_options_profile_groupbox">
						<c:url value="/my_account/orderHistoryDownload" var="encodedUrl" />
						<li><a href="${encodedUrl}"> <spring:theme
									code="text.account.profile.orderHistoryDownload" text="Download Order History" />
						</a></li>
					</ycommerce:testId>
				</ul>
				
			</div>
		</sec:authorize>	
		
		<%-- <sec:authorize ifAnyGranted="ROLE_B2BAPPROVERGROUP,ROLE_B2BADMINGROUP"> --%>
		<sec:authorize access="hasAnyRole('ROLE_B2BAPPROVERGROUP','ROLE_B2BADMINGROUP')">
			<div class="tile column approval-dashboard">
				<c:url value="/my-account/approval-dashboard" var="encodedUrl" />
				<div class="headline">
					<a href="${encodedUrl}"><spring:theme
							code="text.account.orderApproval" text="Order Approval" /></a>
				</div>
				<ul>
					<ycommerce:testId code="myAccount_options_orderApproval_groupbox">
						<li><a href="${encodedUrl}"><spring:theme
									code="text.account.viewOrderApproval"
									text="View orders that require approval" /></a></li>
					</ycommerce:testId>
				</ul>
			</div>
		</sec:authorize>
	</div>
</template:page>
