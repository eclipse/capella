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
package org.polarsys.capella.core.validation;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.impl.EValidatorRegistryImpl;
import org.eclipse.emf.validation.service.ConstraintRegistry;
import org.osgi.framework.BundleContext;
import org.polarsys.capella.common.mdsofa.common.activator.AbstractActivator;
import org.polarsys.capella.common.re.RePackage;
import org.polarsys.capella.core.model.helpers.registry.CapellaPackageRegistry;
import org.polarsys.capella.core.validation.utils.ValidationHelper;

public class CapellaValidationActivator extends AbstractActivator {
  /**
   * Singleton instance.
   */
  private static CapellaValidationActivator __plugin;

  /*
   * This plugin manages its own validator registry
   */
  private final EValidator.Registry registry = new EValidatorRegistryImpl(EValidator.Registry.INSTANCE);

  /**
   * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    initializeCapellaValidatorRegistry();
    ConstraintRegistry.getInstance().addConstraintListener(CapellaConstraintListener.getInstance());
    __plugin = this;
  }

  /**
   * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context_p) throws Exception {
    __plugin = null;
    super.stop(context_p);
  }

  /**
   * Returns the shared instance
   * @return the shared instance
   */
  public static CapellaValidationActivator getDefault() {
    return __plugin;
  }

  /**
   * Returns the validator registry for capella validation. The registry is configured to
   * funnel standard EMF validation through to EMF Constraint Validation via constraints by using an
   * {@link CapellaValidatorAdapter} as the validator for all Capella packages and as the default
   * for other EPackages.
   */
  public EValidator.Registry getCapellaValidatorRegistry() {
    return registry;
  }

  private void initializeCapellaValidatorRegistry() {
    CapellaValidatorAdapter adapter = new CapellaValidatorAdapter(ValidationHelper.newDefaultCapellaBatchValidator());
    for (EPackage p : CapellaPackageRegistry.getAllCapellaPackages()) {
      registry.put(p, adapter);
    }
    registry.put(RePackage.eINSTANCE, adapter);
    registry.put(null, adapter);
  }
}
