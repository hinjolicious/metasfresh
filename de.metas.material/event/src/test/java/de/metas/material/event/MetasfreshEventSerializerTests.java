package de.metas.material.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.metas.event.Event;
import de.metas.event.SimpleObjectSerializer;
import de.metas.event.jms.JsonEventSerializer;

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
/**
 *
 * @author metas-dev <dev@metasfresh.com>
 *
 */
public class MetasfreshEventSerializerTests
{
//	@Before
//	public void init()
//	{
//		AdempiereTestHelper.get().init();
//	}

	/**
	 * Verifies that {@link JsonEventSerializer} works, because we put a JSON string into an {@link Event} which is also serialized into JSON,
	 * so there might be problems down that road if {@link JsonEventSerializer} get confused about which parts of the string are just payload and which aren't.
	 *
	 */
	@Test
	public void serialize_and_deserialize_event_with_json_payload()
	{
		final TransactionEvent transactionEvent = MaterialEventSerializerTests.createSampleTransactionEvent();
		final String inOutEventStr = SimpleObjectSerializer.get().serialize(transactionEvent);

		final Event event = Event.builder()
				.putProperty(MaterialEventService.MATERIAL_DISPOSITION_EVENT, inOutEventStr)
				.build();

		final String eventStr = JsonEventSerializer.instance.toString(event);

		final Event deserialisedEvent = JsonEventSerializer.instance.fromString(eventStr);
		final String deserializedInOutEventStr = deserialisedEvent.getProperty(MaterialEventService.MATERIAL_DISPOSITION_EVENT);

		final MaterialEvent deserializedInOutEvent = SimpleObjectSerializer.get().deserialize(deserializedInOutEventStr, MaterialEvent.class);

		assertThat(deserializedInOutEvent).isInstanceOf(TransactionEvent.class);
		assertThat(((TransactionEvent)deserializedInOutEvent).getMaterialDescr().getProductId()).isEqualTo(20); // "spot check": picking the productId
		assertThat(deserializedInOutEvent, is(transactionEvent));
	}
}
