package de.metas.inoutcandidate.api;

/*
 * #%L
 * de.metas.swat.base
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

import java.util.List;
import java.util.Properties;

import org.adempiere.util.ISingletonService;

import de.metas.inoutcandidate.model.I_M_IolCandHandler;
import de.metas.inoutcandidate.model.I_M_ShipmentSchedule;
import de.metas.inoutcandidate.spi.IShipmentScheduleHandler;
import de.metas.inoutcandidate.spi.ModelWithoutShipmentScheduleVetoer;


/**
 * This interface declares methods to
 * <ul>
 * <li>register SPI implementations in the framework</li>
 * <li>invoke the registered implementations</li>
 * </ul>
 *
 * @author ts
 *
 */
public interface IShipmentScheduleHandlerBL extends ISingletonService
{

	/**
	 * Registers a handler instance for the given table name. This method is intended to be called by various specific
	 * modules to register their SPI implementations.
	 * <p>
	 * <b>Important:</b> the implementation
	 * <ul>
	 * <li>assumes that there is <b>one</b> handler registered per table name</li>
	 * <li>makes sure that a {@link I_M_IolCandHandler} record is created for every registered handler</li>
	 * </ul>
	 *
	 * @param ctx
	 *            context properties that are used when the system makes sure that a <code>I_M_IolCandHandler</code> exists for the given <code>handler</code>.
	 * @param handler
	 *            the implementation to register. This method will call {@link IShipmentScheduleHandler#getSourceTable()} to
	 *            find out for which table the handler is registered.
	 */
	public void registerHandler(Properties ctx, IShipmentScheduleHandler handler);

	/**
	 * Registers a listener for the given table name. The listener is informed if a handler found a data record with a
	 * missing {@link I_M_ShipmentSchedule}. In that case the listener may veto the creation of the shipment schedule.
	 * <p>
	 * Note that
	 * <ul>
	 * <li>there can be zero, one or many listeners for each table name</li>
	 * <li>it is allowed to register a listener for a table name when no handler has (yet) been registered for the same table name</li>
	 * </ul>
	 *
	 * @param vetoer
	 * @param tableName
	 */
	public void registerVetoer(ModelWithoutShipmentScheduleVetoer vetoer, String tableName);

	void invalidateCandidatesFor(Object model, String tableName);

	/**
	 * Invokes all registered {@link IShipmentScheduleHandler}s to create missing InOut candidates.
	 *
	 * @param ctx
	 * @param trxName
	 * @return
	 */
	List<I_M_ShipmentSchedule> createMissingCandidates(Properties ctx, String trxName);

	/**
	 * Loads the handler record with the given class name
	 *
	 * @param ctx
	 * @param className
	 * @param trxName
	 * @return
	 */
	I_M_IolCandHandler retrieveHandlerRecordOrNull(Properties ctx, String className, String trxName);

	/**
	 * Invokes the given <code>sched</code>'s {@link IShipmentScheduleHandler} to get a {@link IDeliverRequest} instance.
	 *
	 * @param sched
	 * @return
	 */
	IDeliverRequest createDeliverRequest(I_M_ShipmentSchedule sched);
}
