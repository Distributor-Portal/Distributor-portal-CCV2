/**
 * 
 */
package com.energizer.storefront.userform.validator;

import java.util.Collection;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.energizer.storefront.forms.B2BCustomerForm;


/**
 * @author m9005673
 * 
 */
public class UserRoleValidator implements Validator
{
	protected static final Logger LOG = Logger.getLogger(UserRoleValidator.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */

	@Override
	public boolean supports(final Class<?> arg0)
	{
		return B2BCustomerForm.class.isAssignableFrom(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(final Object target, final Errors errors)
	{
		final B2BCustomerForm b2bCustomerForm = (B2BCustomerForm) target;
		ValidationUtils.rejectIfEmpty(errors, "roles", "profile.text.b2BCustomerForm.roles");
		final Collection<String> roles = b2bCustomerForm.getRoles();
		try
		{
			Validate.notEmpty(roles, "profile.text.b2BCustomerForm.roles");
			if (b2bCustomerForm.getRoles().size() == 0)
			{
				errors.rejectValue("roles", "profile.text.b2BCustomerForm.roles");
			}
		}
		catch (final IllegalArgumentException illegalArgumentException)
		{
			LOG.error("Roles:" + illegalArgumentException.getMessage());
		}
	}
}
