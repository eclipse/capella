/*******************************************************************************
 * Copyright (c) 2019, 2020 THALES GLOBAL SERVICES.
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
package org.polarsys.capella.test.diagram.filters.ju.sab;

import java.util.Arrays;
import java.util.List;

import org.polarsys.capella.core.sirius.analysis.constants.IFilterNameConstants;

public class HideComponentPortsWithoutExchangesForSAB extends FiltersForSAB {

  @Override
  protected String getFilterName() {
    return IFilterNameConstants.FILTER_SAB_HIDE_COMPONENT_PORTS_WITHOUT_EXCHANGES;
  }

  @Override
  protected List<String> getFilteredObjetIDs() {
    return Arrays.asList(new String[] { COMPONENT_PORT_WITHOUT_EXCHANGES_ID });
  }

}
