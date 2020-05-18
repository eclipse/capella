/*******************************************************************************
 * Copyright (c) 2019 THALES GLOBAL SERVICES.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/
package org.polarsys.capella.test.diagram.filters.ju.cdi;

import java.util.ArrayList;
import java.util.List;

import org.polarsys.capella.test.framework.api.BasicTestArtefact;
import org.polarsys.capella.test.framework.api.BasicTestSuite;

public class CDIDiagramFiltersTestSuite extends BasicTestSuite {

  @Override
  protected List<BasicTestArtefact> getTests() {
    List<BasicTestArtefact> tests = new ArrayList<>();
    tests.add(new HideDiagramTitleBocksForCDI());
    tests.add(new HideElementTitleBocksForCDI());
    tests.add(new HideInterfaceContentsForCDI());
    tests.add(new HideInterfacesForCDI());
    tests.add(new HideExchangeItemsDetailsInInterfacesForCDI());
    tests.add(new HideExchangeItemElementsForCDI());
    tests.add(new HideExchangeItemsForCDI());
    tests.add(new HideComponentPortsForCDI());
    tests.add(new HideUseLinksForCDI());
    tests.add(new HideImplementationLinksForCDI());
    tests.add(new HideProvideLinksForCDI());
    tests.add(new HideRequireLinksForCDI());
    tests.add(new HideCommunicationLinksForCDI());
    tests.add(new HideGeneralizationLinksForCDI());
    tests.add(new HideTechnicalInterfacesForCDI());
    tests.add(new ShowModifiersForCDI());
    tests.add(new HidePropertyValuesForCDI());
    return tests;
  }
}
