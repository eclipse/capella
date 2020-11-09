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
package org.polarsys.capella.core.model.handler.validation;

import java.util.Collection;
import java.util.Map;

import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.util.Diagnostician;

/**
 * A {@link Diagnostician} that accepts a collection of {@link EObject}s 
 * for validation.
 */
public abstract class MultiobjectDiagnostician extends Diagnostician {

  public MultiobjectDiagnostician(EValidator.Registry registry) {
    super(registry);
  }

  public abstract boolean validate(Collection<EObject> eObjects, DiagnosticChain diagnostics, Map<Object, Object> context);

}
