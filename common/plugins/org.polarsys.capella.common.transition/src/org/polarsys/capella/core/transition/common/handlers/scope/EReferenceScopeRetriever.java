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
package org.polarsys.capella.core.transition.common.handlers.scope;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.polarsys.kitalpha.transposer.rules.handler.rules.api.IContext;

public class EReferenceScopeRetriever implements IScopeRetriever {
  private final EReference ref;

  public EReferenceScopeRetriever(EReference ref) {
    this.ref = ref;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<? extends EObject> retrieveRelatedElements(EObject element, IContext context) {
    EClass refClass = ref.getEContainingClass();
    if (refClass.isInstance(element)) {
      if (element.eIsSet(ref)) {
        Object target = element.eGet(ref);
        if (ref.isMany()) {
          return (Collection<? extends EObject>) element.eGet(ref);
        } else if (target != null) {
          return (Collection<? extends EObject>) Collections.singleton(target);
        }
      }
    }
    return Collections.emptyList();
  }

}
