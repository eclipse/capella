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
package org.polarsys.capella.core.platform.sirius.ui.preferences;

import java.util.Collection;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.widgets.Group;
import org.polarsys.capella.core.commands.preferences.service.AbstractDefaultPreferencePage;
import org.polarsys.capella.core.model.handler.validation.DiagnosticianProviderRegistry;
import org.polarsys.capella.core.model.handler.validation.DiagnosticianProviderRegistry.Descriptor;
import org.polarsys.capella.core.platform.sirius.ui.actions.CapellaActionsActivator;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we can use the
 * field support built into JFace that allows us to create a page that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that belongs to the main plug-in class. That way, preferences can be
 * accessed directly via the preference store.
 */

public class CapellaValidationPreferencesPage extends AbstractDefaultPreferencePage {

  /*
   * 
   */
  public static final String PROPERTY_PAGE_ID = "org.polarsys.capella.core.platform.sirius.ui.actions.preferences.CapellaValidationPreferences"; //$NON-NLS-1$

  public CapellaValidationPreferencesPage() {
    super(CapellaActionsActivator.PLUGIN_ID);
    setDescription(Messages.ModelValidationPreferencePage_Description);
  }

  /**
   * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to manipulate various types of preferences. Each field editor
   * knows how to save and restore itself.
   */
  @Override
  public void createFieldEditors() {
    Group generalGroup = createGroup(Messages.CapellaValidationPreferencesPage_General_Group_Label, Messages.CapellaValidationPreferencesPage_General_Group_Tooltip, getFieldEditorParent());
    addField(new BooleanFieldEditor(ICapellaValidationPreferences.P_CLEAN_PREVIOUS_VALIDATION_RESULTS,
        Messages.ModelValidationPreferencePage_DeletePreviousResults_Title, generalGroup));

    Collection<Descriptor> descriptors = DiagnosticianProviderRegistry.INSTANCE.getDescriptors();
    String[][] labelsAndValues = new String[descriptors.size()][2];

    int i = 0;
    for (Descriptor d : descriptors) {
      labelsAndValues[i][0] = d.getName();
      labelsAndValues[i][1] = d.getID();
      i++;
    }

    addField(new RadioGroupFieldEditor(ICapellaValidationPreferences.P_DIAGNOSTICIAN_PROVIDER, Messages.CapellaValidationPreferencesPage_ValidationScope_Group_Label , 1, labelsAndValues, getFieldEditorParent(), true));

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getPageTitle() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getPageDescription() {
    return Messages.ModelValidationPreferencePage_Description;
  }

}
