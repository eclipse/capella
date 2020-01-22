/*******************************************************************************
 * Copyright (c) 2006, 2018 THALES GLOBAL SERVICES.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/
package org.polarsys.capella.core.data.migration.handlers;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sirius.ui.tools.internal.actions.repair.RepresentationFilesNeedCloseSessionValidator;
import org.eclipse.ui.handlers.HandlerUtil;
import org.polarsys.capella.core.data.migration.Messages;
import org.polarsys.capella.core.data.migration.MigrationConstants;
import org.polarsys.capella.core.data.migration.MigrationHelpers;

/**
 * 
 */
@SuppressWarnings("restriction")
public class ProjectMigrationHandler extends AbstractMigrationHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    boolean skipConfirmation = Boolean.valueOf(event.getParameter("skipConfirmation"));
    
    try {
      for (Object selected : getSelection((IEvaluationContext) event.getApplicationContext(), IResource.class)) {
        if (selected instanceof IProject) {
          IProject project = (IProject) selected;
          IStatus validationStatus = new RepresentationFilesNeedCloseSessionValidator(
              NLS.bind(Messages.MigrationAction_Title, project.getName())).validate(null);
          if (validationStatus.isOK()) {
            MigrationHelpers.getInstance().trigger(project, HandlerUtil.getActiveShell(event), skipConfirmation, true,
                MigrationConstants.DEFAULT_KIND_ORDER);
          }
        }
      }
      
    } finally {
      MigrationHelpers.getInstance().dispose();
    }
    return event;
  }

  @Override
  public boolean isValidSelection(List<Object> selection) {
    // The selection is valid only if each element is an open project with a melodymodeller file inside.
    for (Object select : selection) {
      if (select instanceof IProject) {
        IProject project = (IProject) select;
        if (!project.isOpen()) {
          // One element is a closed project.
          return false;
        }
          try {
            boolean hasMelodymodellerFile = false;
            for (IResource content : project.members()) {
              if ("melodymodeller".equals(content.getFileExtension())) {
                hasMelodymodellerFile = true;
                break;
              }
            }
            if (!hasMelodymodellerFile) {
              // One element has no melodymodeller file.
              return false;
            }
          } catch (CoreException e) {
            // One element does not exist anymore or is not an open project.
            return false;
          }
      } else {
        // One element is not a project.
        return false;
      }
    }
    return true;
  }
}
