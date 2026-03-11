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
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;


/**
 * Form object for updating profile.
 */
public class UpdateProfileForm
{
	private String titleCode;
	private String firstName;
	private String lastName;
	private String contactNumber;
	private String passwordQuestion;
	private String passwordAnswer;

	@NotNull(message = "{profile.title.invalid}")
	@Size(min = 1, max = 255, message = "{profile.title.invalid}")
	public String getTitleCode()
	{
		return titleCode;
	}

	public void setTitleCode(final String titleCode)
	{
		this.titleCode = titleCode;
	}

	/*
	 * As per enhancement,firstname,lastname length restricted to 25
	 */
	@NotNull(message = "{profile.firstName.invalid}")
	@Size(min = 1, max = 25, message = "{profile.firstName.invalid}")
	@NotBlank(message = "{profile.firstName.invalid}")
	@Pattern(regexp = "[ a-z-A-Z-0-9]*", message = "{profile.firstName.format.invalid}")
	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(final String firstName)
	{
		this.firstName = firstName;
	}

	@NotNull(message = "{profile.lastName.invalid}")
	@Size(min = 1, max = 25, message = "{profile.lastName.invalid}")
	@NotBlank(message = "{profile.lastName.invalid}")
	@Pattern(regexp = "[ a-z-A-Z-0-9]*", message = "{profile.lastName.format.invalid}")
	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(final String lastName)
	{
		this.lastName = lastName;
	}

	static String PH_FORMAT_US_CANDA_REGEX = "^[+]?[01]?[- .]?(\\([2-9]\\d{3}\\)|[2-9]\\d{2})[- .]?\\d{3}[- .]?\\d{4}$";

	static String PH_FORMAT_US_CANDA_REGEX_01 = "^[+]?[01]?[- .]?(\\([2-9]\\d{3}\\)|[2-9]\\d{2})[- .]?\\d{3}[- .]?\\d{4}$";

	static String reged = "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$";

	//@Pattern(regexp = "^[+]?[01]?[- .]?(\\([2-9]\\d{3}\\)|[2-9]\\d{2})[- .]?\\d{3}[- .]?\\d{4}$", message = "{profile.contactNumber.format.invalid}")
	@NotNull(message = "{profile.contactNumber.invalid}")
	//	@Size(min = 1, max = 255, message = "{profile.contactNumber.invalid}")
	@NotBlank(message = "{profile.contactNumber.invalid}")
	@Pattern(regexp = "[0-9]{3}-?[0-9]{3}-?[0-9]{4,9}", message = "{profile.contactNumber.format.invalid}")
	public String getContactNumber()
	{
		return contactNumber;
	}

	public void setContactNumber(final String contactNum)
	{
		this.contactNumber = contactNum;
	}

	//@NotNull(message = "{profile.passwordQuestion.invalid}")
	//@Size(min = 1, max = 255, message = "{profile.passwordQuestion.invalid}")
	public String getPasswordQuestion()
	{
		return passwordQuestion;
	}


	public void setPasswordQuestion(final String passwordQuestion)
	{
		this.passwordQuestion = passwordQuestion;
	}

	/* Password Answer is restricted to max 25 length */
	//@NotNull(message = "{profile.passwordAnswer.invalid}")
	//@Size(min = 1, max = 25, message = "{profile.passwordAnswer.invalid}")
	//@NotBlank(message = "{profile.passwordAnswer.invalid}")
	public String getPasswordAnswer()
	{
		return passwordAnswer;
	}


	public void setPasswordAnswer(final String passwordAnswer)
	{
		this.passwordAnswer = passwordAnswer;
	}

}
