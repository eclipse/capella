/*******************************************************************************
 * Copyright (c) 2006, 2014 THALES GLOBAL SERVICES.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/
package org.polarsys.capella.core.dashboard.hyperlinkadapter.sa;

import org.eclipse.sirius.business.api.session.Session;
import org.polarsys.capella.common.data.modellingcore.ModelElement;
import org.polarsys.capella.core.dashboard.hyperlinkadapter.AbstractNewDiagramHyperlinkAdapter;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.model.helpers.ModelQueryHelper;
import org.polarsys.capella.core.sirius.analysis.IDiagramNameConstants;

/**
 * Create a System Actors / Operational Entities Traceability Matrix.
 */
public class NewSystemActorsOperationalEntitiesMatrixAdapter extends AbstractNewDiagramHyperlinkAdapter {
  /**
   * Constructor.
   * @param capellaProject_p
   * @param session_p
   */
  public NewSystemActorsOperationalEntitiesMatrixAdapter(Session session_p) {
    super(session_p);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getDiagramName() {
    return IDiagramNameConstants.SYSTEM_ACTOR_OPERATIONAL_ACTOR_DIAGRAM_NAME;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ModelElement getModelElement(Project project_p) {
    return ModelQueryHelper.getSystemAnalysis(project_p);
  }
}