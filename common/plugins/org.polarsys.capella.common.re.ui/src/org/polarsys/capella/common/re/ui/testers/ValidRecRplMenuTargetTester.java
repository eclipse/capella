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
package org.polarsys.capella.common.re.ui.testers;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.polarsys.capella.core.diagram.helpers.TitleBlockHelper;

public class ValidRecRplMenuTargetTester extends PropertyTester {

  public static final String PROPERTY_IS_VALID_MENU_TARGET = "isValidRecRplTarget"; //$NON-NLS-1$

  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

    if (PROPERTY_IS_VALID_MENU_TARGET.equals(property)) {
      return isSemanticElement(receiver) || isValidGraphicalElement(receiver);
    }

    throw new IllegalArgumentException("Unknown property: " + property); //$NON-NLS-1$
  }

  private boolean isSemanticElement(Object element) {
    return element instanceof EObject;
  }

  private boolean isValidGraphicalElement(Object element) {
    if (element instanceof EditPart) {
      return !TitleBlockHelper.isTitleBlockEditPart((EditPart) element);
    }

    return false;
  }

}
