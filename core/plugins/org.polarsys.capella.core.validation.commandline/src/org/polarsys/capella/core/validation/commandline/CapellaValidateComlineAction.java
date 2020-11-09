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
package org.polarsys.capella.core.validation.commandline;

import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.polarsys.capella.common.ef.ExecutionManager;
import org.polarsys.capella.common.helpers.TransactionHelper;
import org.polarsys.capella.core.platform.sirius.ui.actions.CapellaValidateAction;

/**
 */
public class CapellaValidateComlineAction extends CapellaValidateAction {

  private Resource resourceToValidate;
  private Diagnostic diagnostic;
  private String providerId;

  public void setSelectedObjects(List<EObject> selectedObjects) {
    this.selectedObjects = selectedObjects;
  }

  public void setDiagnosticianProviderId(String id) {
    this.providerId = id;
  }

  @Override
  protected String getDiagnosticianProviderId() {
    if (providerId != null) {
      return providerId;
    }
    return super.getDiagnosticianProviderId();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void run() {
    if(isSetEditingDomain()){
      // works fine
      diagnostic = super.validate(new NullProgressMonitor());
      handleDiagnostic(diagnostic);
    }
  }

  private boolean isSetEditingDomain(){
    if(domain == null && !selectedObjects.isEmpty()){
      ExecutionManager executionManager = TransactionHelper.getExecutionManager(selectedObjects);
      domain = executionManager != null ? executionManager.getEditingDomain() : TransactionUtil.getEditingDomain(selectedObjects.get(0));      
    }
    return domain != null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void handleDiagnostic(Diagnostic diagnostic) {
    // Create markers
    for (Diagnostic childDiagnostic : diagnostic.getChildren()) {
      eclipseResourcesUtil.createMarkers(resourceToValidate, childDiagnostic);
    }
  }

  /**
   * @param airdSemanticModel
   */
  public void setResource(Resource airdSemanticModel) {
    resourceToValidate = airdSemanticModel;
  }
  
  public void deleteMarkers() {
    for (Diagnostic childDiagnostic : diagnostic.getChildren()) {
      eclipseResourcesUtil.deleteMarkers(childDiagnostic);
    }
  }
}
