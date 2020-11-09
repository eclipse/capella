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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.polarsys.capella.core.data.cs.Interface;
import org.polarsys.capella.core.data.fa.ComponentExchange;
import org.polarsys.capella.core.data.fa.FunctionalExchange;
import org.polarsys.capella.core.transition.common.handlers.scope.IScopeFilter;
import org.polarsys.kitalpha.transposer.rules.handler.rules.api.IContext;

public class DefaultValidationScopeFilter implements IScopeFilter {

  @Override
  public boolean isValidScopeElement(EObject element, IContext context) {

    ScopedDiagnostician.Context tContext = (ScopedDiagnostician.Context) context;

    // Only validate extended scope interfaces if one provider and one requirer are in the scope
    if (element instanceof Interface) {
      boolean providerInScope = ((Interface) element).getProvidingComponentPorts().stream().anyMatch(cp -> EcoreUtil.isAncestor(tContext.getValidationRoots(), cp));
      boolean requirerInScope = ((Interface) element).getRequiringComponentPorts().stream().anyMatch(cp -> EcoreUtil.isAncestor(tContext.getValidationRoots(), cp));
      return providerInScope && requirerInScope;
    }

    // Only validate extended scope exchanges if source and target are in the scope
    if (element instanceof ComponentExchange) {
      ComponentExchange ce = (ComponentExchange) element;
      boolean sourceInScope = EcoreUtil.isAncestor(tContext.getValidationRoots(), ce.getSource());
      boolean targetInScope = EcoreUtil.isAncestor(tContext.getValidationRoots(), ce.getTarget());
      return sourceInScope && targetInScope;
    }
    if (element instanceof FunctionalExchange) {
      FunctionalExchange fe = (FunctionalExchange) element;
      boolean sourceInScope = EcoreUtil.isAncestor(tContext.getValidationRoots(), fe.getSource());
      boolean targetInScope = EcoreUtil.isAncestor(tContext.getValidationRoots(), fe.getTarget());
      return sourceInScope && targetInScope;
    }

    return true;
  }

}
