package de.metas.handlingunits.impl;

import java.util.ArrayList;

/*
 * #%L
 * de.metas.handlingunits.base
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.model.IContextAware;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.Check;
import org.adempiere.util.Services;
import org.adempiere.util.time.SystemTime;

import com.google.common.collect.ImmutableList;

import de.metas.handlingunits.IHUContext;
import de.metas.handlingunits.IHUPackingMaterialsCollector;
import de.metas.handlingunits.IHandlingUnitsBL;
import de.metas.handlingunits.IMutableHUContext;
import de.metas.handlingunits.attribute.storage.IAttributeStorageFactory;
import de.metas.handlingunits.attribute.storage.IAttributeStorageFactoryService;
import de.metas.handlingunits.hutransaction.IHUTrxBL;
import de.metas.handlingunits.spi.IHUPackingMaterialCollectorSource;
import de.metas.handlingunits.spi.impl.HUPackingMaterialsCollector;
import de.metas.handlingunits.storage.EmptyHUListener;
import de.metas.handlingunits.storage.IHUStorageFactory;
import lombok.NonNull;

/* package */class MutableHUContext implements IMutableHUContext
{
	private final Properties ctx;

	private String trxName;

	private Map<String, Object> _contextProperties = new HashMap<>();

	private IHUStorageFactory huStorageFactory = null;
	private IAttributeStorageFactory _attributesStorageFactory = null;
	private boolean _attributesStorageFactoryInitialized = false;
	private Date date = null;
	private CompositeHUTrxListener _trxListeners = null;

	final IHUContext huCtx = null; // task 07734: we don't want to track M_MaterialTrackings, so we don't need to provide a HU context.
	private IHUPackingMaterialsCollector<IHUPackingMaterialCollectorSource> _destroyedHUPackingMaterialsCollector = new HUPackingMaterialsCollector(null);

	private final List<EmptyHUListener> emptyHUListeners = new ArrayList<>();

	public MutableHUContext(final Object contextProvider)
	{
		super();

		Check.assumeNotNull(contextProvider, "contextProvider  not null");
		final IContextAware contextAware = InterfaceWrapperHelper.getContextAware(contextProvider);
		ctx = contextAware.getCtx();
		trxName = contextAware.getTrxName();
		date = DateTrxProvider.getDateTrx();
	}

	public MutableHUContext(final Properties ctx)
	{
		this(ctx, ITrx.TRXNAME_None);
	}

	public MutableHUContext(final Properties ctx, final String trxName)
	{
		super();

		Check.assumeNotNull(ctx, "ctx not null");
		this.ctx = ctx;

		this.trxName = trxName;
		date = SystemTime.asDate();
	}

	@Override
	public String toString()
	{
		return "MutableHUContext ["
				+ "trxName=" + trxName
				+ ", huStorageFactory=" + huStorageFactory
				+ ", attributesStorageFactory=" + _attributesStorageFactory
				+ ", date=" + getDate()
				+ ", ctx=" + ctx
				+ "]";
	}

	@Override
	public Object setProperty(final String propertyName, final Object value)
	{
		return _contextProperties.put(propertyName, value);
	}

	@Override
	public <T> T getProperty(final String propertyName)
	{
		@SuppressWarnings("unchecked")
		final T propertyValue = (T)_contextProperties.get(propertyName);
		return propertyValue;
	}

	private final void setProperties(final Map<String, Object> contextProperties)
	{
		_contextProperties = contextProperties;
	}

	private final Map<String, Object> getProperties()
	{
		return _contextProperties;
	}

	@Override
	public IMutableHUContext copyAsMutable()
	{
		final MutableHUContext huContextCopy = new MutableHUContext(getCtx());
		huContextCopy.setTrxName(getTrxName());

		huContextCopy.setProperties(getProperties()); // copy properties from old context

		huContextCopy.setHUStorageFactory(getHUStorageFactory());
		huContextCopy.setHUAttributeStorageFactory(getHUAttributeStorageFactory());
		huContextCopy.setDate(getDate());
		huContextCopy.setHUPackingMaterialsCollector(_destroyedHUPackingMaterialsCollector);
		huContextCopy._trxListeners = getTrxListeners().copy(); // using the getter to make sure they are loaded

		emptyHUListeners.forEach(l -> huContextCopy.addEmptyHUListener(l));

		return huContextCopy;
	}

	@Override
	public Properties getCtx()
	{
		return ctx;
	}

	@Override
	public String getTrxName()
	{
		return trxName;
	}

	@Override
	public void setTrxName(final String trxName)
	{
		this.trxName = trxName;
	}

	@Override
	public void setDate(final Date date)
	{
		Check.assumeNotNull(date, "date not null");
		this.date = date;
	}

	@Override
	public Date getDate()
	{
		return date;
	}

	@Override
	public IHUStorageFactory getHUStorageFactory()
	{
		if (huStorageFactory == null)
		{
			huStorageFactory = Services.get(IHandlingUnitsBL.class).getStorageFactory();

			//
			// Intercept HU Storage changes and forward the event to IAttributeStorage
			//
			// NOTEs:
			// * 06952: ts: commented it out, because the performance is prohibitive
			// * tsa: it seems it is also introducing a loop which prevents garbage collecting
			//
			// huStorageFactory.addHUStorageListener(new IHUStorageListener()
			// {
			// @Override
			// public void onQtyChanged(final HUStorageChangeEvent event)
			// {
			// final I_M_HU hu = event.getHUStorage().getM_HU();
			// final IAttributeStorage attributeStorage = getHUAttributeStorageFactory().getAttributeStorage(hu);
			// if (attributeStorage instanceof IHUStorageListener)
			// {
			// final IHUStorageListener delegate = (IHUStorageListener)attributeStorage;
			// delegate.onQtyChanged(event);
			// }
			// }
			// });
		}
		return huStorageFactory;
	}

	@Override
	public void setHUStorageFactory(final IHUStorageFactory huStorageFactory)
	{
		Check.assumeNotNull(huStorageFactory, "huStorageFactory not null");
		this.huStorageFactory = huStorageFactory;
	}

	@Override
	public IAttributeStorageFactory getHUAttributeStorageFactory()
	{
		if (_attributesStorageFactoryInitialized)
		{
			// shall not be null at this point
			return _attributesStorageFactory;
		}

		if (_attributesStorageFactory == null)
		{
			_attributesStorageFactory = Services.get(IAttributeStorageFactoryService.class).createHUAttributeStorageFactory();
		}

		final IHUStorageFactory huStorageFactory = getHUStorageFactory();
		_attributesStorageFactory.setHUStorageFactory(huStorageFactory);
		_attributesStorageFactoryInitialized = true;

		return _attributesStorageFactory;
	}

	@Override
	public void setHUAttributeStorageFactory(final IAttributeStorageFactory attributesStorageFactory)
	{
		Check.assumeNotNull(attributesStorageFactory, "attributesStorageFactory not null");
		Check.assume(!_attributesStorageFactoryInitialized, "attributesStorageFactory not already initialized");
		_attributesStorageFactory = attributesStorageFactory;
	}

	@Override
	public IHUPackingMaterialsCollector<IHUPackingMaterialCollectorSource> getHUPackingMaterialsCollector()
	{

		return _destroyedHUPackingMaterialsCollector;
	}

	@Override
	public IMutableHUContext setHUPackingMaterialsCollector(@NonNull final IHUPackingMaterialsCollector<IHUPackingMaterialCollectorSource> huPackingMaterialsCollector)
	{
		this._destroyedHUPackingMaterialsCollector = huPackingMaterialsCollector;
		return this;
	}

	@Override
	public CompositeHUTrxListener getTrxListeners()
	{
		if (_trxListeners == null)
		{
			final CompositeHUTrxListener trxListeners = new CompositeHUTrxListener();

			// Add system registered listeners
			final IHUTrxBL huTrxBL = Services.get(IHUTrxBL.class);
			trxListeners.addListeners(huTrxBL.getHUTrxListenersList());

			_trxListeners = trxListeners;
		}
		return _trxListeners;
	}

	public void addEmptyHUListener(@NonNull final EmptyHUListener emptyHUListener)
	{
		emptyHUListeners.add(emptyHUListener);
	}

	@Override
	public List<EmptyHUListener> getEmptyHUListeners()
	{
		return ImmutableList.copyOf(emptyHUListeners);
	}
}
