package de.metas.document.impl;

/*
 * #%L
 * de.metas.swat.base
 * %%
 * Copyright (C) 2015 metas GmbH
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


import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.Services;
import org.compiere.model.I_AD_User;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_BPartner_Location;
import org.compiere.model.I_M_Warehouse;

import de.metas.bpartner.IBPartnerBL;
import de.metas.document.IDocumentLocationBL;
import de.metas.document.model.IDocumentBillLocation;
import de.metas.document.model.IDocumentDeliveryLocation;
import de.metas.document.model.IDocumentLocation;

/**
 * 
 * @author tsa
 * @task http://dewiki908/mediawiki/index.php/03120:_Error_in_DocumentLocation_callout_%282012080910000142%29
 */
public class DocumentLocationBL implements IDocumentLocationBL
{
	@Override
	public void setBPartnerAddress(final IDocumentLocation location)
	{
		if (location.getC_BPartner_ID() <= 0)
		{
			return;
		}
		final I_C_BPartner bp = InterfaceWrapperHelper.create(location.getC_BPartner(), I_C_BPartner.class);

		// We need to use BP's trxName because else is not sure that we will get the right data or if we will get it at all
		final String trxName = InterfaceWrapperHelper.getTrxName(bp);

		if (location.getC_BPartner_Location_ID() <= 0)
		{
			return;
		}
		final I_C_BPartner_Location bpartnerLocation = location.getC_BPartner_Location();

		final I_AD_User user;
		if (location.getAD_User_ID() > 0)
		{
			user = location.getAD_User();
		}
		else
		{
			user = null;
		}

		final IBPartnerBL bPartnerBL = Services.get(IBPartnerBL.class);
		final String address = bPartnerBL.mkFullAddress(bp, bpartnerLocation, user, trxName);
		location.setBPartnerAddress(address);
	}

	@Override
	public void setBillToAddress(final IDocumentBillLocation billLocation)
	{
		if (billLocation.getBill_BPartner_ID() <= 0)
		{
			return;
		}
		final I_C_BPartner bp = InterfaceWrapperHelper.create(billLocation.getBill_BPartner(), I_C_BPartner.class);

		if (billLocation.getBill_Location_ID() <= 0)
		{
			return;
		}
		final I_C_BPartner_Location bpartnerLocation = billLocation.getBill_Location();

		final I_AD_User user;
		if (billLocation.getBill_User_ID() > 0)
		{
			user = billLocation.getBill_User();
		}
		else
		{
			user = null;
		}

		final IBPartnerBL bPartnerBL = Services.get(IBPartnerBL.class);
		final String address = bPartnerBL.mkFullAddress(bp, bpartnerLocation, user, null);
		billLocation.setBillToAddress(address);
	}

	@Override
	public void setDeliveryToAddress(final IDocumentDeliveryLocation docDeliveryLocation)
	{
		if (!docDeliveryLocation.isDropShip())
		{
			final int warehouseId = docDeliveryLocation.getM_Warehouse_ID();
			if (warehouseId <= 0)
			{
				return;
			}

			final I_M_Warehouse warehouse = docDeliveryLocation.getM_Warehouse();
			final String address = makeWarehouseAddress(warehouse);
			docDeliveryLocation.setDeliveryToAddress(address);
			return;
		}

		if (docDeliveryLocation.getDropShip_BPartner_ID() <= 0)
		{
			return;
		}
		final I_C_BPartner bp = InterfaceWrapperHelper.create(docDeliveryLocation.getDropShip_BPartner(), I_C_BPartner.class);

		if (docDeliveryLocation.getDropShip_Location_ID() <= 0)
		{
			return;
		}
		final I_C_BPartner_Location bpartnerLocation = docDeliveryLocation.getDropShip_Location();

		final I_AD_User user;
		if (docDeliveryLocation.getDropShip_User_ID() > 0)
		{
			user = docDeliveryLocation.getDropShip_User();
		}
		else
		{
			user = null;
		}

		final IBPartnerBL bPartnerBL = Services.get(IBPartnerBL.class);
		final String address = bPartnerBL.mkFullAddress(bp, bpartnerLocation, user, ITrx.TRXNAME_None);
		docDeliveryLocation.setDeliveryToAddress(address);
	}

	/**
	 * Builds the warehouse address by using {@link I_M_Warehouse#getC_BPartner_Location()}
	 * 
	 * @param warehouse
	 * @return address string
	 */
	private String makeWarehouseAddress(final I_M_Warehouse warehouse)
	{
		if (warehouse.getC_BPartner_Location_ID() <= 0)
		{
			throw new AdempiereException("@NotFound@ @C_BPartner_Location_ID@ (@M_Warehouse_ID@:" + warehouse.getName() + ")");
		}

		final I_C_BPartner_Location bpLocation = warehouse.getC_BPartner_Location();
		final I_C_BPartner bpartner = InterfaceWrapperHelper.create(bpLocation.getC_BPartner(), I_C_BPartner.class);

		// There is no contact available for warehouse
		final I_AD_User bpContact = null;

		final IBPartnerBL bPartnerBL = Services.get(IBPartnerBL.class);
		final String address = bPartnerBL.mkFullAddress(bpartner, bpLocation, bpContact, ITrx.TRXNAME_None);

		return address;
	}

}
