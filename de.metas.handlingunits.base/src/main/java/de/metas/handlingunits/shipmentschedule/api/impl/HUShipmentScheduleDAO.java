package de.metas.handlingunits.shipmentschedule.api.impl;

/*
 * #%L
 * de.metas.handlingunits.base
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

import java.util.List;
import java.util.Properties;

import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.dao.IQueryBuilder;
import org.adempiere.util.Check;
import org.adempiere.util.Services;
import org.compiere.util.Env;

import com.google.common.base.Preconditions;

import de.metas.handlingunits.IHandlingUnitsBL;
import de.metas.handlingunits.exceptions.HUException;
import de.metas.handlingunits.model.I_M_HU;
import de.metas.handlingunits.model.I_M_ShipmentSchedule_QtyPicked;
import de.metas.handlingunits.shipmentschedule.api.IHUShipmentScheduleDAO;
import lombok.NonNull;

public class HUShipmentScheduleDAO implements IHUShipmentScheduleDAO
{
	@Override
	public List<I_M_ShipmentSchedule_QtyPicked> retrieveSchedsQtyPickedForHU(final I_M_HU hu)
	{
		Check.assumeNotNull(hu, "hu not null");

		// Services
		final IQueryBL queryBL = Services.get(IQueryBL.class);
		final IHandlingUnitsBL handlingUnitsBL = Services.get(IHandlingUnitsBL.class);

		final IQueryBuilder<I_M_ShipmentSchedule_QtyPicked> queryBuilder = queryBL.createQueryBuilder(I_M_ShipmentSchedule_QtyPicked.class, hu)
				.addOnlyActiveRecordsFilter();

		//
		// Filter HU based on it's type (LU/TU)
		if (handlingUnitsBL.isVirtual(hu))
		{
			queryBuilder.addEqualsFilter(I_M_ShipmentSchedule_QtyPicked.COLUMNNAME_VHU_ID, hu.getM_HU_ID());
		}
		else if (handlingUnitsBL.isLoadingUnit(hu))
		{
			queryBuilder.addEqualsFilter(I_M_ShipmentSchedule_QtyPicked.COLUMNNAME_M_LU_HU_ID, hu.getM_HU_ID());
		}
		else if (handlingUnitsBL.isTransportUnitOrVirtual(hu))
		{
			queryBuilder.addEqualsFilter(I_M_ShipmentSchedule_QtyPicked.COLUMNNAME_M_TU_HU_ID, hu.getM_HU_ID());
		}
		else
		{
			throw new HUException("HU shall be a LU or a TU: " + handlingUnitsBL.getDisplayName(hu));
		}

		//
		// Order by creation sequence
		queryBuilder.orderBy()
				.addColumn(de.metas.inoutcandidate.model.I_M_ShipmentSchedule_QtyPicked.COLUMNNAME_M_ShipmentSchedule_QtyPicked_ID);

		//
		// Execute the query and return the list
		return queryBuilder
				.create()
				.list();
	}

	@Override
	public List<I_M_ShipmentSchedule_QtyPicked> retriveQtyPickedNotDeliveredForTopLevelHU(final I_M_HU hu)
	{
		Check.assumeNotNull(hu, "hu not null");

		final IQueryBL queryBL = Services.get(IQueryBL.class);
		final IHandlingUnitsBL handlingUnitsBL = Services.get(IHandlingUnitsBL.class);

		final IQueryBuilder<I_M_ShipmentSchedule_QtyPicked> queryBuilder = queryBL
				.createQueryBuilder(I_M_ShipmentSchedule_QtyPicked.class, hu)
				// Not delivered
				.addEqualsFilter(I_M_ShipmentSchedule_QtyPicked.COLUMNNAME_M_InOutLine_ID, null)
				// Only Actives
				.addOnlyActiveRecordsFilter();

		//
		// Filter by LU/TU/VHU
		if (handlingUnitsBL.isLoadingUnit(hu))
		{
			queryBuilder.addEqualsFilter(I_M_ShipmentSchedule_QtyPicked.COLUMNNAME_M_LU_HU_ID, hu.getM_HU_ID());
		}
		else if (handlingUnitsBL.isTransportUnit(hu))
		{
			queryBuilder.addEqualsFilter(I_M_ShipmentSchedule_QtyPicked.COLUMNNAME_M_LU_HU_ID, null);
			queryBuilder.addEqualsFilter(I_M_ShipmentSchedule_QtyPicked.COLUMNNAME_M_TU_HU_ID, hu.getM_HU_ID());
		}
		else if (handlingUnitsBL.isVirtual(hu))
		{
			queryBuilder.addEqualsFilter(I_M_ShipmentSchedule_QtyPicked.COLUMNNAME_M_LU_HU_ID, null);
			queryBuilder.addEqualsFilter(I_M_ShipmentSchedule_QtyPicked.COLUMNNAME_M_TU_HU_ID, null);
			queryBuilder.addEqualsFilter(I_M_ShipmentSchedule_QtyPicked.COLUMNNAME_VHU_ID, hu.getM_HU_ID());
		}
		else
		{
			throw new HUException("Unsupported HU type: " + handlingUnitsBL.getDisplayName(hu));
		}

		return queryBuilder.create().list();

	}

	@Override
	public List<I_M_ShipmentSchedule_QtyPicked> retrieveSchedsQtyPickedForTU(final int shipmentScheduleId, final int tuHUId, final String trxName)
	{
		Preconditions.checkArgument(shipmentScheduleId > 0, "shipmentScheduleId > 0");
		Preconditions.checkArgument(tuHUId > 0, "tuHUId > 0");
		
		final Properties ctx = Env.getCtx();
		final IQueryBuilder<I_M_ShipmentSchedule_QtyPicked> queryBuilder = Services.get(IQueryBL.class).createQueryBuilder(I_M_ShipmentSchedule_QtyPicked.class, ctx, trxName)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(de.metas.inoutcandidate.model.I_M_ShipmentSchedule_QtyPicked.COLUMNNAME_M_ShipmentSchedule_ID, shipmentScheduleId)
				.addEqualsFilter(I_M_ShipmentSchedule_QtyPicked.COLUMNNAME_M_TU_HU_ID, tuHUId)
				.orderBy()
				.addColumn(de.metas.inoutcandidate.model.I_M_ShipmentSchedule_QtyPicked.COLUMNNAME_M_ShipmentSchedule_QtyPicked_ID).endOrderBy();

		return queryBuilder.create().list();
	}

	@Override
	public List<I_M_ShipmentSchedule_QtyPicked> retrieveSchedsQtyPickedForVHU(final I_M_HU vhu)
	{
		return retrieveSchedsQtyPickedForVHUQuery(vhu)
				.create()
				.list();
	}

	@Override
	public IQueryBuilder<I_M_ShipmentSchedule_QtyPicked> retrieveSchedsQtyPickedForVHUQuery(@NonNull final I_M_HU vhu)
	{
		final IQueryBuilder<I_M_ShipmentSchedule_QtyPicked> queryBuilder = Services.get(IQueryBL.class).createQueryBuilder(I_M_ShipmentSchedule_QtyPicked.class, vhu)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_M_ShipmentSchedule_QtyPicked.COLUMNNAME_VHU_ID, vhu.getM_HU_ID())
				.orderBy()
				.addColumn(de.metas.inoutcandidate.model.I_M_ShipmentSchedule_QtyPicked.COLUMNNAME_M_ShipmentSchedule_QtyPicked_ID).endOrderBy();

		return queryBuilder;
	}

}
