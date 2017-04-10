package de.metas.manufacturing.dispo.testing.otherstuff;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.core.MessageSource;
import org.springframework.messaging.support.GenericMessage;

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

//@SpringBootApplication(scanBasePackages = { "de.metas.manufacturing.dispo.testing" })
//@EnableBinding(Source.class)
public class StreamdemoApplication
{

	public static void main(String[] args)
	{
		new SpringApplicationBuilder(StreamdemoApplication.class)
				.web(false)
				.run(args);
	}

	@Bean
	@InboundChannelAdapter(value = Source.OUTPUT)
	public MessageSource<String> timerMessageSource()
	{
		return () -> new GenericMessage<>(new SimpleDateFormat().format(new Date()));
	}

}
