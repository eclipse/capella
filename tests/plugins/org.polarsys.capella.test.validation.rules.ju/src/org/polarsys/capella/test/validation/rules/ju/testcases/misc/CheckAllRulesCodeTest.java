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
package org.polarsys.capella.test.validation.rules.ju.testcases.misc;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.validation.model.EvaluationMode;
import org.eclipse.emf.validation.service.IBatchValidator;
import org.eclipse.emf.validation.service.IConstraintDescriptor;
import org.eclipse.emf.validation.service.IConstraintFilter;
import org.eclipse.emf.validation.service.ModelValidationService;
import org.eclipse.osgi.util.NLS;
import org.junit.Assert;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.libraries.model.CapellaModel;
import org.polarsys.capella.core.validation.utils.ValidationHelper;
import org.polarsys.capella.test.framework.api.OracleDefinition;
import org.polarsys.capella.test.validation.rules.ju.testcases.ValidationRuleTestCase;

/**
 * Check "code" of all constraints on a various enough model.
 * This test apply each rules and check their state after operation.
 *
 */
public class CheckAllRulesCodeTest extends ValidationRuleTestCase {
  
  @Override
  protected String getRequiredTestModel() {
    return "testOnRules";
  }

  @Override
  protected EClass getTargetedEClass() {
    return null;
  }

  @Override
  protected String getRuleID() {
    return null;
  }

  @Override
  protected List<OracleDefinition> getOracleDefinitions() {
    return null;
  }

  /**
   * Check if a given rule is not in an error state
   * @param icd_p the rule {@link IConstraintDescriptor}
   * @param msg_p msg in case of failure, could be <code>null</code> (default one will be applied.)
   */
  static public void checkIfConstraintIsInError(IConstraintDescriptor icd_p, String msg_p) {
    String msg = msg_p == null ? "Constraint {0} is disabled due to error. Please check its code." : msg_p;
    Assert.assertFalse(NLS.bind(msg, icd_p.getId()), icd_p.isError());
  }

  @Override
  // FIXME This doesn't execute disabled rules, also, why not validate just once?!
  // FIXME
  public void test() throws Exception {
    CapellaModel model = getTestModel(getRequiredTestModel());
    Project project = model.getProject(getSessionForTestModel(getRequiredTestModel()).getTransactionalEditingDomain());
    if (project != null) {
      IBatchValidator validator = ModelValidationService.getInstance().newValidator(EvaluationMode.BATCH);
      for (final IConstraintDescriptor icd : ValidationHelper.getAllConstraintDescriptors()) {
        IConstraintFilter filter = new IConstraintFilter() {
          public boolean accept(IConstraintDescriptor constraint_p, EObject target_p) {
            return constraint_p.getId().equals(icd.getId());
          }
        };

        try {
          validator.addConstraintFilter(filter);
          checkIfConstraintIsInError(icd, "Constraint {0} is errored before its application. Please check its code.");
          validator.validate(project);
          checkIfConstraintIsInError(icd, "Constraint {0} is errored after its application. Please check its code.");
        } catch (Exception ex) {
          ex.printStackTrace();
        } finally {
          validator.removeConstraintFilter(filter);
        }
      }
    }
  }
}
