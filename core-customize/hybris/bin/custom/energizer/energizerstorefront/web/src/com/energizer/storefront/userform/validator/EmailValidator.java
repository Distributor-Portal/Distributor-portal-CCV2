/**
 * 
 */
package com.energizer.storefront.userform.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.energizer.storefront.forms.B2BCustomerForm;


/**
 * @author selvaraja savarimuthu
 * 
 */
public class EmailValidator implements Validator
{
	private Pattern pattern;
	private Matcher matcher;

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
	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,4})$";

	@Override
	public void validate(final Object target, final Errors errors)
	{
		final B2BCustomerForm b2bCustomerForm = (B2BCustomerForm) target;
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "profile.email.invalid");
		if (!(b2bCustomerForm.getEmail() != null && b2bCustomerForm.getEmail().isEmpty()))
		{
			pattern = Pattern.compile(EMAIL_PATTERN);
			matcher = pattern.matcher(b2bCustomerForm.getEmail());
			if (!matcher.matches())
			{
				errors.rejectValue("email", "profile.email.invalid");
			}
		}
	}
}
