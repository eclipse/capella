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
package org.polarsys.capella.core.transition.system.handlers.scope;

import org.polarsys.capella.core.data.cs.CsPackage;
import org.polarsys.capella.core.transition.common.handlers.scope.EReferenceScopeRetriever;

public class DeployedElementRetriever extends EReferenceScopeRetriever {
  public DeployedElementRetriever() {
    super(CsPackage.Literals.ABSTRACT_DEPLOYMENT_LINK__DEPLOYED_ELEMENT);
  }
}
