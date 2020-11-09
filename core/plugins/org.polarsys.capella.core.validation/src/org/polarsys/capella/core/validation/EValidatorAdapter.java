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

import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EObjectValidator;
import org.eclipse.emf.validation.model.IConstraintStatus;
import org.eclipse.emf.validation.service.IBatchValidator;

/**
 * An adapter that plugs the EMF Model Validation Service API into the {@link org.eclipse.emf.ecore.EValidator} API.
 */
public class EValidatorAdapter extends EObjectValidator {

  /**
   * Clients may provide their own batch validator instance by 
   * storing it under this key in the validation context.
   */
  public static final Object BATCH_VALIDATOR = IBatchValidator.class;

  private final IBatchValidator defaultValidator;

  private IBatchValidator getValidator(Map<Object, Object> context) {
    IBatchValidator validator = (IBatchValidator) context.get(EValidatorAdapter.BATCH_VALIDATOR);
    if (validator == null) {
      validator = defaultValidator;
    }
    return validator;
  }

  /**
   * Create an adapter that uses the argument as the default batch validator.
   * Clients that invoke validation can override this validator
   * by passing their own under the {@link #BATCH_VALIDATOR} key in 
   * the validation context.
   */
  public EValidatorAdapter(IBatchValidator defaultBatchValidator) {
    defaultValidator = defaultBatchValidator;
  }

  /**
   * @see org.eclipse.emf.ecore.util.EObjectValidator#validate(org.eclipse.emf.ecore.EObject, org.eclipse.emf.common.util.DiagnosticChain, java.util.Map)
   */
  @Override
  public boolean validate(EObject eObject_p, DiagnosticChain diagnostics_p, Map<Object, Object> context_p) {
    return validate(eObject_p.eClass(), eObject_p, diagnostics_p, context_p);
  }

  /**
   * Implements validation by delegation to the EMF validation framework.
   */
  @Override
  public boolean validate(EClass eClass_p, EObject eObject_p, DiagnosticChain diagnostics_p, Map<Object, Object> context_p) {
    IStatus status = Status.OK_STATUS;
    if (context_p.get(ROOT_OBJECT) == null) {
      status = getValidator(context_p).validate(eObject_p, new NullProgressMonitor());
      appendDiagnostics(status, diagnostics_p);
    }
    return super.validate(eClass_p, eObject_p, diagnostics_p, context_p) && status.isOK();
  }


  /**
   * Converts a status result from the EMF validation service to diagnostics.
   * @param status_p the EMF validation service's status result
   * @param diagnostics_p a diagnostic chain to accumulate results on
   */
  protected void appendDiagnostics(IStatus status_p, DiagnosticChain diagnostics_p) {
    if (status_p.isMultiStatus()) {
      IStatus[] children = status_p.getChildren();
      for (int i = 0; i < children.length; i++) {
        appendDiagnostics(children[i], diagnostics_p);
      }
    } else if (status_p instanceof IConstraintStatus) {
      diagnostics_p.add(new BasicDiagnostic(status_p.getSeverity(), status_p.getPlugin(), status_p.getCode(), status_p.getMessage(),
                                            ((IConstraintStatus) status_p).getResultLocus().toArray()));
    }
  }
}
