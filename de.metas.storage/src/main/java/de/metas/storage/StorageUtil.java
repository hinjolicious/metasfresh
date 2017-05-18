package de.metas.storage;

import java.util.function.Predicate;

import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.mm.attributes.api.ASICopy;
import org.adempiere.mm.attributes.api.IAttributeDAO;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.Services;
import org.compiere.Adempiere;
import org.compiere.model.I_M_AttributeInstance;
import org.compiere.model.I_M_AttributeSetInstance;
import org.compiere.util.DB;

import de.metas.storage.model.I_M_Attribute;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StorageUtil
{

	/**
	 * Creates a string the contains the all "storage relevant" attributes of the given ASI.
	 * Can be used for grouping and matching.
	 * 
	 * @param attributeSetInstanceID
	 * @return an attribute-storage relevant string, or then empty string. never {@code null}.
	 */
	public String getASIKey(final int attributeSetInstanceID)
	{
		if (Adempiere.isUnitTestMode())
		{
			return "!AsiKey is created by a DB function!";
		}
		final String asiKey = DB.getSQLValueString(ITrx.TRXNAME_None,
				"SELECT GenerateHUStorageASIKey(?, '')",               // important to get an empty string instead of NULL
				attributeSetInstanceID);

		return asiKey;
	}

	/**
	 * If the given {@code asi} is {@code null} or if all {@link I_M_AttributeInstance}'s are storage relevant, then the given {@code asi} is returned.
	 * Otherwise a new {@link I_M_AttributeSetInstance} is created which contains only copies of the storage-relevant attribute instances from the given {@code asi}.
	 * 
	 * @param asi
	 * @return
	 */
	public I_M_AttributeSetInstance mkStorageRelevantASI(final I_M_AttributeSetInstance asi)
	{
		if (asi == null)
		{
			return asi; // nothing to do
		}

		final Predicate<I_M_AttributeInstance> aiStorageRelevant = ai ->
			{
				final I_M_Attribute attribute = InterfaceWrapperHelper.create(ai.getM_Attribute(), I_M_Attribute.class);
				return attribute.isMatchHUStorage();
			};

		final IAttributeDAO attributesDAO = Services.get(IAttributeDAO.class);
		final boolean noNeedToCreateNewASI = attributesDAO.retrieveAttributeInstances(asi)
				.stream()
				.allMatch(aiStorageRelevant);

		if (noNeedToCreateNewASI)
		{
			return asi; // nothing to do
		}

		// return a filtered copy
		return ASICopy
				.newInstance(asi)
				.filter(aiStorageRelevant)
				.copy();
	}
}
