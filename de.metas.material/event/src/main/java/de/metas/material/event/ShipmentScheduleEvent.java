package de.metas.material.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Value;

/*
 * #%L
 * metasfresh-manufacturing-event-api
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
@Value // this includes @AllArgsconstructor that is used by jackson when it deserializes a string
@Builder // used by devs to make sure they know with parameter-value goes into which property
public class ShipmentScheduleEvent implements MaterialEvent
{
	public static final String TYPE = "ShipmentScheduleEvent";

	@NonNull
	EventDescr eventDescr;

	@NonNull
	MaterialDescriptor materialDescr;

	int shipmentScheduleId;

	@Default
	int orderLineId = -1;

	@JsonCreator
	public ShipmentScheduleEvent(
			@JsonProperty("eventDescr") @NonNull final EventDescr eventDescr,
			@JsonProperty("materialDescr") @NonNull final MaterialDescriptor materialDescr,
			@JsonProperty("shipmentScheduleId") int shipmentScheduleId,
			@JsonProperty("orderLineId") int orderLineId)
	{
		Preconditions.checkArgument(shipmentScheduleId > 0, "Given parameter shipmentScheduleId=%s needs to be >0", shipmentScheduleId);
		this.shipmentScheduleId = shipmentScheduleId;

		this.eventDescr = eventDescr;
		this.materialDescr = materialDescr;
		this.orderLineId = orderLineId;
	}

}
