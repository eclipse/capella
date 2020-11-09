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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.validation.model.IConstraintStatus;
import org.eclipse.emf.validation.service.IBatchValidator;
import org.polarsys.capella.common.helpers.validation.ConstraintStatusDiagnostic;

/**
 * A specialized validator adapter that handles rule-aware constraints.
 *
 */
// FIXME what are 'rule-aware' constraints..
public class CapellaValidatorAdapter extends EValidatorAdapter {

  /**
   * @see EValidatorAdapter
   */
  public CapellaValidatorAdapter(IBatchValidator defaultBatchValidator) {
    super(defaultBatchValidator);
  }

  /**
   * @see org.polarsys.capella.core.validation.EValidatorAdapter#appendDiagnostics(org.eclipse.core.runtime.IStatus,
   *      org.eclipse.emf.common.util.DiagnosticChain)
   *      FIXME but... i _want_ multistatus constraints!! clients have asked for it multiple times..
   */
  @Override
  protected void appendDiagnostics(IStatus status_p, DiagnosticChain diagnostics_p) {
    // Deal recursively with multi status.
    if (status_p.isMultiStatus()) {
      IStatus[] children = status_p.getChildren();
      for (IStatus element : children) {
        appendDiagnostics(element, diagnostics_p);
      }
    } else if (status_p instanceof IConstraintStatus) {
      diagnostics_p.add(new ConstraintStatusDiagnostic((IConstraintStatus) status_p));
    }

  }


}
