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
package com.energizer.storefront.tags;

import com.energizer.storefront.web.wrappers.RemoveEncodingHttpServletRequestWrapper;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.BodyContent;
import jakarta.servlet.jsp.tagext.BodyTagSupport;

import com.granule.CompressTagHandler;
import com.granule.CompressorSettings;
import com.granule.IRequestProxy;


public class AcceleratorCompressTag extends BodyTagSupport
{
	public static final String COMPREST_TAG_CONTENT = "com.granule.CompressTagContent";
	public static final String COMPREST_TAG_JS = "com.granule.CompressTagjs";
	public static final String COMPREST_TAG_CSS = "com.granule.CompressTagcss";
	private static final String NOT_PROCESS_PARAMETER = "granule";

	private final String method = null;
	private final String id = null;
	private final String options = null;
	private final String basepath = null;
	private String urlpattern = null;

	@Override
	public int doAfterBody() throws JspTagException
	{
		final HttpServletRequest httpRequest = new RemoveEncodingHttpServletRequestWrapper(
				(HttpServletRequest) pageContext.getRequest(), urlpattern);
		final BodyContent bodyContent = getBodyContent();
		final String oldBody = bodyContent.getString();
		bodyContent.clearBody();
		if (httpRequest.getParameter(NOT_PROCESS_PARAMETER) != null)
		{
			final boolean process = CompressorSettings.getBoolean(httpRequest.getParameter(NOT_PROCESS_PARAMETER), false);
			if (!process)
			{
				httpRequest.getSession().setAttribute(NOT_PROCESS_PARAMETER, Boolean.TRUE);
			}
			else
			{
				httpRequest.getSession().removeAttribute(NOT_PROCESS_PARAMETER);
			}
		}
		if (httpRequest.getSession().getAttribute(NOT_PROCESS_PARAMETER) != null)
		{
			try
			{
				getPreviousOut().print(oldBody);
			}
			catch (final IOException e)
			{
				throw new JspTagException(e);
			}
			return SKIP_BODY;
		}
		try
		{
			final CompressTagHandler compressor = new CompressTagHandler(id, method, options, basepath);
			final IRequestProxy runtimeRequest = new JakartaRequestProxy(httpRequest);
			final String newBody = compressor.handleTag(runtimeRequest, runtimeRequest, oldBody);
			getPreviousOut().print(newBody);
		}
		catch (final Exception e)
		{
			throw new JspTagException(e);
		}
		return SKIP_BODY;
	}

	public String getUrlpattern()
	{
		return urlpattern;
	}

	public void setUrlpattern(final String urlpattern)
	{
		this.urlpattern = urlpattern;
	}

	private static class JakartaRequestProxy implements IRequestProxy
	{
		private final HttpServletRequest request;

		JakartaRequestProxy(final HttpServletRequest request)
		{
			this.request = request;
		}

		@Override
		public String getRealPath(final String path)
		{
			return request.getServletContext().getRealPath(path);
		}

		@Override
		public Object getAttribute(final String name)
		{
			return request.getAttribute(name);
		}

		@Override
		public void setAttribute(final String name, final Object value)
		{
			request.setAttribute(name, value);
		}

		@Override
		public String getServletPath()
		{
			return request.getServletPath();
		}

		@Override
		public String getBasePath()
		{
			return request.getContextPath();
		}

		@Override
		public String getContextPath()
		{
			return request.getContextPath();
		}
	}
}
