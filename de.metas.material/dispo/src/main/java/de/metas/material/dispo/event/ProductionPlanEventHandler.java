package de.metas.material.dispo.event;

import org.springframework.stereotype.Service;

import de.metas.material.dispo.Candidate;
import de.metas.material.dispo.Candidate.Status;
import de.metas.material.dispo.Candidate.SubType;
import de.metas.material.dispo.Candidate.Type;
import de.metas.material.dispo.CandidateChangeHandler;
import de.metas.material.dispo.CandidateService;
import de.metas.material.dispo.DemandCandidateDetail;
import de.metas.material.dispo.ProductionCandidateDetail;
import de.metas.material.event.ProductionPlanEvent;
import de.metas.material.event.pporder.PPOrder;
import de.metas.material.event.pporder.PPOrderLine;
import lombok.NonNull;

/*
 * #%L
 * metasfresh-material-dispo
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
@Service
public class ProductionPlanEventHandler
{
	private final CandidateChangeHandler candidateChangeHandler;
	private final CandidateService candidateService;

	public ProductionPlanEventHandler(
			@NonNull final CandidateChangeHandler candidateChangeHandler,
			@NonNull final CandidateService candidateService)
	{
		this.candidateChangeHandler = candidateChangeHandler;
		this.candidateService = candidateService;
	}

	void handleProductionPlanEvent(final ProductionPlanEvent event)
	{
		final PPOrder ppOrder = event.getPpOrder();

		final Candidate.Status candidateStatus;
		if (ppOrder.getPpOrderId() <= 0)
		{
			candidateStatus = Status.doc_planned;
		}
		else if ("DR".equals(ppOrder.getDocStatus())||"IP".equals(ppOrder.getDocStatus()))
		{
			candidateStatus = Status.doc_created;
		}
		else if ("CO".equals(ppOrder.getDocStatus()))
		{
			candidateStatus = Status.doc_completed;
		}
		else if ("CL".equals(ppOrder.getDocStatus()))
		{
			candidateStatus = Status.doc_closed;
		}
		else
		{
			candidateStatus = Status.unexpected;
		}

		final Candidate supplyCandidate = Candidate.builder()
				.type(Type.SUPPLY)
				.subType(SubType.PRODUCTION)
				.status(candidateStatus)

				.date(ppOrder.getDatePromised())
				.orgId(ppOrder.getOrgId())
				.productId(ppOrder.getProductId())
				.quantity(ppOrder.getQuantity())
				.warehouseId(ppOrder.getWarehouseId())
				.reference(event.getReference())
				.productionDetail(ProductionCandidateDetail.builder()
						.plantId(ppOrder.getPlantId())
						.productPlanningId(ppOrder.getProductPlanningId())
						.ppOrderId(ppOrder.getPpOrderId())
						.ppOrderDocStatus(ppOrder.getDocStatus())
						.build())
				.demandDetail(DemandCandidateDetail.builder()
						.orderLineId(ppOrder.getOrderLineId())
						.build())
				.build();

		// this might cause 'candidateChangeHandler' to trigger another event
		final Candidate candidateWithGroupId = candidateChangeHandler.onSupplyCandidateNewOrChange(supplyCandidate);

		for (final PPOrderLine ppOrderLine : ppOrder.getLines())
		{
			final Candidate lineCandidate = Candidate.builder()
					.type(ppOrderLine.isReceipt() ? Type.SUPPLY : Type.DEMAND)
					.subType(SubType.PRODUCTION)
					.status(candidateStatus)

					.groupId(candidateWithGroupId.getGroupId())
					.seqNo(candidateWithGroupId.getSeqNo() + 1)

					.date(ppOrderLine.isReceipt() ? ppOrder.getDatePromised() : ppOrder.getDateStartSchedule())
					.orgId(ppOrder.getOrgId())
					.productId(ppOrderLine.getProductId())
					.attributeSetInstanceId(ppOrderLine.getAttributeSetInstanceId())
					.quantity(ppOrderLine.getQtyRequired())
					.warehouseId(ppOrder.getWarehouseId())
					.reference(event.getReference())
					.productionDetail(ProductionCandidateDetail.builder()
							.productBomLineId(ppOrderLine.getProductBomLineId())
							.description(ppOrderLine.getDescription())
							.ppOrderId(ppOrder.getPpOrderId())
							.ppOrderDocStatus(ppOrder.getDocStatus())
							.ppOrderLineId(ppOrderLine.getPpOrderLineId())
							.build())
					.demandDetail(DemandCandidateDetail.builder()
							.orderLineId(ppOrder.getOrderLineId())
							.build())
					.build();

			// might trigger further demand events
			candidateChangeHandler.onCandidateNewOrChange(lineCandidate);
		}

		if (ppOrder.isCreatePPOrder())
		{
			candidateService.requestPPOrder(candidateWithGroupId.getGroupId());
		}
	}
}