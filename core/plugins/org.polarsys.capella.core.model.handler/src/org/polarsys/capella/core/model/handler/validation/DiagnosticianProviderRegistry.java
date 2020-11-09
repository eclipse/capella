/*******************************************************************************
 * Copyright (c) 2020 THALES GLOBAL SERVICES.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.polarsys.capella.core.model.handler.ModelHandlerPlugin;

/**
 * Reads registered {@link AbstractDiagnosticianProvider}s from the extension registry.
 */
public final class DiagnosticianProviderRegistry {

  public static final String DIAGNOSTICIAN_PROVIDER_EXTENSION = ModelHandlerPlugin.PLUGIN_ID + ".diagnosticianProviders"; //$NON-NLS-1$
  public static final DiagnosticianProviderRegistry INSTANCE = new DiagnosticianProviderRegistry();

  public final Collection<Descriptor> providerDescriptors;

  private DiagnosticianProviderRegistry() {
    providerDescriptors = readProviderDescriptors();
  }

  private Collection<Descriptor> readProviderDescriptors() {
    Collection<Descriptor> result = new ArrayList<Descriptor>();
    IConfigurationElement[] elems = Platform.getExtensionRegistry().getConfigurationElementsFor(DIAGNOSTICIAN_PROVIDER_EXTENSION);
    for (IConfigurationElement e : elems) {
        result.add(new Descriptor(e));
    }
    return Collections.unmodifiableCollection(result);
  }

  public Collection<Descriptor> getDescriptors(){
    return providerDescriptors;
  }

  public static AbstractDiagnosticianProvider getDiagnosticianProvider(String providerId) throws CoreException {
    for (DiagnosticianProviderRegistry.Descriptor d : INSTANCE.getDescriptors()) {
      if (providerId.equals(d.getID())){
        return d.getProvider();
      }
    }
    throw new CoreException(new Status(IStatus.ERROR, ModelHandlerPlugin.PLUGIN_ID, "No diagnostician provider with id: " + providerId)); //$NON-NLS-1$
  }

  public static class Descriptor {

    private final IConfigurationElement element;

    Descriptor(IConfigurationElement descriptor) {
      this.element = descriptor;
    }

    public String getID() {
      return element.getAttribute("id"); //$NON-NLS-1$
    }

    public AbstractDiagnosticianProvider getProvider() throws CoreException {
      return (AbstractDiagnosticianProvider) element.createExecutableExtension("class"); //$NON-NLS-1$
    }

    public String getName() {
      return element.getAttribute("name"); //$NON-NLS-1$
    }

    public String getDescription() {
      return element.getAttribute("description"); //$NON-NLS-1$
    }
  }

}
