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
package org.polarsys.capella.core.data.fa.ui.wizards.dialogs;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.validation.service.IConstraintDescriptor;
import org.eclipse.emf.validation.service.IConstraintFilter;
import org.eclipse.emf.validation.service.IValidator;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.polarsys.capella.common.ui.providers.MDEAdapterFactoryLabelProvider;
import org.polarsys.capella.core.validation.utils.ValidationHelper;

public class EIAllocationLabelProvider extends MDEAdapterFactoryLabelProvider implements IBaseLabelProvider, IColorProvider, IFontProvider {
  protected final boolean isSourceViewer;
  private final EIAllocationTreeViewer treeViewer;
  private LinkManager linkManager;
  private Font startedLinkElementFont;
  private final IValidator<EObject> validator;
  public final static String VALIDATION_KEY = "Validation"; //$NON-NLS-1$
  
  @SuppressWarnings("nls")
  private static List<String> _srcDesc = Arrays.asList(
      "org.polarsys.capella.core.data.fa.validation.TC_DF_10",
      "org.polarsys.capella.core.data.fa.validation.TC_DF_11",
      "org.polarsys.capella.core.data.fa.validation.TC_DF_12",
      "org.polarsys.capella.core.data.fa.validation.TC_DF_13",
      "org.polarsys.capella.core.data.fa.validation.TC_DF_14");

  @SuppressWarnings("nls")
  private static List<String> _tgtDesc = Arrays.asList(
      "org.polarsys.capella.core.data.fa.validation.TC_DF_11",
      "org.polarsys.capella.core.data.fa.validation.TC_DF_12",
      "org.polarsys.capella.core.data.fa.validation.TC_DF_13",
      "org.polarsys.capella.core.data.fa.validation.TC_DF_14");

  /**
   * @param linkManager
   * @param treeViewer
   * @param isSourceViewer
   */
  public EIAllocationLabelProvider(LinkManager linkManager, EIAllocationTreeViewer treeViewer, boolean isSourceViewer) {
    this.isSourceViewer = isSourceViewer;
    this.treeViewer = treeViewer;
    this.linkManager = linkManager;
    validator = ValidationHelper.newDefaultCapellaBatchValidator();
    validator.addConstraintFilter(new IConstraintFilter() {
      @Override
      public boolean accept(IConstraintDescriptor constraint, EObject target) {
        return (isSourceViewer ? _srcDesc.contains(constraint.getId()) : _tgtDesc.contains(constraint.getId()));
      }
    });
  }

  /**
   * @see org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider#dispose()
   */
  @Override
  public void dispose() {
    // Dispose created font.
    if ((null != startedLinkElementFont) && !startedLinkElementFont.isDisposed()) {
      startedLinkElementFont.dispose();
      startedLinkElementFont = null;
    }
    super.dispose();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getText(Object element) {
    String text = super.getText(element);
    if (linkManager.isStartedElement(element)) {
      text += " [start link]"; //$NON-NLS-1$
    }
    return text;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Font getFont(Object element) {
    if (linkManager.isStartedElement(element)) {
      return getStartedLinkElementFont();
    }
    return super.getFont(element);
  }

  private boolean isValid(EObject element) {
    IStatus status = validator.validate(element);
    Widget w = treeViewer.findItem(element);
    if (w instanceof TreeItem) {
      if (!status.isOK()) {
        w.setData(VALIDATION_KEY, status);
      } else {
        w.setData(VALIDATION_KEY, null);
      }
    }
    return status.isOK();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Color getForeground(Object element) {
    if (!isValid((EObject) element)) {
      return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
    }
    return super.getForeground(element);
  }
  

  /**
   * Create started link element font.
   */
  protected Font getStartedLinkElementFont() {
    if (null == startedLinkElementFont) {
      startedLinkElementFont = new Font(Display.getDefault(), "Arial", 9, SWT.NORMAL | SWT.ITALIC | SWT.BOLD); //$NON-NLS-1$
    }
    return startedLinkElementFont;
  }
}
