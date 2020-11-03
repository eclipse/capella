/*******************************************************************************
 * Copyright (c) 2006, 2020 THALES GLOBAL SERVICES.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/

package org.polarsys.capella.core.semantic.queries.basic.queries;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import org.polarsys.capella.core.data.fa.FunctionalChain;
import org.polarsys.capella.common.helpers.query.IQuery;

/**
 *
 */
public class FunctionalChain_owningFunction implements IQuery {

	/**
	 * 
	 */
	public FunctionalChain_owningFunction() {
	  // do nothing
	}

	/** 
	 *  
	 * current.ownerFunction
	 * 
	 * @see org.polarsys.capella.common.helpers.query.IQuery#compute(java.lang.Object)
	 */
	public List<Object> compute(Object object) {
		List<Object> result = new ArrayList<Object>();
		if (object instanceof FunctionalChain) {
			FunctionalChain fc = (FunctionalChain) object;
			EObject container = fc.eContainer();
			if(null != container)
				result.add(container);
		}
		return result;
	}
}
