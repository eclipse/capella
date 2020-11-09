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
package org.polarsys.capella.test.validation.rules.ju.testcases.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.validation.internal.EMFModelValidationPlugin;
import org.eclipse.emf.validation.internal.EMFModelValidationStatusCodes;
import org.eclipse.emf.validation.service.IConstraintDescriptor;
import org.eclipse.emf.validation.service.ModelValidationService;
import org.polarsys.capella.common.mdsofa.common.constant.ICommonConstants;
import org.polarsys.capella.core.validation.ui.ide.internal.quickfix.CapellaQuickFixExtPointUtil;
import org.polarsys.capella.core.validation.ui.ide.quickfix.AbstractCapellaMarkerResolution;
import org.polarsys.capella.core.validation.utils.ValidationHelper;
import org.polarsys.capella.test.framework.api.BasicTestCase;

/**
 * A test that checks for duplicate constraint descriptors 
 * and ambiguous quick fix descriptors.
 */
public class NoDuplicateRuleIdsTest extends BasicTestCase {
  
  /** 
   * Check if constraint with identical ID's are declared.
   * Attaches a log listener and forces constraint loading.
   * EMF Validation logs a warning in case a constraint
   * ID clash is detected. We capture these events and fail
   * the test in such a case.
   * 
   * Additionally, check if all declared constraints have
   * a distinct suffix. This is normally not a problem for
   * EMF Validation, but the
   *   CapellaMarkerResolutionCache
   * uses a fuzzy rule id matching to find marker resolutions
   * for a given rule id, e.g. for two constraints
   * 
   * a.b.c.RULEX
   * d.e.f.RULEX
   * 
   * and one marker resolution with id RULEX, the marker resolution
   * will be provided for both kinds of constraint violations, even
   * if the resolution only knows how to resolve the first constraint
   * violation.
   */
  public void test() throws Exception {
    final List<IStatus> logged = new ArrayList<IStatus>();
    
    EMFModelValidationPlugin.getPlugin().getLog().addLogListener(new ILogListener(){
     public void logging(IStatus status_p, String plugin_p) {
        if (status_p.getCode() == EMFModelValidationStatusCodes.PROVIDER_DUPLICATE_CONSTRAINT)
        logged.add(status_p);
      }
    });

    // force loading of constraint descriptors.
    ModelValidationService.getInstance().loadXmlConstraintDeclarations();

    // EMF validation logs an error if it finds descriptors with the same ID.
    if (!logged.isEmpty()){
      StringBuilder builder = new StringBuilder();
      for (IStatus s : logged){
        builder.append(s + System.getProperty("line.separator")); //$NON-NLS-1$
      }
      fail("Recorded unexpected log message(s) while loading constraint descriptors: " //$NON-NLS-1$
           + System.getProperty("line.separator") //$NON-NLS-1$
           + builder);
    }

    // for quickfixes to work correctly, we need an even tighter check. find constraint descriptors
    // that share a common suffix:
    Map<String, List<IConstraintDescriptor>> suffixDescriptors = new HashMap<String, List<IConstraintDescriptor>>();
    Set<String> dups = new HashSet<String>();
    for (IConstraintDescriptor descriptor : ValidationHelper.getAllConstraintDescriptors()) {
      String suffix = descriptor.getId();
      int lastDot = suffix.lastIndexOf('.');
      if (lastDot >= 0 && lastDot < suffix.length() - 1) {
        suffix = suffix.substring(lastDot + 1);
      }
      List<IConstraintDescriptor> descriptorsForSuffix = suffixDescriptors.get(suffix);
      if (descriptorsForSuffix == null) {
        descriptorsForSuffix = new ArrayList<IConstraintDescriptor>();
        suffixDescriptors.put(suffix, descriptorsForSuffix);
        descriptorsForSuffix.add(descriptor);
      } else {
        descriptorsForSuffix.add(descriptor);
        dups.add(suffix);
      }
    }

    
    if (!dups.isEmpty()) {
      // ... and if we found one or more constraint descriptors that share a common suffix,
      // make sure to declare all quick fix (marker resolutions) that refer to this
      // suffix unqualifiedly as invalid.
      StringBuilder message = new StringBuilder();
      Map<AbstractCapellaMarkerResolution, Set<String>> resolutions = CapellaQuickFixExtPointUtil.gettAllAvailableMarkerResolution();
      for (Map.Entry<AbstractCapellaMarkerResolution, Set<String>> entry : resolutions.entrySet()) {
        for (String resolutionId : entry.getValue()) {
          if (dups.contains(resolutionId)) {
            AbstractCapellaMarkerResolution res = entry.getKey();
            message.append("Bundle " + res.getContributorId() //$NON-NLS-1$
                           + " provides a marker resolution for ruleId '" //$NON-NLS-1$
                           + resolutionId + "' which is ambiguous. " + ICommonConstants.LINE_SEPARATOR //$NON-NLS-1$
                           + "You can fix this problem by specifying" //$NON-NLS-1$
                           + " one of these qualified constraint IDs in the extension's " //$NON-NLS-1$
                           + "'ruleId' attribute:" //$NON-NLS-1$
                           + ICommonConstants.LINE_SEPARATOR);
            for (IConstraintDescriptor descriptor : suffixDescriptors.get(resolutionId)) {
              message.append(descriptor.getId());
              message.append(ICommonConstants.LINE_SEPARATOR);
            }
          }
        }
        if (message.length() > 0) {
          fail(message.toString());
        }
      }
    }
  }

  public void testNoDuplicateRuleIdsTest() throws Exception {
    test();
  }
}
