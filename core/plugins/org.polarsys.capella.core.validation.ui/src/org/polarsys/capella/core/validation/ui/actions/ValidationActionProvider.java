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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.edit.ui.action.ValidateAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

import org.polarsys.capella.core.commands.preferences.util.PreferencesHelper;
import org.polarsys.capella.core.platform.sirius.ui.actions.CapellaValidateAction;
import org.polarsys.capella.core.validation.ui.CapellaValidationUIActivator;

/**
 */
public class ValidationActionProvider extends CommonActionProvider {

  /*
   * 
   */
  private ValidateAction defaultValidationAction;
  /*
   * 
   */
  private List<ValidateAction> userValidationActions;
  /*
   * 
   */
  private ICommonViewerSite commonViewSite;
  /*
   * 
   */
  private ImageDescriptor imageDescriptor;

  /**
   * Constructs the contribution actions provider.
   */
  public ValidationActionProvider() {
    userValidationActions = new ArrayList<ValidateAction>();
  }

  /**
   * @see org.eclipse.ui.actions.ActionGroup#dispose()
   */
  @Override
  public void dispose() {
    ISelectionProvider selectionProvider = getActionSite().getViewSite().getSelectionProvider();

    if (null != defaultValidationAction) {
      selectionProvider.removeSelectionChangedListener(defaultValidationAction);
      defaultValidationAction = null;
    }

    for (ValidateAction action : userValidationActions) {
      selectionProvider.removeSelectionChangedListener(action);
    }
    userValidationActions.clear();

    super.dispose();
  }

  @Override
  protected boolean filterAction(org.eclipse.jface.action.IAction action) {
    return false;

  }

  /**
   * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.ICommonActionExtensionSite)
   */
  @Override
  public void init(ICommonActionExtensionSite site) {
    super.init(site);

    commonViewSite = site.getViewSite();
    if (!(commonViewSite instanceof ICommonViewerWorkbenchSite)) {
      return;
    }

  }

  /**
   * @param isRootAction
   * @param file
   * @param selectionProvider
   * @param imageDescriptor
   * @return
   */
  protected ValidateAction createValidationAction(IFile file, ISelectionProvider selectionProvider, ImageDescriptor imageDescriptor) {
    ValidateAction validationAction = new EPFValidationAction(file);

    validationAction.setImageDescriptor(imageDescriptor);

    validationAction.setActiveWorkbenchPart(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart());
    selectionProvider.addSelectionChangedListener(validationAction);
    // Set the current selection otherwise at first run, selection is lost.
    ISelection selection = selectionProvider.getSelection();
    if (!selection.isEmpty()) {
      validationAction.selectionChanged(new SelectionChangedEvent(selectionProvider, selection));
    }

    return validationAction;
  }

  /**
   * @return
   */
  public ValidateAction createDefaultValidation() {

    imageDescriptor = CapellaValidationUIActivator.getDefault().getImageDescriptor(CapellaValidationUIActivator.IMG_ENABLED_VALIDATE);

    ISelectionProvider selectionProvider = commonViewSite.getSelectionProvider();
    CapellaValidateAction validationAction = new CapellaValidateAction();

    validationAction.setImageDescriptor(imageDescriptor);

    validationAction.setText("Validate Model"); //$NON-NLS-1$

    validationAction.setActiveWorkbenchPart(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart());
    selectionProvider.addSelectionChangedListener(validationAction);
    // Set the current selection otherwise at first run, selection is lost.
    ISelection selection = selectionProvider.getSelection();
    if (!selection.isEmpty()) {
      validationAction.selectionChanged(new SelectionChangedEvent(selectionProvider, selection));
    }

    return validationAction;

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void fillContextMenu(IMenuManager menu) {
    initActions();
    if (userValidationActions.size() > 0) {
      IMenuManager menuManager = new MenuManager("Validate Model", imageDescriptor, "ID"); //$NON-NLS-1$ //$NON-NLS-2$
      menu.insertBefore("group.validation", menuManager); //$NON-NLS-1$
      menuManager.add(defaultValidationAction);
      menuManager.add(new Separator());
      for (ValidateAction action : userValidationActions) {
        menuManager.add(action);
      }
    } else {
      imageDescriptor = CapellaValidationUIActivator.getDefault().getImageDescriptor(CapellaValidationUIActivator.IMG_ENABLED_VALIDATE);
      userValidationActions = new ArrayList<ValidateAction>();
      menu.prependToGroup("group.validation", defaultValidationAction); //$NON-NLS-1$
    }
  }

  private void initActions() {
    ISelectionProvider selectionProvider = commonViewSite.getSelectionProvider();
    imageDescriptor = CapellaValidationUIActivator.getDefault().getImageDescriptor(CapellaValidationUIActivator.IMG_ENABLED_VALIDATE);
    userValidationActions = new ArrayList<ValidateAction>();
    defaultValidationAction = createDefaultValidation();// ValidationAction(false, null, selectionProvider, imageDescriptor);
    for (IFile file : PreferencesHelper.retrieveUserDefinedPreferenceFiles(selectionProvider, EPFValidationAction.EPF_EXTNAME)) {
      userValidationActions.add(createValidationAction(file, selectionProvider, imageDescriptor));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void fillActionBars(IActionBars actionBars) {
    // Do nothing.
  }

}
