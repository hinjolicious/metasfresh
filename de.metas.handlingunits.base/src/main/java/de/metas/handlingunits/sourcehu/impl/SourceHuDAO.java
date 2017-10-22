package de.metas.handlingunits.sourcehu.impl;

import java.util.List;

import org.adempiere.ad.dao.ICompositeQueryFilter;
import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.dao.IQueryBuilder;
import org.adempiere.ad.dao.IQueryFilter;
import org.adempiere.util.Services;
import org.adempiere.util.proxy.Cached;
import org.compiere.model.IQuery;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import de.metas.adempiere.util.CacheModel;
import de.metas.handlingunits.IHandlingUnitsDAO;
import de.metas.handlingunits.model.I_M_HU;
import de.metas.handlingunits.model.I_M_Source_HU;
import de.metas.handlingunits.sourcehu.ISourceHuDAO;
import de.metas.handlingunits.sourcehu.SourceHUsService.MatchingSourceHusQuery;
import lombok.NonNull;

/*
 * #%L
 * de.metas.handlingunits.base
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

public class SourceHuDAO implements ISourceHuDAO
{
	@Override
	@Cached(cacheName = I_M_Source_HU.Table_Name + "#by#" + I_M_Source_HU.COLUMNNAME_M_HU_ID)
	public boolean isSourceHu(final int huId)
	{
		final boolean isSourceHU = Services.get(IQueryBL.class)
				.createQueryBuilder(I_M_Source_HU.class)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_M_Source_HU.COLUMNNAME_M_HU_ID, huId)
				.create()
				.match();
		return isSourceHU;
	}

	@Override
	@Cached(cacheName = I_M_Source_HU.Table_Name + "#by#" + I_M_Source_HU.COLUMNNAME_M_HU_ID)
	public I_M_Source_HU retrieveSourceHuMarkerOrNull(@NonNull @CacheModel final I_M_HU hu)
	{
		return Services.get(IQueryBL.class)
				.createQueryBuilder(I_M_Source_HU.class)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_M_Source_HU.COLUMN_M_HU_ID, hu.getM_HU_ID())
				.create()
				.firstOnly(I_M_Source_HU.class);
	}
	
	@Override
	public List<I_M_HU> retrieveActiveSourceHus(@NonNull final MatchingSourceHusQuery query)
	{
		if (query.getProductIds().isEmpty())
		{
			return ImmutableList.of();
		}

		final ICompositeQueryFilter<I_M_HU> huFilters = createHuFilter(query);

		final IQueryBuilder<I_M_HU> queryBuilder = Services.get(IQueryBL.class).createQueryBuilder(I_M_Source_HU.class)
				.addOnlyActiveRecordsFilter()
				.andCollect(I_M_Source_HU.COLUMN_M_HU_ID)
				.filter(huFilters);

		final List<I_M_HU> result = queryBuilder.create()
				.list();
		return result;
	}

	@Override
	public List<I_M_Source_HU> retrieveActiveSourceHuMarkers(@NonNull final MatchingSourceHusQuery query)
	{
		if (query.getProductIds().isEmpty())
		{
			return ImmutableList.of();
		}
		final IQueryBL queryBL = Services.get(IQueryBL.class);

		final IQuery<I_M_HU> huQuery = queryBL.createQueryBuilder(I_M_HU.class)
				.addOnlyActiveRecordsFilter()
				.filter(createHuFilter(query))
				.create();

		return queryBL.createQueryBuilder(I_M_Source_HU.class)
				.addOnlyActiveRecordsFilter()
				.addInSubQueryFilter(I_M_Source_HU.COLUMN_M_HU_ID, I_M_HU.COLUMN_M_HU_ID, huQuery)
				.create()
				.list();
	}

	@VisibleForTesting
	static ICompositeQueryFilter<I_M_HU> createHuFilter(@NonNull final MatchingSourceHusQuery query)
	{
		final IQueryBL queryBL = Services.get(IQueryBL.class);

		final IHandlingUnitsDAO handlingUnitsDAO = Services.get(IHandlingUnitsDAO.class);

		final ICompositeQueryFilter<I_M_HU> huFilters = queryBL.createCompositeQueryFilter(I_M_HU.class)
				.setJoinOr();

		final IQueryFilter<I_M_HU> huFilter = handlingUnitsDAO.createHUQueryBuilder()
				.setOnlyActiveHUs(true)
				.setAllowEmptyStorage()
				.addOnlyWithProductIds(query.getProductIds())
				.addOnlyInWarehouseId(query.getWarehouseId())
				.createQueryFilter();
		huFilters.addFilter(huFilter);

		return huFilters;
	}
}
