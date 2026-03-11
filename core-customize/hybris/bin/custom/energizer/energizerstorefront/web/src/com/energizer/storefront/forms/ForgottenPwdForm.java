/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2014 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 *
 *  
 */
package com.energizer.storefront.forms;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;


/**
 * Form for forgotten password.
 */
public class ForgottenPwdForm
{
	private String email;
	/*private String passwordQuestion;
	private String passwordAnswer;*/

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


	/*@NotNull(message = "{profile.passwordQuestion.invalid}")
	@Size(min = 1, max = 255, message = "{profile.passwordQuestion.invalid}")
	public String getPasswordQuestion()
	{
		return passwordQuestion;
	}


	public void setPasswordQuestion(final String passwordQuestion)
	{
		this.passwordQuestion = passwordQuestion;
	}*/

	/* Password Answer is restricted to max 25 length */
	/*@NotNull(message = "{profile.passwordAnswer.invalid}")
	@Size(min = 1, max = 25, message = "{profile.passwordAnswer.invalid}")
	@NotBlank(message = "{profile.passwordAnswer.invalid}")
	public String getPasswordAnswer()
	{
		return passwordAnswer;
	}


	public void setPasswordAnswer(final String passwordAnswer)
	{
		this.passwordAnswer = passwordAnswer;
	}*/

}
