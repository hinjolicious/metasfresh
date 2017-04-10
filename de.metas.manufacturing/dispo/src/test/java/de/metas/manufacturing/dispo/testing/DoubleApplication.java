/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.metas.manufacturing.dispo.testing;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.aggregate.AggregateApplicationBuilder;

import de.metas.manufacturing.dispo.testing.processor.ProcessorApplication;
import de.metas.manufacturing.dispo.testing.sink.SinkApplication;
import de.metas.manufacturing.dispo.testing.source.SourceApplication;

@SpringBootApplication
public class DoubleApplication
{

	public static void main(String[] args)
	{
		new AggregateApplicationBuilder(DoubleApplication.class, args)
				.from(SourceApplication.class).args("--fixedDelay=5000")
				.via(ProcessorApplication.class)
				.to(SinkApplication.class).args("--debug=true")
				.run();
	}

}
