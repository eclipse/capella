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
package org.polarsys.capella.core.validation.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.edit.ui.action.ValidateAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.ICommonMenuConstants;

import org.polarsys.capella.core.commands.preferences.util.PreferencesHelper;
import org.polarsys.capella.core.platform.sirius.ui.actions.CapellaValidateAction;
import org.polarsys.capella.core.validation.ui.actions.EPFValidationAction;
import org.polarsys.capella.common.menu.dynamic.contributions.ActionContributionProvider;
import org.polarsys.capella.common.ui.menu.dynamic.AbstractActionProvider;

/**
 */
public class DynamicActionProvider extends AbstractActionProvider {

  private ValidateAction defaultValidationAction;
  private List<ValidateAction> userValidationActions;
  private ImageDescriptor imageDescriptor;
  private ISelectionProvider selectionProvider;

  public DynamicActionProvider() {
    // Initialize the action provider to force it to load menu contributors.
    ActionContributionProvider.getInstance();
  }

  /**
   * @see org.polarsys.capella.common.mdsofa.rootasset.ui.workbench.action.navigator.AbstractActionProvider#initActions(org.eclipse.swt.widgets.Shell,
   *      org.eclipse.ui.IWorkbenchPage, org.eclipse.jface.viewers.ISelectionProvider)
   */
  @Override
  protected void initActions(Shell shell, IWorkbenchPage page, ISelectionProvider _selectionProvider) {
    this.selectionProvider = _selectionProvider;

    imageDescriptor = CapellaValidationUIActivator.getDefault().getImageDescriptor(CapellaValidationUIActivator.IMG_ENABLED_VALIDATE);
    userValidationActions = new ArrayList<ValidateAction>();
    defaultValidationAction = createDefaultValidation(); // createValidationAction(false, null, selectionProvider, imageDescriptor);
    for (IFile file : PreferencesHelper.retrieveUserDefinedPreferenceFiles(selectionProvider, EPFValidationAction.EPF_EXTNAME)) {
      userValidationActions.add(createValidationAction(file, selectionProvider, imageDescriptor));
    }
  }

  /**
   * @return
   */
  public ValidateAction createDefaultValidation() {

    imageDescriptor = CapellaValidationUIActivator.getDefault().getImageDescriptor(CapellaValidationUIActivator.IMG_ENABLED_VALIDATE);

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
   * @return
   */
  protected IContributionItem createContributionItem() {

    if (userValidationActions.size() > 0) {
      IMenuManager menuManager = new MenuManager("Validate Model", imageDescriptor, "ID"); //$NON-NLS-1$ //$NON-NLS-2$
      menuManager.add(defaultValidationAction);
      menuManager.add(new Separator());
      for (ValidateAction action : userValidationActions) {
        menuManager.add(action);
      }
      return menuManager;
    }

    imageDescriptor = CapellaValidationUIActivator.getDefault().getImageDescriptor(CapellaValidationUIActivator.IMG_ENABLED_VALIDATE);
    userValidationActions = new ArrayList<ValidateAction>();
    defaultValidationAction = createDefaultValidation(); // createValidationAction(true, null, selectionProvider, imageDescriptor);

    IMenuManager menu = new MenuManager("Validate Model", imageDescriptor, "ID"); //$NON-NLS-1$ //$NON-NLS-2$
    menu.add(new Separator());
    menu.add(defaultValidationAction);
    return menu;

  }

  /**
   * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
   */
  @Override
  public void fillContextMenu(IMenuManager menu) {
    menu.insertAfter(ICommonMenuConstants.GROUP_NEW, createContributionItem());
  }

  /**
   * @see org.polarsys.capella.common.mdsofa.rootasset.ui.workbench.action.navigator.AbstractActionProvider#fillActionBars(org.eclipse.ui.IActionBars)
   */
  @Override
  public void fillActionBars(IActionBars actionBars) {
    // Do nothing.
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
      validationAction.setEnabled(true);
    }

    return validationAction;

  }

  /**
   * Add given contribution item in specified menu.
   * @param menu
   * @param groupId
   * @param item
   */
  protected void addContributionItem(IMenuManager menu, String groupId, IContributionItem item) {
    // Append the action to a group if provided...
    if (null != groupId) {
      menu.appendToGroup(groupId, item);
    } else {
      menu.add(item);
    }
  }

  /**
   * Add given action in specified menu manager.
   * @param menu
   * @param groupId
   * @param action
   */
  protected void addAction(IMenuManager menu, String groupId, IAction action) {
    // Override the action contribution item to force the context menu to be
    // refreshed even if the selected object has not changed.
    addContributionItem(menu, groupId, new DynamicActionContributionItem(action));
  }
}
