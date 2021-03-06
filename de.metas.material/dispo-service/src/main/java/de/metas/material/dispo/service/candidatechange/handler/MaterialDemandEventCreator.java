package de.metas.material.dispo.service.candidatechange.handler;

import java.math.BigDecimal;

import com.google.common.base.Preconditions;

import de.metas.material.dispo.CandidateSpecification.Type;
import de.metas.material.dispo.candidate.Candidate;
import de.metas.material.event.EventDescr;
import de.metas.material.event.MaterialDemandDescr;
import de.metas.material.event.MaterialDemandEvent;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/*
 * #%L
 * metasfresh-material-dispo-service
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

@UtilityClass
public class MaterialDemandEventCreator
{
	public MaterialDemandEvent createMaterialDemandEvent(
			@NonNull final Candidate demandCandidate,
			@NonNull final BigDecimal requiredAdditionalQty)
	{
		verifyCandidateType(demandCandidate);

		final int orderLineId = demandCandidate.getDemandDetail() == null ? 0
				: demandCandidate.getDemandDetail().getOrderLineId();

		final MaterialDemandEvent materialDemandEvent = MaterialDemandEvent
				.builder()
				.materialDemandDescr(createMaterialDemandDescr(demandCandidate, requiredAdditionalQty, orderLineId))
				.build();
		return materialDemandEvent;
	}

	private void verifyCandidateType(final Candidate demandCandidate)
	{
		final Type candidateType = demandCandidate.getType();
		Preconditions.checkArgument(candidateType == Type.DEMAND || candidateType == Type.STOCK_UP,
				"Given parameter demandCandidate needs to have DEMAND or STOCK_UP as type; demandCandidate=%s", demandCandidate);
	}

	private MaterialDemandDescr createMaterialDemandDescr(
			@NonNull final Candidate candidate,
			@NonNull final BigDecimal qty,
			final int orderLineId)
	{
		return MaterialDemandDescr.builder()
				.demandCandidateId(candidate.getId())
				.eventDescr(new EventDescr(candidate.getClientId(), candidate.getOrgId()))
				.materialDescriptor(candidate.getMaterialDescr().withQuantity(qty))
				.orderLineId(orderLineId)
				.build();
	}

}
