package de.metas.handlingunits.allocation.transfer.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.Services;
import org.adempiere.util.time.SystemTime;
import org.compiere.model.I_M_Product;

import com.google.common.base.Preconditions;

import de.metas.handlingunits.IHUContext;
import de.metas.handlingunits.IHUPIItemProductDAO;
import de.metas.handlingunits.IHUTrxBL;
import de.metas.handlingunits.IHandlingUnitsBL;
import de.metas.handlingunits.IHandlingUnitsDAO;
import de.metas.handlingunits.IMutableHUContext;
import de.metas.handlingunits.allocation.IAllocationDestination;
import de.metas.handlingunits.allocation.IAllocationRequest;
import de.metas.handlingunits.allocation.IAllocationSource;
import de.metas.handlingunits.allocation.IHUContextProcessor;
import de.metas.handlingunits.allocation.IHUProducerAllocationDestination;
import de.metas.handlingunits.allocation.impl.HUListAllocationSourceDestination;
import de.metas.handlingunits.allocation.impl.HULoader;
import de.metas.handlingunits.allocation.impl.IMutableAllocationResult;
import de.metas.handlingunits.document.IHUDocumentLine;
import de.metas.handlingunits.model.I_M_HU;
import de.metas.handlingunits.model.I_M_HU_PI_Item;
import de.metas.handlingunits.model.I_M_HU_PI_Item_Product;

/**
 * This class is used by {@link HUSplitBuilder} but can also be called from others. It does the "generic" splitting work while HUSplitBuilder has a lot of setters that can help callers in setting up a particular {@link IHUProducerAllocationDestination} which is then passed to this class.
 * If you have your own {@link IAllocationSource}, {@link IAllocationDestination} etc, you can call this class directly.
 * 
 * @author metas-dev <dev@metasfresh.com>
 *
 */
public class HUSplitBuilderCoreEngine
{
	//
	// Services
	private final transient IHUTrxBL huTrxBL = Services.get(IHUTrxBL.class);
	private final transient IHandlingUnitsBL handlingUnitsBL = Services.get(IHandlingUnitsBL.class);
	private final transient IHandlingUnitsDAO handlingUnitsDAO = Services.get(IHandlingUnitsDAO.class);
	private final transient IHUPIItemProductDAO piipDAO = Services.get(IHUPIItemProductDAO.class);

	private final IHUContext huContextInitital;
	private final I_M_HU huToSplit;
	private final Function<IHUContext, IAllocationRequest> requestProvider;
	private final IAllocationDestination destination;

	private I_M_HU_PI_Item tuPIItem;

	private IHUDocumentLine documentLine;
	private boolean allowPartialUnloads;

	private HUSplitBuilderCoreEngine(final IHUContext huContextInitital,
			final I_M_HU huToSplit,
			final Function<IHUContext, IAllocationRequest> requestProvider,
			final IAllocationDestination destination)
	{
		this.huContextInitital = huContextInitital;
		this.huToSplit = huToSplit;
		this.requestProvider = requestProvider;
		this.destination = destination;
	}

	/**
	 * Creates and returns a new instance. Note that all four parameters are mandatory.
	 * 
	 * @param huContextInitital an intitial HU context. the {@link #performSplit()} method will create and run a {@link IHUContextProcessor} which will internally work with a mutable copy of the given context.
	 * @param huToSplit
	 * @param requestProvider a function which will be applied from within the {@link #performSplit()} method to get the actual request, using the "inner" mutable copy of {@code huContextInitital}.
	 * @param destination
	 * @return
	 */
	public static HUSplitBuilderCoreEngine of(final IHUContext huContextInitital,
			final I_M_HU huToSplit,
			final Function<IHUContext, IAllocationRequest> requestProvider,
			final IAllocationDestination destination)
	{
		return new HUSplitBuilderCoreEngine(
				Preconditions.checkNotNull(huContextInitital, "Param 'huContextInitital' may not be null"),
				Preconditions.checkNotNull(huToSplit, "Param 'huToSplit' may not be null"),
				Preconditions.checkNotNull(requestProvider, "Param 'requestProvider' may not be null"),
				Preconditions.checkNotNull(destination, "Param 'destination' may not be null"));
	}

	/**
	 * If this instance's included {@link #destination} member is a {@link IHUProducerAllocationDestination}, then this method changes if by setting values from the included {@link #huToSplit}.
	 * Otherwise this method does nothing.
	 * 
	 * @return
	 * 
	 * @task 06902: Make sure the split keys inherit the status and locator.
	 */
	public HUSplitBuilderCoreEngine withPropagateHUValues()
	{
		final IHUProducerAllocationDestination huAllocationDestination = destinationCastOrNull();
		if (huAllocationDestination != null)
		{
			huAllocationDestination.setHUStatus(huToSplit.getHUStatus());
			huAllocationDestination.setC_BPartner(huToSplit.getC_BPartner());
			huAllocationDestination.setC_BPartner_Location_ID(huToSplit.getC_BPartner_Location_ID());
			huAllocationDestination.setM_Locator(huToSplit.getM_Locator());
		}

		return this;
	}

	private IHUProducerAllocationDestination destinationCastOrNull()
	{
		if (destination instanceof IHUProducerAllocationDestination)
		{
			return (IHUProducerAllocationDestination)destination;
		}
		return null;
	}

	/**
	 * This method only has an impact if this instance's {@link #destination} is {@link IHUProducerAllocationDestination}.
	 * 
	 * @param documentLine
	 * @return
	 */
	public HUSplitBuilderCoreEngine withDocumentLine(final IHUDocumentLine documentLine)
	{
		this.documentLine = documentLine;
		return this;
	}

	public HUSplitBuilderCoreEngine withTuPIItem(final I_M_HU_PI_Item tuPIItem)
	{
		this.tuPIItem = tuPIItem;
		return this;
	}

	/**
	 * Set a value to be passed to the {@link HULoader#setAllowPartialUnloads(boolean)} when {@link #performSplit()} is done. The default is {@code false} because of backwards compatibility.
	 * 
	 * @param allowPartialUnloads
	 * @return
	 */
	public HUSplitBuilderCoreEngine withAllowPartialUnloads(final boolean allowPartialUnloads)
	{
		this.allowPartialUnloads = allowPartialUnloads;
		return this;
	}

	/**
	 * Performs the actual split. Also see {@link #of(IHUContext, I_M_HU, Function, IAllocationDestination)}.
	 * <p>
	 * Note: if this instance's {@link #destination} is not {@code instanceof} {@link IHUProducerAllocationDestination}, then this method will always return an empty list.
	 * 
	 * @return
	 */
	public List<I_M_HU> performSplit()
	{
		final List<I_M_HU> splitHUs = new ArrayList<I_M_HU>();

		huTrxBL.createHUContextProcessorExecutor(huContextInitital)
				.run(new IHUContextProcessor()
				{
					@Override
					public IMutableAllocationResult process(final IHUContext localHuContext)
					{
						// Make a copy of the processing context, we will need to modify it
						final IMutableHUContext localHuContextCopy = localHuContext.copyAsMutable();

						// Register our split HUTrxListener so that the other listeners' onSplit() methods will be called
						localHuContextCopy.getTrxListeners().addListener(HUSplitBuilderTrxListener.instance);

						// Perform the actual split
						final List<I_M_HU> splitHUsInTrx = performSplit0(localHuContextCopy);

						//
						// Make created HUs to be out-of-transaction to be used elsewhere
						for (final I_M_HU splitHU : splitHUsInTrx)
						{
							InterfaceWrapperHelper.setTrxName(splitHU, ITrx.TRXNAME_None);
							splitHUs.add(splitHU);
						}

						return NULL_RESULT; // we don't care about the result
					}
				});

		return splitHUs;
	}

	private List<I_M_HU> performSplit0(final IHUContext localHuContextCopy)
	{
		//
		// using thread-inherited to let this split key work in transaction and out of transaction
		InterfaceWrapperHelper.setTrxName(huToSplit, ITrx.TRXNAME_ThreadInherited);

		//
		// Source: our handling unit
		final IAllocationSource source = HUListAllocationSourceDestination.of(huToSplit);

		final IAllocationRequest request = requestProvider.apply(localHuContextCopy);

		//
		// Perform allocation
		HULoader.of(source, destination)
				.setAllowPartialLoads(true)
				.setAllowPartialUnloads(allowPartialUnloads)
				.load(request);
		// NOTE: we are not checking if everything was fully allocated because we can leave the remaining Qty into initial "huToSplit"

		final List<I_M_HU> result;

		final IHUProducerAllocationDestination huProducerAllocationDestination = destinationCastOrNull();
		if (huProducerAllocationDestination != null)
		{
			//
			// Get created HUs in contextProvider's transaction
			final List<I_M_HU> createdHUs = huProducerAllocationDestination.getCreatedHUs();

			//
			// Transfer PI Item Product from HU to split to all HUs that we created
			setM_HU_PI_Item_Product(huToSplit, request.getProduct(), createdHUs);

			//
			// Assign createdHUs to documentLine
			// NOTE: Even if IHUTrxListener implementations are already assigning HUs to documents,
			// the top level HUs (i.e. LUs) are not assigned because only those HUs on which it were material transactions are detected
			if (documentLine != null)
			{
				documentLine.getHUAllocations().addAssignedHUs(createdHUs);
			}

			result = huProducerAllocationDestination.getCreatedHUs();
		}
		else
		{
			result = Collections.emptyList();
		}
		//
		// Destroy empty HUs from huToSplit
		handlingUnitsBL.destroyIfEmptyStorage(localHuContextCopy, huToSplit);

		return result;
	}

	/**
	 * Set HU PIIP in-depth, recursively
	 *
	 * @param sourceHU
	 * @param husToConfigure
	 */
	private void setM_HU_PI_Item_Product(final I_M_HU sourceHU, final I_M_Product product, final List<I_M_HU> husToConfigure)
	{
		final I_M_HU_PI_Item_Product piip = getM_HU_PI_Item_ProductToUse(sourceHU, product);
		if (piip == null)
		{
			return;
		}

		for (final I_M_HU hu : husToConfigure)
		{
			if (handlingUnitsBL.isLoadingUnit(hu))
			{
				setM_HU_PI_Item_Product(sourceHU, product, handlingUnitsDAO.retrieveIncludedHUs(hu));
				continue;
			}
			else if (handlingUnitsBL.isTransportUnit(hu))
			{
				setM_HU_PI_Item_Product(sourceHU, product, handlingUnitsDAO.retrieveIncludedHUs(hu));
			}

			if (hu.getM_HU_PI_Item_Product_ID() > 0)
			{
				continue;
			}

			hu.setM_HU_PI_Item_Product(piip);
			InterfaceWrapperHelper.save(hu);
		}
	}

	private I_M_HU_PI_Item_Product getM_HU_PI_Item_ProductToUse(final I_M_HU hu, final I_M_Product product)
	{
		if (tuPIItem == null)
		{
			return null;
		}
		final I_M_HU_PI_Item_Product piip = piipDAO.retrievePIMaterialItemProduct(tuPIItem, hu.getC_BPartner(), product, SystemTime.asDate());
		return piip;
	}
}
