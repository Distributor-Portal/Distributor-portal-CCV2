/**
 *
 */
package com.energizer.facades.accounts.impl;


import de.hybris.platform.b2b.strategies.B2BUserGroupsLookUpStrategy;

import java.util.List;


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

	public void setGroups(final List<String> groups)
	{
		this.groups = groups;
	}
}
