package de.metas.procurement.base.impl;

import org.adempiere.model.InterfaceWrapperHelper;
import org.compiere.model.I_C_BPartner;

/*
 * #%L
 * de.metas.procurement.base
 * %%
 * Copyright (C) 2017 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

public class ProcurementTestTools
{
	static final String BPARTNER_NAME = "myName";

	static final String LANGUAGE = "mylanguage";
	
	public static I_C_BPartner createBPartner()
	{
		final I_C_BPartner bpartner = InterfaceWrapperHelper.newInstance(I_C_BPartner.class);
		bpartner.setAD_Language(LANGUAGE);
		bpartner.setName(BPARTNER_NAME);
		bpartner.setIsVendor(true);
		InterfaceWrapperHelper.save(bpartner);
		return bpartner;
	}
}
