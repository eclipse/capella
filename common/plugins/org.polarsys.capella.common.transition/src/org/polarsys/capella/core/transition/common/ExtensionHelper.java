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

package org.polarsys.capella.core.transition.common;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.polarsys.capella.core.transition.common.constants.ISchemaConstants;
import org.polarsys.capella.core.transition.common.handlers.IHandler;
import org.polarsys.kitalpha.transposer.rules.handler.rules.api.IContext;

/**
 *
 */
public class ExtensionHelper {

  public static Collection<String> collectDomainFromExtensions(IContext context, String expectedPurpose,
      String expectedMapping) {
    return collectStringFromExtensions(context, ISchemaConstants.EXTENSION_ID, ISchemaConstants.DOMAIN, expectedPurpose,
        expectedMapping);
  }

  protected static Collection<String> collectStringFromExtensions(IContext context, String extension_id, String string,
      String expectedPurpose, String expectedMapping) {
    IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(extension_id);
    Collection<String> result = new LinkedList<String>();
    for (IConfigurationElement element : point.getConfigurationElements()) {
      if (isValidExtension(element, expectedPurpose, expectedMapping)) {
        for (IConfigurationElement child : element.getChildren()) {
          if (string.equals(child.getName())) {
            result.add(child.getAttribute(ISchemaConstants.ID));
          }
        }
      }
    }
    return result;
  }

  protected static boolean isValidExtension(IConfigurationElement element, String expectedPurpose,
      String expectedMapping) {
    if (!ISchemaConstants.HANDLERS.equals(element.getName())) {
      return false;
    }

    String purpose = element.getAttribute(ISchemaConstants.HANDLERS__PURPOSE);
    String mapping = element.getAttribute(ISchemaConstants.HANDLERS__MAPPING);

    if (purpose.equals(expectedPurpose) || ISchemaConstants.HANDLERS__PURPOSE__ALL_PURPOSES.equals(purpose)
        || ISchemaConstants.HANDLERS__PURPOSE__ALL_PURPOSES.equals(expectedPurpose)) {
      if (mapping.equals(expectedMapping) || ISchemaConstants.HANDLERS__MAPPING__ALL_MAPPINGS.equals(mapping)
          || ISchemaConstants.HANDLERS__MAPPING__ALL_MAPPINGS.equals(expectedMapping)) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public static <T> Collection<T> collectFromExtensions(IContext context, String extension_id, String childName, String expectedPurpose, String expectedMapping){
    IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(extension_id);
    Collection<T> result = new LinkedList<>();
    for (IConfigurationElement element : point.getConfigurationElements()) {
      if (isValidExtension(element, expectedPurpose, expectedMapping)) {
        for (IConfigurationElement child : element.getChildren()) {
          if (childName.equals(child.getName())) {
            try {
              result.add((T) child.createExecutableExtension(ISchemaConstants.CLASS));
            } catch (CoreException e) {
              Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
            }
          }
        }
      }
    }
    return result;
  }

  public static Collection<IHandler> collectActivityExtendersFromExtensions(IContext context, String extension_id,
      String activityIdentifer, String expectedPurpose, String expectedMapping) {
    IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(extension_id);
    Collection<IHandler> result = new LinkedList<IHandler>();

    for (IConfigurationElement element : point.getConfigurationElements()) {
      if (isValidExtension(element, expectedPurpose, expectedMapping)) {
        for (IConfigurationElement child : element.getChildren()) {

          if (ISchemaConstants.ACTIVITY_EXTENDER.equals(child.getName())) {
            String identifier = child.getAttribute(ISchemaConstants.ACTIVITY_EXTENDER__ACTIVITY_IDENTIFIER);
            if (activityIdentifer.equals(identifier)
                || ISchemaConstants.ACTIVITY_EXTENDER__ACTIVITY_IDENTIFIER__ALL_ACTIVITIES.equals(identifier)) {
              try {
                Object extension = child.createExecutableExtension(ISchemaConstants.CLASS);
                if ((extension != null) && (extension instanceof IHandler)) {
                  result.add((IHandler) extension);
                }
              } catch (CoreException exception) {
                // Catch exception silently, we just can't load an extension
              }
            }
          }
        }
      }
    }
    return result;
  }
}
