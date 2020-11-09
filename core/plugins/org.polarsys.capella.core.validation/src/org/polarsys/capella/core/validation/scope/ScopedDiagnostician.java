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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.util.EObjectValidator;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.polarsys.capella.core.model.handler.validation.MultiobjectDiagnostician;
import org.polarsys.capella.core.transition.common.ExtensionHelper;
import org.polarsys.capella.core.transition.common.constants.ISchemaConstants;
import org.polarsys.capella.core.transition.common.handlers.scope.IScopeFilter;
import org.polarsys.capella.core.transition.common.handlers.scope.IScopeRetriever;
import org.polarsys.kitalpha.transposer.generic.GenericContext;
import org.polarsys.kitalpha.transposer.rules.handler.rules.api.IContext;

/**
 * ScopedDiagnosticians calculate a set of validation roots from 
 * a set of {@link IScopeRetriever}s and {@link IScopeFilter}s. These filters and retrievers
 * are selected by providing the mapping/purpose id under which they are registered
 * via the org.polarsys.capella.core.transition.handlers extension point.
 */
public class ScopedDiagnostician extends MultiobjectDiagnostician {

  private final AdapterFactory adapterFactory;
  private final String mapping;
  private final String purpose;

  public ScopedDiagnostician(EValidator.Registry registry, AdapterFactory adapterFactory, String purpose, String mapping) {
    super(registry);
    this.adapterFactory = adapterFactory;
    this.purpose = purpose;
    this.mapping = mapping;
  }

  public static class Context extends GenericContext {
    private Collection<? extends EObject> selection;
    private Collection<? extends EObject> roots;
    Context(HashMap<Object, Object> map){
      _repository = map;
    }
    public Collection<? extends EObject> getSelection() {
      return Collections.unmodifiableCollection(selection);
    }
    public Collection<? extends EObject> getValidationRoots(){
      return roots;
    }
    @Override
    public void reset() {
      super.reset();
      selection = null;
      roots = null;
    }
  }

  public Map<Object, Object> createDefaultContext(){
    HashMap<Object, Object> defaultContext = new HashMap<>();
    IContext gc = new Context(defaultContext);
    gc.put(EValidator.SubstitutionLabelProvider.class, this);
    gc.put(EValidator.class, this);
    gc.put(IContext.class, gc);
    return defaultContext;
  }

  @Override
  public String getObjectLabel(EObject eObject) {
    if (adapterFactory != null && !eObject.eIsProxy()) {
      IItemLabelProvider itemLabelProvider = (IItemLabelProvider)adapterFactory.adapt(eObject, IItemLabelProvider.class);
      if (itemLabelProvider != null) {
        return itemLabelProvider.getText(eObject);
      }
    }
    return super.getObjectLabel(eObject);
  }

  public boolean validate(Collection<EObject> eObjects, DiagnosticChain diagnostics, Map<Object, Object> context) {

    Context tContext = (Context) context.get(IContext.class);
    tContext.reset();
    tContext.selection = eObjects;
    Collection<EObject> roots = new ArrayList<>();

    Collection<IScopeRetriever> retrievers = ExtensionHelper.<IScopeRetriever>collectFromExtensions(tContext, ISchemaConstants.EXTENSION_ID,
        ISchemaConstants.SCOPE_RETRIEVER, purpose, mapping).stream().filter(r -> r.init(tContext).isOK()).collect(Collectors.toList());

    Deque<EObject> work = new ArrayDeque<EObject>(eObjects);
    while (!work.isEmpty()) {
      EObject pseudoRoot = work.pop();
      if (!EcoreUtil.isAncestor(roots, pseudoRoot)) {
        for (TreeIterator<EObject> it = EcoreUtil.getAllContents(Collections.singleton(pseudoRoot)); it.hasNext();) {
          EObject next = it.next();
          if (roots.remove(next)) {
            it.prune();
          } else {
            reportProgress();
            retrievers.forEach(r->work.addAll(r.retrieveRelatedElements(next, tContext)));
          }
        }
        roots.add(pseudoRoot);
      }
    }
    retrievers.forEach(r -> r.dispose(tContext));

    //
    // Every IScopeFilter is given a chance to remove any of the root elements from the validation, unless the root element is part of the initial selection.
    //
    tContext.roots = roots;
    Collection<IScopeFilter> filters = ExtensionHelper.<IScopeFilter>collectFromExtensions(tContext, ISchemaConstants.EXTENSION_ID, ISchemaConstants.SCOPE_FILTER, purpose, mapping)
        .stream().filter(f -> f.init(tContext).isOK()).collect(Collectors.toList());

    for (Iterator<EObject> it = roots.iterator(); it.hasNext();) {
      EObject next = it.next();
      if (!EcoreUtil.isAncestor(tContext.getSelection(), next)) {
        if (filters.stream().anyMatch(f -> !f.isValidScopeElement(next, tContext))) {
          it.remove();
        }
      }
    }
    filters.forEach(f -> f.dispose(tContext));

    boolean result = true;
    for (EObject e : roots) {
      result &= super.validate(e, diagnostics, context);
      context.remove(EObjectValidator.ROOT_OBJECT);
    }
    return result;
  }

  @Override
  public boolean validate(EObject eObject, DiagnosticChain diagnostics, Map<Object, Object> context) {
    return validate(Collections.singleton(eObject), diagnostics, context);
  }

  /**
   * Subclasses can override this hook to report progress to the surrounding context.
   */
  protected void reportProgress() {}

}
