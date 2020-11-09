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
package org.polarsys.capella.core.validation.filter;

import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.validation.model.Category;
import org.eclipse.emf.validation.service.IConstraintDescriptor;
import org.eclipse.emf.validation.service.IConstraintFilter;

/**
 * Only constraints in the capella.category {@link Category} pass
 * this filter.
 */
public final class CapellaConstraintFilter implements IConstraintFilter {

  public static final CapellaConstraintFilter INSTANCE = new CapellaConstraintFilter();
  
  private CapellaConstraintFilter() {
  }

  /**
   * Category path for Common constraints.
   */
  public static final String CAPELLA_CONSTRAINT_CATEGORY_PATH = "capella.category"; //$NON-NLS-1$

  /**
   * Accept all constraints owned by the Common constraint category and the one defined by {@link #getFilterCategoryPath()}.
   * @see org.eclipse.emf.validation.service.IConstraintFilter#accept(org.eclipse.emf.validation.service.IConstraintDescriptor, org.eclipse.emf.ecore.EObject)
   */
  public boolean accept(IConstraintDescriptor constraint_p, EObject target_p) {
    boolean result = true;
    Set<Category> categories = constraint_p.getCategories();
    for (Category category : categories) {
      String categoryPath = category.getPath();
      if (!categoryPath.startsWith(CAPELLA_CONSTRAINT_CATEGORY_PATH)) {
        result = false;
        break;
      }
    }
    return result;
  }
}
