/**
 *
 */
package com.energizer.facades.accounts.impl;


import de.hybris.platform.b2b.strategies.B2BUserGroupsLookUpStrategy;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;


/**
 * @author M1028720
 *
 */
public class DefaultEnergizerGroupsLookUpStrategy implements B2BUserGroupsLookUpStrategy
{
	private List<String> groups;

	@Override
	public List<String> getUserGroups()
	{

		return getGroups();
	}

	protected List<String> getGroups()
	{
		return groups;
	}

	@Required
	public void setGroups(final List<String> groups)
	{
		this.groups = groups;
	}
}
