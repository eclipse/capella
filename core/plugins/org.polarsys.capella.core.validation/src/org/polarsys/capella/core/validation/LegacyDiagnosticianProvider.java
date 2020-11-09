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
package org.polarsys.capella.core.validation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.util.Diagnostician;

import org.polarsys.capella.core.model.handler.validation.AbstractDiagnosticianProvider;
import org.polarsys.capella.core.model.handler.validation.CapellaDiagnostician;

/**
 * Provides the old Tree+Part/Type diagnostician.
 */
public class LegacyDiagnosticianProvider extends AbstractDiagnosticianProvider {

  @Override
  public Diagnostician getDiagnostician(AdapterFactory adapterFactory, IProgressMonitor progressMonitor) {
    return new CapellaDiagnostician(CapellaValidationActivator.getDefault().getCapellaValidatorRegistry(), adapterFactory, progressMonitor);
  }

}
