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
package org.polarsys.capella.core.validation.commandline;

import org.polarsys.capella.core.commandline.core.CommandLineArgumentHelper;

/**
 */
public class ValidationArgumentHelper extends CommandLineArgumentHelper {
  private String validationContext;
  private String validationRuleSet;
  private String providerId;

  /**
   * {@inheritDoc}
   */
  @Override
  public void parseArgs(String[] args) {
    super.parseArgs(args);

    // parse validation specific args
    for (int i = 0; i < args.length; i++) {
      String arg = args[i].toLowerCase();

      if (ValidationCommandLineConstants.VALIDATION_CONTEXT.equalsIgnoreCase(arg)) {
        validationContext = args[++i];
      } else if (ValidationCommandLineConstants.VALIDATION_RULE_SET.equalsIgnoreCase(arg)) {
        validationRuleSet = args[++i];
      } else if (ValidationCommandLineConstants.DIAGNOSTICIAN_PROVIDER_ID.equalsIgnoreCase(arg)) {
        providerId = args[++i];
      }
    }
  }

  /**
   * @return the validationContext
   */
  public String getValidationContext() {
    return validationContext;
  }

  /**
   * @return the validationRuleSet
   */
  public String getValidationRuleSet() {
    return validationRuleSet;
  }
  
  /**
   * @return The diagnostician provider ID
   */
  public String getDiagnosticianProviderId() {
    return providerId;
  }

}
