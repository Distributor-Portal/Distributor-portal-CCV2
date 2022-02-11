<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="actionNameKey" required="true" type="java.lang.String" %>
<%@ attribute name="action" required="true" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/desktop/formElement" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="cart" tagdir="/WEB-INF/tags/desktop/cart" %>
 <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="userLogin">
<spring:url value="/login/pw/request-page"
            var="forgotPassworkdRequestPage"/>
<spring:url value="/login/pw/unlock-account"
            var="unlockAccountRequestPage"/>
	<form:form action="${action}" method="post" modelAttribute="loginForm">
		<c:if test="${not empty message}">
			<span class="errors">
				<spring:theme code="${message}"/>
			</span>
		</c:if>
		<c:if test="${loginError}">
			<div class="form_field_error">
		</c:if>
		<input type="hidden" id="isSelectedDistributor" value="${distributorError}">
		<div class="form_field-elements">
			<formElement:formInputBox idKey="j_username" labelKey="login.username" path="j_username" inputCSS="text" mandatory="true"/>
			<formElement:formPasswordBox idKey="j_password" labelKey="login.password" path="j_password" inputCSS="text password" mandatory="true"/>
			
			<!-- Added SalesRepUser Login for WeSell Implementation - START-->
			<div class="dropdown" style="display: none; margin-top: 20px!important;">
				<label class="control-label">
					<spring:theme code="login.customer"/>&nbsp;<span class="mandatory"><img width="5" height="6" alt="Required" style="vertical-align: top;" title="Required" src="/_ui/desktop/common/images/mandatory.gif"></span>
				</label>
				<form:select id="b2bUnitList" style="width: 355.5px; height: 30px; margin-top: 5px;" path="" >
				</form:select>
			</div>
			<div class="changedB2BunitDropdown">
				<c:if test="${not empty distributorMap}">
				<label class="control-label">
						<spring:theme code="login.customer"/>&nbsp;<span class="mandatory"><img width="5" height="6" alt="Required" style="vertical-align: top;" title="Required" src="/_ui/desktop/common/images/mandatory.gif"></span>
				</label>
				 <form:select id="changeB2BUnit" style="width: 355.5px; height: 30px; margin-top: 5px;" path="">
				   <form:option value="" style="text-align: center;" disabled="disabled">----- <spring:theme code="login.wesell.select.your.customer" /> -----</form:option>
				   <c:forEach items="${distributorMap}" var="entry">
				   		 <form:option value="${entry.value}">${entry.key}</form:option>  
				   </c:forEach>
				</form:select> 
				</c:if>
			</div>
			<!-- Added SalesRepUser Login for WeSell Implementation - END-->
			<%-- <c:if test="${empty FAILED_MAX_ATTEMPTS_TO_LOGIN}"> --%>
			<div class="form_field_error-message">
				<a class="password-forgotten" href="${forgotPassworkdRequestPage}"><spring:theme code="login.link.forgottenPwd"/></a>
			</div>
			<br/>
			<span>
				<a href="${unlockAccountRequestPage}" class="unlock-account" style="text-decoration: underline;"><spring:theme code="login.link.unlock.customer.account"/></a>
			</span>
			<%-- </c:if> --%>
			
		</div>
		<c:if test="${loginError}">
			</div>
		</c:if>
		<c:if test="${expressCheckoutAllowed}">
				<div class="expressCheckoutLogin">
					<div class="headline"><spring:theme text="Express Checkout" code="text.expresscheckout.header"/></div>

					<div class="description"><spring:theme text="Benefit from a faster checkout by:" code="text.expresscheckout.title"/></div>

					<ul>
						<li><spring:theme text="setting a default Delivery Address in your account" code="text.expresscheckout.line1"/></li>
						<li><spring:theme text="setting a default Payment Details in your account" code="text.expresscheckout.line2"/></li>
						<li><spring:theme text="a default shipping method is used" code="text.expresscheckout.line3"/></li>
					</ul>

					<div class="expressCheckoutCheckbox clearfix">
						<label for="expressCheckoutCheckbox"><input id="expressCheckoutCheckbox" name="expressCheckoutEnabled"  type="checkbox" class="form left doExpressCheckout"/>
							<spring:theme text="I would like to Express checkout" code="cart.expresscheckout.checkbox"/></label>
					</div>
				</div>
		</c:if>

		<div class="form-actions clearfix">
			<ycommerce:testId code="login_Login_button">
				<button type="submit" class="positive" id="login_bt" ><spring:theme code="${actionNameKey}"/></button>
				<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
			</ycommerce:testId>
		</div>
	</form:form>
</div>
