/**
 *
 */
package com.energizer.facades.flow;

import de.hybris.platform.b2bacceleratorfacades.order.data.B2BOrderApprovalData;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.workflow.model.WorkflowActionModel;

import java.text.ParseException;
import java.util.List;

import com.energizer.business.BusinessRuleError;
import com.energizer.core.model.EnergizerB2BCustomerModel;


/**
 * @author M1023097
 *
 */
public interface EnergizerB2BCheckoutFlowFacade
{
	public List<BusinessRuleError> getOrderValidation(AbstractOrderModel orderEntryModel);

	public List<BusinessRuleError> getOrderShippingValidation(AbstractOrderEntryModel orderEntryModel);

	public int getLeadTimeData(String shippingPointId, String soldToAddressId);

	public List<String> getsoldToAddressIds(String shippingPointId);

	public List<AddressData> getEnergizerDeliveryAddresses();

	public AbstractOrderData getOrderData();

	public void setLeadTime(int leadTime) throws ParseException;

	public void setLeadTime(int leadTime, CartData cartData) throws ParseException;

	public void setOrderApprover(final EnergizerB2BCustomerModel orderApprover, final B2BOrderApprovalData b2bOrderApprovalData,
			final String rejectionComment);

	public CartData simulateOrder(CartData cartData, final String requestSource) throws Exception;

	public void setContainerAttributes(final CartData cartData);

	/**
	 * @param workFlowActionCode
	 * @return
	 */
	public WorkflowActionModel getActionForCode(String workFlowActionCode);

}
