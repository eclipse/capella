/*******************************************************************************
 * Copyright (c) 2019, 2020, THALES GLOBAL SERVICES.
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
package org.polarsys.capella.test.diagram.tools.ju.testsuites.partial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.polarsys.capella.test.diagram.tools.ju.xab.showhide.functions.ShowHideFunctionsLA;
import org.polarsys.capella.test.diagram.tools.ju.xab.showhide.functions.ShowHideFunctionsOA;
import org.polarsys.capella.test.diagram.tools.ju.xab.showhide.functions.ShowHideFunctionsPAOnBehaviorComponents;
import org.polarsys.capella.test.diagram.tools.ju.xab.showhide.functions.ShowHideFunctionsPAOnDeployedComponents;
import org.polarsys.capella.test.framework.api.BasicTestArtefact;
import org.polarsys.capella.test.framework.api.BasicTestSuite;

import junit.framework.Test;

public class XABShowHideFunctionsTestSuite extends BasicTestSuite {

  public static Test suite() {
    return new XABShowHideFunctionsTestSuite();
  }
  
  public List<String> getRequiredTestModels() {
    return Arrays.asList("EmptyProject");
  }

  @Override
  protected List<BasicTestArtefact> getTests() {
    List<BasicTestArtefact> tests = new ArrayList<>();
    tests.add(new ShowHideFunctionsOA());
    tests.add(new ShowHideFunctionsPAOnBehaviorComponents());
    tests.add(new ShowHideFunctionsPAOnDeployedComponents());
    tests.add(new ShowHideFunctionsLA());

    return tests;
  }

}
