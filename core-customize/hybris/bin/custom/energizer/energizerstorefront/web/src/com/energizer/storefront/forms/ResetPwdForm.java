/**
 * 
 */
package com.energizer.storefront.forms;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;


/**
 * @author M1028886
 * 
 *         Form for Reset Password for new customers created by admin in storefront
 * 
 */
public class ResetPwdForm
{

	private String email;

	/**
	 * @return the email
	 */
	@NotNull(message = "{forgottenPwd.email.invalid}")
	@Size(min = 1, max = 255, message = "{forgottenPwd.email.invalid}")
	@Email(message = "{forgottenPwd.email.invalid}")
	public String getEmail()
	{
		return email;
	}

	/**
	 * @param email
	 *           the email to set
	 */
	public void setEmail(final String email)
	{
		this.email = email;
	}



}
