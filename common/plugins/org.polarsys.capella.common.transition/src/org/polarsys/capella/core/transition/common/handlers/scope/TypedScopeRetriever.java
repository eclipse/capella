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

import org.eclipse.emf.ecore.EObject;
import org.polarsys.kitalpha.transposer.rules.handler.rules.api.IContext;

public abstract class TypedScopeRetriever<T> implements IScopeRetriever {

  private final Class<T> clazz;

  public TypedScopeRetriever(Class<T> clazz) {
    this.clazz = clazz;
  }

  @SuppressWarnings("unchecked")
  @Override
  public final Collection<? extends EObject> retrieveRelatedElements(EObject element, IContext context) {
    if (clazz.isInstance(element)) {
      return doRetrieveRelatedElements((T) element, context);
    }
    return Collections.emptyList();
  }

  protected Collection<? extends EObject> doRetrieveRelatedElements(T element, IContext context) {
    return Collections.emptyList();
  }

}
