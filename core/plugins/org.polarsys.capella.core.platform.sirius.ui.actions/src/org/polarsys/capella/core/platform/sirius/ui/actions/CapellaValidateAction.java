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

package org.polarsys.capella.core.platform.sirius.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EObjectValidator;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.ui.EMFEditUIPlugin;
import org.eclipse.emf.edit.ui.action.ValidateAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;
import org.polarsys.capella.common.ef.ExecutionManager;
import org.polarsys.capella.common.ef.command.AbstractReadOnlyCommand;
import org.polarsys.capella.common.helpers.TransactionHelper;
import org.polarsys.capella.common.mdsofa.common.constant.ICommonConstants;
import org.polarsys.capella.common.tools.report.appenders.reportlogview.LightMarkerRegistry;
import org.polarsys.capella.common.tools.report.appenders.reportlogview.MarkerView;
import org.polarsys.capella.common.tools.report.appenders.usage.UsageMonitoringLogger;
import org.polarsys.capella.common.tools.report.appenders.usage.util.UsageMonitoring.EventStatus;
import org.polarsys.capella.core.commands.preferences.service.AbstractPreferencesInitializer;
import org.polarsys.capella.core.model.handler.markers.ICapellaValidationConstants;
import org.polarsys.capella.core.model.handler.validation.AbstractDiagnosticianProvider;
import org.polarsys.capella.core.model.handler.validation.DiagnosticianProviderRegistry;
import org.polarsys.capella.core.model.handler.validation.MultiobjectDiagnostician;
import org.polarsys.capella.core.platform.sirius.ui.preferences.ICapellaValidationPreferences;

/**
 * The EMF edit validate action, with correct selection of the resource, while handling the resulting diagnostic.<br>
 * Also handles the execution within the execution manager environment.
 */
public class CapellaValidateAction extends ValidateAction {
  /**
   * Resource hosting the elements of the current diagnostic (see {@link #handleDiagnostic(Diagnostic)}).
   */
  protected Resource _currentResource;

  /**
   * Constructor.
   */
  public CapellaValidateAction() {
    super();
    eclipseResourcesUtil = new ResourcesUtil();
  }

  @Override
  protected Diagnostician createDiagnostician(AdapterFactory adapterFactory, IProgressMonitor progressMonitor) {
    String providerID = getDiagnosticianProviderId();
    if (providerID != null) {
      try {
        AbstractDiagnosticianProvider provider = DiagnosticianProviderRegistry.getDiagnosticianProvider(providerID);
        return provider.getDiagnostician(adapterFactory, progressMonitor);
      } catch (CoreException e) {
        StatusManager.getManager().handle(e, CapellaActionsActivator.PLUGIN_ID);
      }
      return null;
    }
    return new Diagnostician();
  }

  /**
   * Get the id for the DiagnosticianProvider to use. This defaults to whatever is 
   * set in the Capella preferences.
   * 
   * Subclasses may override to use a concrete diagnostician provider.
   */
  protected String getDiagnosticianProviderId() {
    return Platform.getPreferencesService().getString(org.polarsys.capella.core.preferences.Activator.PLUGIN_ID, ICapellaValidationPreferences.P_DIAGNOSTICIAN_PROVIDER, null, null);
  }

  /**
   * @see org.eclipse.emf.edit.ui.action.ValidateAction#run()
   */
  @Override
  public void run() {
    ExecutionManager executionManager = TransactionHelper.getExecutionManager(selectedObjects);
    // Precondition.
    // Need for an execution manager.
    if (null == executionManager) {
      return;
    }
    // Set editing domain, if required.
    if (null == domain) {
      domain = executionManager.getEditingDomain();
    }

    // Execution action as a read only command.
    executionManager.execute(new AbstractReadOnlyCommand() {
      @SuppressWarnings("synthetic-access")
      public void run() {
        executeValidationAction();
      }
    });

    try {
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(MarkerView.VIEW_ID);
    } catch (PartInitException e) {
      CapellaActionsActivator.getDefault().log(IStatus.ERROR, e.getMessage(), e);
    }

  }

  public void executeValidationAction() {
    final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
      @Override
      public void run(final IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
        try {
          final Diagnostic diagnostic = validate(progressMonitor);
          shell.getDisplay().asyncExec(new Runnable() {
            public void run() {
              if (progressMonitor.isCanceled()) {
                handleDiagnostic(Diagnostic.CANCEL_INSTANCE);
              } else {
                handleDiagnostic(diagnostic);
              }
            }
          });
        } finally {
          progressMonitor.done();
        }
      }
    };

    if (eclipseResourcesUtil != null) {
      runnableWithProgress = eclipseResourcesUtil.getWorkspaceModifyOperation(runnableWithProgress);
    }

    String eventName = "Validation"; //$NON-NLS-1$
    String eventContext = ICommonConstants.EMPTY_STRING;
    String addendum = ICommonConstants.EMPTY_STRING;

    try {
    
      UsageMonitoringLogger.getInstance().log(eventName, eventContext, EventStatus.NONE, addendum);

      // forks is set to false to make the runnable run in the UI thread. If set to true it will lead
      // to a deadlock
      // see Eclipse Bug 105491 : https://bugs.eclipse.org/bugs/show_bug.cgi?id=105491
      new ProgressMonitorDialog(shell).run(false, true, runnableWithProgress);

      UsageMonitoringLogger.getInstance().log(eventName, eventContext, EventStatus.OK, addendum);
    } catch (Exception exception) {
      EMFEditUIPlugin.INSTANCE.log(exception);
      UsageMonitoringLogger.getInstance().log(eventName, eventContext, EventStatus.ERROR, addendum);
    }
  }

  /**
   * @see org.eclipse.emf.edit.ui.action.ValidateAction#handleDiagnostic(org.eclipse.emf.common.util.Diagnostic)
   */
  @Override
  protected void handleDiagnostic(Diagnostic diagnostic) {
    // This is all about tweaking the default behavior (that picks the first opened resource).
    try {
      // Get diagnostic data.
      List<?> data = diagnostic.getData();
      // Check content availability.
      if ((null != data) && (data.size() > 0)) {
        // Search for a resource holder.
        for (Object object : data) {
          // That has to be an EObject.
          if (object instanceof EObject) {
            // Retain current resource...
            _currentResource = ((EObject) object).eResource();
            // ... and stop the search.
            break;
          }
        }
      }
      // Go for default behavior.
      // Markers will be tagged with current resource at creation time (see constructor).
      super.handleDiagnostic(diagnostic);
    } finally {
      // Reset current resource, whatever its value may be.
      _currentResource = null;
    }
  }

  // Slightly modified from superclass to support MultiobjectDiagnosticians
  // and error handling if invalid diagnosticians are specified
  protected Diagnostic validate(IProgressMonitor progressMonitor) {
    int selectionSize = selectedObjects.size();

    progressMonitor.beginTask("", IProgressMonitor.UNKNOWN); //$NON-NLS-1$

    AdapterFactory adapterFactory =
      domain instanceof AdapterFactoryEditingDomain ? ((AdapterFactoryEditingDomain)domain).getAdapterFactory() : null;
    Diagnostician diagnostician = createDiagnostician(adapterFactory, progressMonitor);
    if (diagnostician == null) {
      return new BasicDiagnostic(Diagnostic.ERROR, CapellaActionsActivator.PLUGIN_ID, 0, 
          "Could not create diagnostician", null); //$NON-NLS-1$
    }

    BasicDiagnostic diagnostic;
    if (selectionSize == 1){
      diagnostic = diagnostician.createDefaultDiagnostic(selectedObjects.get(0));
    } else {
      diagnostic =
          new BasicDiagnostic
          (EObjectValidator.DIAGNOSTIC_SOURCE,
              0,
              EMFEditUIPlugin.INSTANCE.getString("_UI_DiagnosisOfNObjects_message", new String[] { Integer.toString(selectionSize) }), //$NON-NLS-1$
              selectedObjects.toArray());
    }

    Map<Object, Object> context = diagnostician.createDefaultContext();
    context.putAll(getContextEntries());

    if (diagnostician instanceof MultiobjectDiagnostician) {
      ((MultiobjectDiagnostician) diagnostician).validate(selectedObjects, diagnostic, context);
    } else {
      for (EObject eObject : selectedObjects) {
        progressMonitor.setTaskName(EMFEditUIPlugin.INSTANCE.getString("_UI_Validating_message", new Object [] { diagnostician.getObjectLabel(eObject) })); //$NON-NLS-1$
        diagnostician.validate(eObject, diagnostic, context);
        context.remove(EObjectValidator.ROOT_OBJECT);
      }  
    }
    return diagnostic;
  }

  protected Map<Object, Object> getContextEntries(){
    return Collections.emptyMap();
  }


  /**
   * Subclasses may further customize created validation markers
   * @param marker
   */
  protected void modifyMarker(IMarker marker) {
    //   EPF uses this to add the epf file as an additional attribute
  }

  protected class ResourcesUtil extends EclipseResourcesUtil {
    /**
     * {@inheritDoc}
     */
    @Override
    protected String getMarkerID() {
      return ICapellaValidationConstants.CAPELLA_MARKER_ID;
    }

    /**
     * @see org.eclipse.emf.edit.ui.action.ValidateAction.EclipseResourcesUtil#createMarkers(org.eclipse.emf.ecore.resource.Resource,
     *      org.eclipse.emf.common.util.Diagnostic)
     */
    @Override
    public void createMarkers(Resource resource, Diagnostic diagnostic) {
      // Don't use 'traditional' resource markers. TODO investigate to go back to the traditional ones.
      // Original reasons to switch: CDO and too many workspace notifications (especially in transitions)

      // can't use resource, see handleDiagnostics below
      LightMarkerRegistry.getInstance().createMarker(getFile(_currentResource), diagnostic, getMarkerID(), m -> modifyMarker(m));
    }

    /**
     * @see org.eclipse.emf.edit.ui.util.EditUIMarkerHelper#deleteMarkers(java.lang.Object, boolean, int)
     */
    @Override
    public void deleteMarkers(Object object, boolean includeSubtypes, int depth) {
      boolean cleanup = AbstractPreferencesInitializer.getBoolean(ICapellaValidationPreferences.P_CLEAN_PREVIOUS_VALIDATION_RESULTS, false);
      if (cleanup) {
        List<IMarker> markers = new ArrayList<IMarker>(LightMarkerRegistry.getInstance().getMarkers());
        for (IMarker marker : markers) {
          try {
            if (marker.getType().equals(getMarkerID())) {
              marker.delete();
            }
          } catch (CoreException e) {
            CapellaActionsActivator.getDefault().log(IStatus.ERROR, e.getMessage(), e);
          }
        }
      }
    }

    /**
     * Overridden to redirect markers to target to the .aird file so that
     * relevant markers can be removed when the corresponding session is closed
     * (see {@link org.polarsys.capella.core.ui.reportlog.InformationViewSessionListener})
     */
    @Override
    protected IFile getFile(Object datum) {
      Object derivedDatum = datum;
      if (datum instanceof EObject) {
        Session session = SessionManager.INSTANCE.getSession((EObject) datum);
        if (session != null) {
          derivedDatum = session.getSessionResource();
        }
      } else if (datum instanceof Resource) {
        Session session = SessionManager.INSTANCE.getSession((Resource) datum);
        if (session != null) {
          derivedDatum = session.getSessionResource();
        }
      }

      return super.getFile(derivedDatum);
    }
  }

}
