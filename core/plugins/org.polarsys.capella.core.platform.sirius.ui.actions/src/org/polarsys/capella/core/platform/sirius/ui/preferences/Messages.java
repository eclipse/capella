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

import org.eclipse.osgi.util.NLS;

/**
 *
 */
public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.polarsys.capella.core.platform.sirius.ui.preferences.messages"; //$NON-NLS-1$
  public static String DeletePreferencePage_ConfirmDeleteTitle;
  public static String DeletePreferencePage_DeleteChoice;
  public static String DeletePreferencePage_DeletePartsChoiceTitle;
  public static String DeletePreferencePage_Description;
  public static String DeletePreferencePage_Title;
  public static String CapellaPreferencePage_AirdFragmentFileExtension_Title;
  public static String CapellaPreferencePage_Description;
  public static String CapellaPreferencePage_DetectionVersion_Title;
  public static String CapellaPreferencePage_Title;
  public static String CapellaValidationPreferencesPage_General_Group_Label;
  public static String CapellaValidationPreferencesPage_General_Group_Tooltip;
  public static String CapellaValidationPreferencesPage_ValidationScope_Group_Tooltip;
  public static String CapellaValidationPreferencesPage_ValidationScope_Group_Label;
  public static String WizardPreferencePage_Description;
  public static String WizardPreferencePage_Title;
  public static String ModelValidationPreferencePage_Description;
  public static String ModelValidationPreferencePage_DeletePreviousResults_Title;
  public static String DeletePreferencePage_ProtectedElements_Title;
  public static String DeletePreferencePage_MultipartGroup_Title;
  public static String DeletePreferencePage_MultipartGroup_Description;

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  private Messages() {
    // Static initialization.
  }
}
