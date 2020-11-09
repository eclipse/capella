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

import org.osgi.framework.BundleContext;
import org.polarsys.capella.common.ui.services.AbstractUIActivator;


/**
 * The activator class controls the plug-in life cycle
 */
public class CapellaValidationUIActivator extends AbstractUIActivator {

  /**
   * Validate enabled image id.<br>
   */
  public final static String IMG_ENABLED_VALIDATE = "capella_validate_16.gif"; //$NON-NLS-1$

  // The shared instance
  private static CapellaValidationUIActivator plugin;


  /**
   * The constructor
   */
  public CapellaValidationUIActivator() {
    // Do nothing.
  }

  /**
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
  }

  /**
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   * @return the shared instance
   */
  public static CapellaValidationUIActivator getDefault() {
    return plugin;
  }

}
