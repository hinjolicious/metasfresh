package de.metas.contracts.impl;

import static org.adempiere.model.InterfaceWrapperHelper.newInstance;
import static org.adempiere.model.InterfaceWrapperHelper.save;

import java.util.Properties;

import org.adempiere.ad.modelvalidator.IModelInterceptorRegistry;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.model.IContextAware;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.test.AdempiereTestHelper;
import org.adempiere.util.Check;
import org.adempiere.util.Services;
import org.compiere.model.I_AD_Client;
import org.compiere.model.I_AD_Org;
import org.compiere.util.Env;

import de.metas.contracts.interceptor.MainValidator;
import de.metas.contracts.model.I_C_Flatrate_Term;
import de.metas.contracts.model.I_C_SubscriptionProgress;
import de.metas.inoutcandidate.model.I_M_IolCandHandler;
import de.metas.invoicecandidate.model.I_C_ILCandHandler;

/**
 * This class sets up basic master data like partners, addresses, users, flatrate conditions, flarate transitions that can be used in testing.
 *
 * @author metas-dev <dev@metasfresh.com>
 *
 */
public class FlatrateTermTestHelper
{
	private final String invoiceCandClassname = "de.metas.contracts.invoicecandidate.FlatrateTermInvoiceCandidateHandler";
	private final String shipmentCandClassname = "de.metas.contracts.inoutcandidate.SubscriptionShipmentScheduleHandler";

	//
	// Initialization flags
	private boolean initialized = false;
	private boolean initAdempiere = true;

	public I_AD_Client adClient;
	public I_AD_Org adOrg;

	public Properties ctx;
	public String trxName;
	

	public final IContextAware contextProvider = new IContextAware()
	{
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
	};

	public Properties getCtx()
	{
		return ctx;
	}
	
	public I_AD_Client getClient()
	{
		return adClient;
	}
	
	public I_AD_Org getOrg()
	{
		return adOrg;
	}
	
	public String getTrxName()
	{
		return trxName;
	}

	public IContextAware getContextProvider()
	{
		return contextProvider;
	}
	
	/**
	 * Invokes {@link #FlatrateTermTestHelper(boolean)} with init=<code>true</code>.
	 */
	public FlatrateTermTestHelper()
	{
		this(true);
	}

	/**
	 *
	 * @param init if <code>true</code>, the the constructor directly calls {@link #init()}.
	 */
	public FlatrateTermTestHelper(final boolean init)
	{
		if (init)
		{
			init();
		}
		
		setupModuleInterceptors_Contracts_Full();
	}

	public void setInitAdempiere(final boolean initAdempiere)
	{
		Check.assume(!initialized, "helper not initialized");
		this.initAdempiere = initAdempiere;
	}

	/**
	 * Final, because its called by a constructor.
	 */
	public final void init()
	{
		if (initialized)
		{
			return;
		}

		if (initAdempiere)
		{
			AdempiereTestHelper.get().init();
		}

		ctx = Env.getCtx();

		adClient = InterfaceWrapperHelper.create(ctx, I_AD_Client.class, ITrx.TRXNAME_None);
		InterfaceWrapperHelper.save(adClient);
		Env.setContext(ctx, Env.CTXNAME_AD_Client_ID, adClient.getAD_Client_ID());

		adOrg = InterfaceWrapperHelper.create(ctx, I_AD_Org.class, ITrx.TRXNAME_None);
		InterfaceWrapperHelper.save(adOrg);
		Env.setContext(ctx, Env.CTXNAME_AD_Org_ID, adOrg.getAD_Org_ID());
		
		createCILCandHandler();
		createMIolCandHandler();
		
		initialized = true;

		afterInitialized();
	}

	protected void afterInitialized()
	{
		// nothing here
	}

	/**
	 * Setup module interceptors: "de.metas.contracts" module - FULL (interceptors, factories, etc), like in production (used by some integration tests).
	 * 
	 * <b>Important:</b> if you do the full monty with interceptors, then you also need to annotate the respective test class like this:
	 * 
	 * <pre>
	&#64;RunWith(SpringRunner.class)
	&#64;SpringBootTest(classes= StartupListener.class)
	 * </pre>
	 * 
	 * Otherwise, tests will probably fail due to spring application context.
	 */
	protected final void setupModuleInterceptors_Contracts_Full()
	{
		Services.get(IModelInterceptorRegistry.class)
				.addModelInterceptor(new MainValidator());
	}
	
	private void createCILCandHandler()
	{
		final I_C_ILCandHandler handler = newInstance(I_C_ILCandHandler.class);
		handler.setName(invoiceCandClassname );
		handler.setClassname(invoiceCandClassname);
		handler.setTableName(I_C_Flatrate_Term.Table_Name);
		save(handler);
	}
	
	private void createMIolCandHandler()
	{
		final I_M_IolCandHandler handler = newInstance(I_M_IolCandHandler.class);
		handler.setClassname(shipmentCandClassname);
		handler.setIsActive(true);
		handler.setTableName(I_C_SubscriptionProgress.Table_Name);
		save(handler);
	}
}
