/*******************************************************************************
 * Copyright (c) 2020 THALES GLOBAL SERVICES.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/
package org.polarsys.capella.test.diagram.filters.ju.pd;

import java.util.Arrays;
import java.util.List;

import org.polarsys.capella.core.sirius.analysis.constants.IFilterNameConstants;
import org.polarsys.capella.test.diagram.filters.ju.DiagramTitleBlockFilterTestCase;

public class HideElementTitleBlocksForPD extends DiagramTitleBlockFilterTestCase {

  private final String ELEMENT_TITLE_BLOCK_DATA_ID = "_vQbUUI7OEeqR99_BeAxvxA";
  private final String ELEMENT_TITLE_BLOCK_INTERFACES_ID = "_voTo0I7OEeqR99_BeAxvxA";

  @Override
  protected String getFilterName() {
    return IFilterNameConstants.FILTER_COMMON_HIDE_ELEMENT_TITLE_BLOCKS;
  }

  @Override
  protected List<String> getFilteredObjetIDs() {
    return Arrays.asList(new String[] { ELEMENT_TITLE_BLOCK_DATA_ID,
        ELEMENT_TITLE_BLOCK_INTERFACES_ID});
  }
  
  @Override
  protected String getDiagramName() {
    return "[PD] Interfaces Title Blocks";
  }
}