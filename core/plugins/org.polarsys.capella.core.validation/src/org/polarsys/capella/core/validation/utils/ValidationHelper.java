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
package org.polarsys.capella.core.validation.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.validation.model.Category;
import org.eclipse.emf.validation.model.EvaluationMode;
import org.eclipse.emf.validation.service.ConstraintRegistry;
import org.eclipse.emf.validation.service.IBatchValidator;
import org.eclipse.emf.validation.service.IConstraintDescriptor;
import org.eclipse.emf.validation.service.IValidator;
import org.eclipse.emf.validation.service.ModelValidationService;

import org.polarsys.capella.core.validation.filter.CapellaConstraintFilter;


/**
 * Utility class for EMF validation use.
 *
 */
public class ValidationHelper {

  static {
    ModelValidationService.getInstance().loadXmlConstraintDeclarations();
  }

  /**
   * Get all constraints contributed via the EMF Validation framework 
   * @return
   */
  public static List<IConstraintDescriptor> getAllConstraintDescriptors() {
    return new ArrayList<IConstraintDescriptor>(ConstraintRegistry.getInstance().getAllDescriptors());
  }
  
  /**
   * Get all constraints contributed via the EMF Validation framework for Capella purposes
   * e.g. rules stored into the Capella constraint category 
   * @return
   */
  public static List<IConstraintDescriptor> getAllCapellaConstraintDescriptors() {
    List<IConstraintDescriptor> result = new ArrayList<IConstraintDescriptor>();
    for (IConstraintDescriptor icd: ConstraintRegistry.getInstance().getAllDescriptors()) {
      Set<Category> categories = icd.getCategories();
      for (Category category : categories) {
        String categoryPath = category.getPath();
        if (categoryPath.startsWith(CapellaConstraintFilter.CAPELLA_CONSTRAINT_CATEGORY_PATH)) {
          result.add(icd);
          break;
        }
      }
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  /**
   * Create a new filtering validator.
   * @param ruleIDs the IDs of constraints to validate.
   */
  public static <T, V extends IValidator<T>> V newValidator(Collection<String> ruleIDs, EvaluationMode<T> mode) {
    IValidator<T> validator = ModelValidationService.getInstance().newValidator(mode);
    validator.addConstraintFilter((constraint, target) -> ruleIDs.contains(constraint.getId()));
    return (V) validator;
  }

  /**
   * Creates and configures a standard batch validator for usage in Capella.
   *  - Live constraints are enabled
   *  - Report Sucesses is disabled
   *  - CapellaConstraintFilter is installed
   */
  public static IBatchValidator newDefaultCapellaBatchValidator() {
    IBatchValidator defaultBatchValidator = ModelValidationService.getInstance().newValidator(EvaluationMode.BATCH);
    defaultBatchValidator.setIncludeLiveConstraints(true);
    defaultBatchValidator.setReportSuccesses(false);
    defaultBatchValidator.addConstraintFilter(CapellaConstraintFilter.INSTANCE);
    return defaultBatchValidator;
  }

  
}
