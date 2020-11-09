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
package org.polarsys.capella.core.validation.scope;

import org.polarsys.capella.core.validation.CapellaValidationActivator;

public class DefaultScopedDiagnosticianProvider extends ScopedDiagnosticianProvider {

  public DefaultScopedDiagnosticianProvider() {
    super("org.polarsys.capella.core.validation", 
          "org.polarsys.capella.core.validation.extended",
        CapellaValidationActivator.getDefault().getCapellaValidatorRegistry());
  }
}
