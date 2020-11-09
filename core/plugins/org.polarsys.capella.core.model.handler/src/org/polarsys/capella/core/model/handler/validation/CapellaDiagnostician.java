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
package org.polarsys.capella.core.model.handler.validation;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.provider.IItemLabelProvider;

import org.polarsys.capella.core.data.cs.Part;
import org.polarsys.capella.common.data.modellingcore.AbstractType;

/**
 * In emf validation, a {@link Diagnostician} is a class to navigate in the model during 
 * a validation. The capella Diagnostician navigate recursively in the model, but also 
 * can follow the part->component link. 
 */
public class CapellaDiagnostician extends Diagnostician {
    private AdapterFactory adapterFactory;
    private IProgressMonitor progressMonitor;
    
  public CapellaDiagnostician (AdapterFactory adapterFactory, IProgressMonitor progressMonitor) {
  	this(EValidator.Registry.INSTANCE, adapterFactory, progressMonitor);
  }

  public CapellaDiagnostician (EValidator.Registry registry, AdapterFactory adapterFactory, IProgressMonitor monitor) {
    super(registry);
    this.adapterFactory = adapterFactory;
    this.progressMonitor = monitor;
  }

  @Override
  public String getObjectLabel(EObject eObject) {
    if (adapterFactory != null && !eObject.eIsProxy()) {
      IItemLabelProvider itemLabelProvider = (IItemLabelProvider)adapterFactory.adapt(eObject, IItemLabelProvider.class);
      if (itemLabelProvider != null) {
        return itemLabelProvider.getText(eObject);
      }
    }
    return super.getObjectLabel(eObject);
  }

  @Override
  public boolean validate(EClass eClass, EObject eObject, DiagnosticChain diagnostics, Map<Object, Object> context) {
    progressMonitor.worked(1);
    return super.validate(eClass, eObject, diagnostics, context);
  }

	@Override
	protected boolean doValidateContents(EObject eObject, DiagnosticChain diagnostics, Map<Object, Object> context) {
		boolean result = super.doValidateContents(eObject, diagnostics, context);
		if (eObject instanceof Part) {
			// also launch validation on representedInstance
			AbstractType component = ((Part) eObject).getAbstractType();
      if ((null != component) && !component.eIsProxy() && !EcoreUtil.isAncestor(component, eObject)) {
			  // FIXME doing that might throw duplicated validations
			  // TODO workaround : if component is already included in validation scope, do nothing
			  result &= validate(component, diagnostics, context);
			}
		}		
		return result;
	}
}
