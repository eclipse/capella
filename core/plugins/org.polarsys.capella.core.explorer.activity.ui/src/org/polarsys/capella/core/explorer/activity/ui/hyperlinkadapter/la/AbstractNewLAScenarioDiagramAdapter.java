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
package org.polarsys.capella.core.explorer.activity.ui.hyperlinkadapter.la;

import org.eclipse.emf.ecore.EObject;
import org.polarsys.capella.common.data.modellingcore.ModelElement;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.la.LogicalArchitecture;
import org.polarsys.capella.core.explorer.activity.ui.hyperlinkadapter.AbstractNewScenarioDiagramAdapter;
import org.polarsys.capella.core.explorer.activity.ui.hyperlinkadapter.ModelCreationHelper;
import org.polarsys.capella.core.model.helpers.ModelQueryHelper;

public abstract class AbstractNewLAScenarioDiagramAdapter extends AbstractNewScenarioDiagramAdapter {
  @Override
  protected ModelElement getModelElement(EObject rootSemanticModel) {
    LogicalArchitecture logicalArchitecture = ModelQueryHelper.getLogicalArchitecture((Project) rootSemanticModel);
    return ModelCreationHelper.selectCapability((Project) rootSemanticModel, logicalArchitecture);
  }
}