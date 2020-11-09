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
package org.polarsys.capella.core.validation.ui.actions;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.validation.preferences.EMFModelValidationPreferences;
import org.eclipse.emf.validation.service.IBatchValidator;
import org.eclipse.emf.validation.service.IConstraintDescriptor;
import org.eclipse.ui.statushandlers.StatusManager;
import org.polarsys.capella.common.helpers.validation.IValidationConstants;
import org.polarsys.capella.common.mdsofa.common.constant.ICommonConstants;
import org.polarsys.capella.core.platform.sirius.ui.actions.CapellaValidateAction;
import org.polarsys.capella.core.validation.EValidatorAdapter;
import org.polarsys.capella.core.validation.service.EPFConstraintFilter;
import org.polarsys.capella.core.validation.ui.CapellaValidationUIActivator;
import org.polarsys.capella.core.validation.utils.ValidationHelper;

public class EPFValidationAction extends CapellaValidateAction {

  public final static String EPF_EXTNAME = "epf"; //$NON-NLS-1$

  public final static String EPF_EXTPATTERN = "." + EPF_EXTNAME; //$NON-NLS-1$

  public final static String KEY_PREFIX = "/instance/org.eclipse.emf.validation//con.disabled/"; //$NON-NLS-1$
  
  public final static List<IConstraintDescriptor> constraints = ValidationHelper.getAllCapellaConstraintDescriptors();

  private IFile epf;

  protected Properties properties;

  /**
   * @param isRootAction
   * @param epf
   */
  public EPFValidationAction(IFile epf) {
    this.epf = epf;
  }

  @Override
  protected Map<Object, Object> getContextEntries() {
    Map<Object,Object> entries = new HashMap<Object,Object>(super.getContextEntries());
    IBatchValidator validator = ValidationHelper.newDefaultCapellaBatchValidator();
    validator.addConstraintFilter(new EPFConstraintFilter(properties));
    entries.put(EValidatorAdapter.BATCH_VALIDATOR, validator);
    return entries;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void run() {
    IStatus status = Status.OK_STATUS;
    try (InputStream stream = epf.getContents()){
      properties = new Properties();
      properties.load(stream);
    } catch (IOException exception) {
      status = new Status(IStatus.ERROR, 
           CapellaValidationUIActivator.getDefault().getPluginId(),
           exception.getLocalizedMessage(), exception); 
    } catch (CoreException e) {
      status = new Status(e.getStatus().getSeverity(), 
          CapellaValidationUIActivator.getDefault().getPluginId(),
          e.getLocalizedMessage(), e); 
    }
    if (status.isOK()) {
      Map<String, Boolean> oldPreferences = disableEPFConstraint();
      try {
        super.run();
      } finally {
        restoreOldPreference(oldPreferences);
      }
    } else {
      StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.LOG);
    }
  }


  // The epf file only contains an entry for a rule if the current enablement state of the rule 
  // is different to the default enablement state of the rule.
  // This means that all enablement states must be reset to the default before applying 
  // the values from the epf file.
  // After validation, the current enablement state is restored.
  private Map<String, Boolean> disableEPFConstraint() {
    Map<String,Boolean> oldPreferences = new HashMap<>();
    for (IConstraintDescriptor descriptor : constraints) {
      String id = descriptor.getId();
      oldPreferences.put(id, EMFModelValidationPreferences.isConstraintDisabled(id));
      boolean disabled = false;
      String value = properties.getProperty(EPFConstraintFilter.KEY_PREFIX + id);
      if (value != null) {
        disabled = Boolean.parseBoolean(value);
      } else {
        disabled = EMFModelValidationPreferences.isConstraintDisabledByDefault(id);
      }
      EMFModelValidationPreferences.setConstraintDisabled(id, disabled);
    }
    EMFModelValidationPreferences.save();
    return oldPreferences;
  }


  private void restoreOldPreference(Map<String, Boolean> oldPreferences) {
    for (String key : oldPreferences.keySet()) {
      EMFModelValidationPreferences.setConstraintDisabled(key, oldPreferences.get(key));
    }
    EMFModelValidationPreferences.save();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getText() {
    return epf.getName().replace(EPF_EXTPATTERN, ICommonConstants.EMPTY_STRING);
  }


  protected void modifyMarker(IMarker marker) {
    final String epf = getEpf() == null ? "Default" : getEpf().getName(); //$NON-NLS-1$
    try {
      marker.setAttribute(IValidationConstants.TAG_PREFERENCE_EPF_FILE, epf);
    } catch (CoreException e) {
      CapellaValidationUIActivator.getDefault().getLog().log(
          new Status(e.getStatus().getSeverity(), CapellaValidationUIActivator.getDefault().getBundle().getSymbolicName(), e.getMessage(), e));
    }
  }

  /**
   * @return the epf
   */
  public IFile getEpf() {
    return epf;
  }

}
