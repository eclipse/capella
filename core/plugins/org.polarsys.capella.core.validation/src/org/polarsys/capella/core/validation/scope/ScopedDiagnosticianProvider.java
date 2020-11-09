/*******************************************************************************
 * Copyright (c) 2020 THALES GLOBAL SERVICES.
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
package org.polarsys.capella.core.validation.scope;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.polarsys.capella.core.model.handler.validation.AbstractDiagnosticianProvider;

public class ScopedDiagnosticianProvider extends AbstractDiagnosticianProvider {

  private final String purpose;
  private final String mapping;
  private final EValidator.Registry registry;
  
  protected ScopedDiagnosticianProvider(String purpose, String mapping, EValidator.Registry registry) {
    this.purpose = purpose;
    this.mapping = mapping;
    this.registry = registry;
  }

  @Override
  public Diagnostician getDiagnostician(AdapterFactory adapterFactory, IProgressMonitor progressMonitor) {
    return new ScopedDiagnostician(
        registry, adapterFactory, purpose, mapping) {
        @Override
      protected boolean doValidate(EValidator eValidator, EClass eClass, EObject eObject, DiagnosticChain diagnostics,
          Map<Object, Object> context) {
        progressMonitor.worked(1);
        return super.doValidate(eValidator, eClass, eObject, diagnostics, context);
      }

      @Override
      protected void reportProgress() {
        progressMonitor.worked(1);
      }
    };
  }

}
