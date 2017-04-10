package de.metas.manufacturing.dispo.testing.otherstuff;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/*
 * #%L
 * metasfresh-manufacturing-dispo
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
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest()
//@ActiveProfiles("test")
//@EnableBinding(Source.class)
//@SpringBootApplication(scanBasePackages = { "de.metas.manufacturing.dispo.testing" })
public class CloudStreamEventListenerTests
{
	// @Autowired
	// TestMessagingGateway gateway;
	//
	// @Test
	// public void testwithGateway()
	// {
	// gateway.generate(new ShipmentScheduleQuantityOrderedEvent());
	// }

	// @Autowired
	// private MessageChannel output;

	// @Test
	// public void testWithMessageChannelTypedEvent()
	// {
	// output.send(
	// MessageBuilder
	// .withPayload(new ShipmentScheduleQuantityOrderedEvent())
	// .build());
	// System.out.println("Message sent");
	// }

	// @Test
	// public void testWithMessageChannelString()
	// {
	// output.send(new GenericMessage<String>("test"));
	// System.out.println("Message sent");
	// }

	@Autowired
	private TestSendingBean testSendingBean;

	@Test
	public void testWithSource()
	{
		testSendingBean.sayHello("test");
	}

}
