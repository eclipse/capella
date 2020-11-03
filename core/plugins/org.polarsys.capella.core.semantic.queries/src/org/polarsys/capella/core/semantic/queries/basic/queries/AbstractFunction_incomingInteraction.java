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

import org.polarsys.capella.core.data.ctx.SystemFunction;
import org.polarsys.capella.core.data.fa.AbstractFunction;
import org.polarsys.capella.core.data.fa.FunctionalExchange;
import org.polarsys.capella.core.data.helpers.fa.services.FunctionExt;
import org.polarsys.capella.core.data.la.LogicalFunction;
import org.polarsys.capella.core.data.oa.OperationalActivity;
import org.polarsys.capella.core.data.pa.PhysicalFunction;
import org.polarsys.capella.common.helpers.query.IQuery;

/**
 * This query allows to get the incoming interactions from an AbstractFuntion
 * 
 */
public class AbstractFunction_incomingInteraction implements IQuery {

  /**
	 * 
	 */
  public AbstractFunction_incomingInteraction() {
    // Does nothing
  }

  /**
   * Get the incoming flows: ownedFlowPorts.incomingFlows
   * 
   * @see org.polarsys.capella.common.helpers.query.IQuery#compute(java.lang.Object)
   */
  public List<Object> compute(Object object) {
    List<Object> result = new ArrayList<Object>(0);
    
    if (object instanceof OperationalActivity) {
      getInComingExchagnes(object, result);
    }else if (object instanceof SystemFunction || object instanceof LogicalFunction || object instanceof PhysicalFunction ) {
      getInComingExchagnes(object, result);
    }
    
    return result;
  }

  /**
   * @param object
   * @param result
   */
  private void getInComingExchagnes(Object object, List<Object> result) {
    List<FunctionalExchange> incomingExchange = FunctionExt.getIncomingExchange((AbstractFunction) object);
    result.addAll(incomingExchange);
  }
}
