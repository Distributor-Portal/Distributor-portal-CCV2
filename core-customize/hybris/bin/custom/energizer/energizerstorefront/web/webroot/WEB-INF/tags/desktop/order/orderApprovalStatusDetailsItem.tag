<%@ tag body-content="empty" trimDirectiveWhitespaces="true"%>
<%@ attribute name="orderApprovalData" required="true"
	type="de.hybris.platform.b2bacceleratorfacades.order.data.B2BOrderApprovalData"%>
<%@ attribute name="isOrderDetailsPage" type="java.lang.Boolean"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/desktop/product"%>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme"%>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format"%>
<%@ taglib prefix="order" tagdir="/WEB-INF/tags/desktop/order"%>


<div class="span-19">
	<div class="headline">
		<spring:theme code="text.account.orderApprovalDetails.orderStatusDetails.label" text="Order Status Details" />
		</h1>
	</div>
	<div class="span-9">
		<table class="table_budget orderListTable fixthis_budget_cls">
			<tr>
				<td><spring:theme code="text.account.orderApprovalDetails.OrderNumber" text="Order number" />:${orderApprovalData.b2bOrderData.code}</td>
			</tr>
			<c:if test="${not empty orderApprovalData.b2bOrderData.erpOrderNumber}">
			<tr>
			<td><spring:theme code="text.account.orderApprovalDetails.HybrisOrderNumber" text="Order number" />:${orderApprovalData.b2bOrderData.erpOrderNumber}</td>
			</tr>
			</c:if>
			<tr>
				<td><spring:theme code="text.account.orderApprovalDetails.orderPlacedBy" text="Order placed by" />:${orderApprovalData.b2bOrderData.b2bCustomerData.name}</td>
			</tr>
			<tr>
				<td><spring:theme code="text.account.orderApprovalDetails.purchaseOrderNumber" text="P.O.No" />:${orderApprovalData.b2bOrderData.purchaseOrderNumber}</td>
			</tr>
  <c:if test="${not empty orderApprovalData.b2bOrderData.orderComments}">
			<tr>
				<td>
					<spring:theme code="text.account.orderApproval.OrderComments" text="Order Comments" />:
					<p style="word-wrap: break-word;text-align: justify;" class="span-9">${orderApprovalData.b2bOrderData.orderComments}</p>
				</td>			
			</tr>
     </c:if>

		</table>
	</div>
	<div class="span-9 right last">
		<table class="table_budget orderListTable fixthis_budgettwo_cls">
			<tr>
				<td><spring:theme code="text.account.orderApprovalDetails.parentBusinessUnit" text="Parent Business Unit" />:${orderApprovalData.b2bOrderData.costCenter.unit.name}</td>
			</tr>
			<c:if test="${orderApprovalData.b2bOrderData.paymentType.code eq 'ACCOUNT'}">
				<%-- <tr>
					<td><spring:theme code="text.account.orderApprovalDetails.costCenter" text="Cost Center" />:${orderApprovalData.b2bOrderData.costCenter.code}</td>
				</tr> --%>
				<tr>
					<td><spring:theme code="text.account.orderApproval.orderStatus" text="Order Status" />:<spring:theme code="text.account.order.status.display.${orderApprovalData.b2bOrderData.status}"/></td>
				</tr>
			</c:if>
		</table>
	</div>
</div>

