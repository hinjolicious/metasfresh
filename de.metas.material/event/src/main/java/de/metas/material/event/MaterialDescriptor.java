package de.metas.material.event;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Wither;

/*
 * #%L
 * metasfresh-material-event
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
@Data
@Builder
@AllArgsConstructor
@Wither
public class MaterialDescriptor
{
	@NonNull
	private final Integer warehouseId;

	@NonNull
	private final Integer productId;

	/**
	 * A string representation of those ASI parts that are storage relevant.
	 * Product and asiKey together are used to decide if e.g. a given demand can be balanced with a given stock.
	 */
	@NonNull
	private final String asiKey;

	/**
	 * The attribute set instance ID is there to be rendered nicely in the client. Apart from that, it doesn't really matter in materia dispo.
	 */
	@NonNull
	private final Integer attributeSetInstanceId;

	/**
	 * The meaning of this field might differ.
	 * It can be the absolute stock quantity at a given time (if the type is "stock") or it can be a supply, demand or stock related <b>delta</b>,
	 * i.e. one addition or removal that occurs at a particular time.
	 */
	@NonNull
	private final BigDecimal qty;

	/**
	 * The projected date at which we expect this descriptor's {@link #getQuantity()}.
	 */
	@NonNull
	private final Date date;
}
