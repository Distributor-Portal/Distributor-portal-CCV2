/**
 * 
 */
package com.energizer.core.datafeed.processor.exception;

import java.io.Serial;


/**
 * @author m9005673
 * 
 */
public class B2BGroupUnknownIdentifierException extends de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException
{

	/**
	 * 
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public B2BGroupUnknownIdentifierException(final String message)
	{
		super(message);
	}

}
