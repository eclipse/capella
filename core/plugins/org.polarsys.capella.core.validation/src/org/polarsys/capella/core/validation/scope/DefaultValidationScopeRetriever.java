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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.eclipse.emf.ecore.EObject;
import org.polarsys.capella.core.data.fa.FaPackage;
import org.polarsys.capella.core.data.information.InformationPackage;
import org.polarsys.capella.core.transition.common.handlers.scope.EReferenceScopeRetriever;
import org.polarsys.capella.core.transition.common.handlers.scope.IScopeRetriever;
import org.polarsys.capella.core.transition.system.handlers.scope.DeployedElementRetriever;
import org.polarsys.capella.core.transition.system.handlers.scope.PartTypeRetriever;
import org.polarsys.kitalpha.transposer.rules.handler.rules.api.IContext;

public class DefaultValidationScopeRetriever implements IScopeRetriever {

  @Override
  public Collection<? extends EObject> retrieveRelatedElements(EObject element, IContext context) {
    Collection<IScopeRetriever> subs = Arrays.asList(
        new PartTypeRetriever(),
        new DeployedElementRetriever(),
        new EReferenceScopeRetriever(FaPackage.Literals.ABSTRACT_FUNCTIONAL_BLOCK__ALLOCATED_FUNCTIONS),
        new EReferenceScopeRetriever(FaPackage.Literals.COMPONENT_PORT__COMPONENT_EXCHANGES),
        new EReferenceScopeRetriever(FaPackage.Literals.FUNCTION_OUTPUT_PORT__OUTGOING_FUNCTIONAL_EXCHANGES),
        new EReferenceScopeRetriever(FaPackage.Literals.FUNCTION_INPUT_PORT__INCOMING_FUNCTIONAL_EXCHANGES),
        new EReferenceScopeRetriever(InformationPackage.Literals.PORT__PROVIDED_INTERFACES),
        new EReferenceScopeRetriever(InformationPackage.Literals.PORT__REQUIRED_INTERFACES)
        );
    Collection<EObject> result = new LinkedHashSet<EObject>();
    subs.forEach(sub -> result.addAll(sub.retrieveRelatedElements(element, context)));
    return result;
  }

}
