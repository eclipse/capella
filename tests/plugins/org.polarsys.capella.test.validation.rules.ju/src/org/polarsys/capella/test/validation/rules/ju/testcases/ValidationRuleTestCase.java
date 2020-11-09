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
package org.polarsys.capella.test.validation.rules.ju.testcases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.validation.service.ConstraintRegistry;
import org.eclipse.emf.validation.service.IBatchValidator;
import org.eclipse.emf.validation.service.IConstraintDescriptor;
import org.eclipse.emf.validation.service.IConstraintFilter;
import org.eclipse.emf.validation.service.ModelValidationService;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.polarsys.capella.common.helpers.EObjectExt;
import org.polarsys.capella.common.helpers.EcoreUtil2;
import org.polarsys.capella.common.helpers.validation.ConstraintStatusDiagnostic;
import org.polarsys.capella.common.re.ReAbstractElement;
import org.polarsys.capella.common.tools.report.appenders.reportlogview.LightMarkerRegistry;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.libraries.model.ICapellaModel;
import org.polarsys.capella.core.model.handler.markers.ICapellaValidationConstants;
import org.polarsys.capella.core.validation.CapellaValidationActivator;
import org.polarsys.capella.core.validation.EValidatorAdapter;
import org.polarsys.capella.core.validation.utils.ValidationHelper;
import org.polarsys.capella.test.framework.api.BasicTestCase;
import org.polarsys.capella.test.framework.api.OracleDefinition;

/**
 * This class is a generic test case for validation rule test.<br>
 * To use it, create a test case that inherits from this class and implement abstract methods, that are<br>
 * (see method documentation for more details) :<br>
 * <br>
 * - getTestProjectName()<br>
 * - getTargetedEClass()<br>
 * - getRuleID()<br>
 * - getOracleDefinitions()<br>
 * 
 * @author Erwan Brottier
 * @contributor Joao Barata
 */
public abstract class ValidationRuleTestCase extends BasicTestCase {

  // these values are obtained by using methods defined in concrete test cases
  protected String ruleID = getRuleID();
  protected EClass targetedEClass = getTargetedEClass();
  protected boolean quickFix = getCheckQuickFix();

  // internal variables
  protected IConstraintDescriptor ruleDescriptor;
  protected IConstraintFilter filter;
  protected IBatchValidator validator;
  protected Hashtable<String, OracleDefinition> objectID2OracleDefinition = new Hashtable<String, OracleDefinition>();

  // stores the status of the rule before to launch the test
  private boolean _ruleWasDisabled;

  // these methods must be overridden by concrete test cases
  /** returns the name of the test project folder (by default in the folder "model") */
  protected abstract String getRequiredTestModel();

  /** returns the class targeted by the tested rule (should be automatically found from the rule but not accessible) */
  protected abstract EClass getTargetedEClass();

  /** returns tested rule ID */
  protected abstract String getRuleID();

  /** returns the oracle definition to be checked */
  protected abstract List<OracleDefinition> getOracleDefinitions();
  
  protected boolean getCheckQuickFix() {
    return false;
  }
  
  protected IStatus testCheckQuickFix(List<IMarker> markers) {
    return Status.OK_STATUS;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // init validator
    validator = ValidationHelper.newDefaultCapellaBatchValidator();

    ModelValidationService.getInstance().loadXmlConstraintDeclarations();// load the xml definition of constraints
    // get the descriptor of the rule to test
    ConstraintRegistry registry = ConstraintRegistry.getInstance();

    if (ruleID != null) {
      ruleDescriptor = registry.getDescriptor(ruleID);
      if (ruleDescriptor == null) {
        throw new InternalError("Rule " + ruleID + " does not exist. Test can not be performed"); //$NON-NLS-1$//$NON-NLS-2$
      }

      if (!ruleDescriptor.isEnabled()) {
        ruleDescriptor.setEnabled(true);
        _ruleWasDisabled = true;
      }

      // create the filter and add it to the validator
      filter = new IConstraintFilter() {
        @SuppressWarnings("synthetic-access")
        public boolean accept(IConstraintDescriptor constraint_p, EObject target_p) {
          return ruleDescriptor == constraint_p;
        }
      };
      validator.addConstraintFilter(filter);
    }
  }

  @Override
  public List<String> getRequiredTestModels() {
    if (getRequiredTestModel() != null) {
      return Collections.singletonList(getRequiredTestModel());
    } else {
      return Collections.emptyList();
    }
  }

  protected List<EObject> getTestScope(ICapellaModel model) {
    List<EObject> scope = new ArrayList<EObject>();
    Project project = model.getProject(getSessionForTestModel(getRequiredTestModel()).getTransactionalEditingDomain());
    if (project != null) {
      for (EObject object : EObjectExt.getAll(project, targetedEClass)) {
        if (object instanceof CapellaElement || object instanceof ReAbstractElement) {
          scope.add(object);
        }
      }
    }
    return scope;
  }

  public void test() throws Exception {
    // get the objects to validate (only CapellaElement because the oracle is based on object ID)
    ICapellaModel model = getTestModel(getRequiredTestModel());
    List<EObject> objectsToValidate = getTestScope(model);
    List<OracleDefinition> oracleDefinitions = getOracleDefinitions();
    List<IMarker> markers = new ArrayList<IMarker>();
    if (oracleDefinitions != null) {
      // prepare oracle table
      for (OracleDefinition definition : oracleDefinitions) {
        objectID2OracleDefinition.put(definition.getObjectID(), definition);
      }
      // check the validation result for each objects
      List<EObject> failedObjects = new ArrayList<EObject>();
      Diagnostician diagnostician = new Diagnostician(CapellaValidationActivator.getDefault().getCapellaValidatorRegistry());
      Map<Object, Object> contextEntries = new HashMap<>();
      contextEntries.put(EValidatorAdapter.BATCH_VALIDATOR, validator);
      
      for (EObject object : objectsToValidate) {
        String objectID = getId(object);
        OracleDefinition oracleDef = objectID2OracleDefinition.get(objectID);
        Diagnostic diagnostic = diagnostician.validate(object, contextEntries);

        if ((diagnostic.getSeverity() == Diagnostic.OK) && (oracleDef != null) && oracleDef.getNbExpectedErrors() > 0) {
          fail("Validation rule " + ruleID + " has not detected an error on object " + objectID + " while it must be the case"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } else {
          if (diagnostic.getSeverity() != Diagnostic.OK) {
            for(Diagnostic diag : diagnostic.getChildren()) {
              IMarker marker = LightMarkerRegistry.getInstance().createMarker(EcoreUtil2.getFile(object.eResource()), diag, ICapellaValidationConstants.CAPELLA_MARKER_ID);
              if(marker != null) {
                markers.add(marker);
              }
            }
            assertExpectedRuleHasBeenThrown(diagnostic, object);
            if (oracleDef != null) {
              oracleDef.countOneError();
            } else {
              failedObjects.add(object);
            }
          }
        }
      }
      // check all objects to be validated before throwing a message
      if (!failedObjects.isEmpty()) {
        String objects = failedObjects.stream().map(elt -> " - " + getId(elt)).collect(Collectors.joining("\n"));
        fail(NLS.bind("Validation rule {0} has detected an error on object(s): \n{1}\nwhile it must not be the case.", ruleID, objects));
      }
      // check that some errors has not been detected according to the number of error occurences for each object in
      // error
      for (OracleDefinition oracleDef : oracleDefinitions) {
        int nbExpectedErrors = oracleDef.getNbExpectedErrors();
        int nbFoundErrors = oracleDef.getNbFoundErrors();
        if (nbFoundErrors < nbExpectedErrors) {
          fail("Validation rule " + ruleID + " has only detected " + nbFoundErrors + " of " + nbExpectedErrors + " expected error(s) on object " + oracleDef.getObjectID()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        } else if (nbFoundErrors > nbExpectedErrors) {
          fail("Validation rule " + ruleID + " has detected " + nbFoundErrors + " error(s) instead of " + nbExpectedErrors + " error(s) on object " + oracleDef.getObjectID()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
      }
      
      if(getCheckQuickFix()) {
        IStatus status = testCheckQuickFix(markers);
        if(status != null && !status.isOK()) {
          fail(status.getMessage());
        }
      }
    }
  }

  protected void assertExpectedRuleHasBeenThrown(Diagnostic diagnostic, EObject object) {
    assertFalse(diagnostic.getChildren().isEmpty());
    Diagnostic nestedDiag = diagnostic.getChildren().get(0);
    
    assertTrue("Validation rule " + ruleID
        + " has detected another kind of error, please check EMF integrity of the model: "+ getAirdURI(object),
        nestedDiag instanceof ConstraintStatusDiagnostic);

    String ruleIdThrown = ((ConstraintStatusDiagnostic) nestedDiag).getConstraintStatus().getConstraint()
        .getDescriptor().getId();
    assertEquals("Validation rule " + ruleIdThrown + " has detected an error but " + ruleID + " expected",
        ruleID, ruleIdThrown);
  }

  protected String getAirdURI(EObject object) {
    return SessionManager.INSTANCE.getSession(object).getSessionResource().getURI().toPlatformString(true);
  }

  protected String getId(EObject object) {
    if (object instanceof CapellaElement) {
      return ((CapellaElement) object).getId();
    }
    if (object instanceof ReAbstractElement) {
      return ((ReAbstractElement) object).getId();
    }
    throw new IllegalArgumentException(object.eClass().getName() + "is not supported as a validation targeted EClass");
  }

  @Override
  protected void tearDown() throws Exception {
    if (_ruleWasDisabled && ruleDescriptor != null) {
      ruleDescriptor.setEnabled(false);
    }

    if (validator != null) {
      // remove the filter from the validator
      validator.removeConstraintFilter(filter);
    }
    
    super.tearDown();
  }

}
