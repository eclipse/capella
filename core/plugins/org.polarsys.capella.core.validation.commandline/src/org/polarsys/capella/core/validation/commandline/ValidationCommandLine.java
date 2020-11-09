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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IExportedPreferences;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.validation.model.Category;
import org.eclipse.emf.validation.preferences.EMFModelValidationPreferences;
import org.eclipse.emf.validation.service.ModelValidationService;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.ui.views.markers.MarkerViewUtil;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.polarsys.capella.common.data.modellingcore.ModelElement;
import org.polarsys.capella.common.helpers.validation.ConstraintStatusDiagnostic;
import org.polarsys.capella.common.tools.report.EmbeddedMessage;
import org.polarsys.capella.common.tools.report.appenders.reportlogview.LightMarkerRegistry;
import org.polarsys.capella.common.tools.report.appenders.reportlogview.MarkerViewHelper;
import org.polarsys.capella.common.tools.report.util.IReportManagerDefaultComponents;
import org.polarsys.capella.core.commandline.core.CommandLineException;
import org.polarsys.capella.core.commandline.core.ui.AbstractWorkbenchCommandLine;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.model.handler.markers.ICapellaValidationConstants;
import org.polarsys.capella.core.model.helpers.query.CapellaQueries;
import org.polarsys.capella.core.sirius.ui.helper.SessionHelper;

public class ValidationCommandLine extends AbstractWorkbenchCommandLine {
  static final String CONSTRAINT_DISABLED_PREFIX = "org.eclipse.emf.validation//con.disabled/"; //$NON-NLS-1$

  public ValidationCommandLine() {
    super();
    argHelper = new ValidationArgumentHelper();
  }

  @Override
  public void printHelp() {
    super.printHelp(Arrays.asList("outputfolder"));
    printArgumentsFromTable("validationParameters", false, Collections.emptyList());
  }

  @Override
  protected IStatus executeWithinWorkbench() {
    // load the AIRDs
    MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, IStatus.OK, Messages.validationStatus, null);
    List<IFile> airdFiles = getAirdFilesFromInput();
    for (IFile file : airdFiles) {
      URI uri = URI.createPlatformResourceURI(file.getFullPath().toString(), true);
      status.add(execute(uri));
    }
    
    return status;
  }

  private IStatus execute(final URI airdURI) {
    IStatus status = Status.OK_STATUS;
    try {

      // Create the validate action.
      CapellaValidateComlineAction capellaValidateCLineAction = new CapellaValidateComlineAction();

      // load
      Project semanticRootElement = loadSemanticRootElement(airdURI);
      if (semanticRootElement == null) {
        return new Status(IStatus.ERROR, FrameworkUtil.getBundle(this.getClass()).getSymbolicName(), "No semantic model found!");
      }
      Resource semanticRootResource = semanticRootElement.eResource();
      capellaValidateCLineAction.setResource(semanticRootResource);

      // set the rule set
      String validationRuleSetFile = ((ValidationArgumentHelper) argHelper).getValidationRuleSet();

      if (!isEmtyOrNull(validationRuleSetFile)) {// validate selected EObjects
        List<String> ruleSet = readRules(validationRuleSetFile);
        ModelValidationService.getInstance().loadXmlConstraintDeclarations();
        for (String ruleId : ruleSet) {
          EMFModelValidationPreferences.setConstraintDisabled(ruleId, true);
        }
      }

      // list of EObject to validate
      String validationContext = ((ValidationArgumentHelper) argHelper).getValidationContext();

      if (!isEmtyOrNull(validationContext)) {// validate selected EObjects
        List<String> objectToValidateUris = toListOfURIString(validationContext);
        List<EObject> loadedEObjs = loadEObjects(semanticRootElement, objectToValidateUris);
        capellaValidateCLineAction.setSelectedObjects(loadedEObjs);
      } else {// validate the whole model
        capellaValidateCLineAction.setSelectedObjects(semanticRootResource.getContents());
      }

      capellaValidateCLineAction
          .setDiagnosticianProviderId(((ValidationArgumentHelper) argHelper).getDiagnosticianProviderId());

      // Run the validation
      capellaValidateCLineAction.run();

      storeResultsToFile(getOrCreateOutputFolderForAird(airdURI));
      
      capellaValidateCLineAction.deleteMarkers();

    } catch (FileNotFoundException exception) {
      status = new Status(IStatus.ERROR, FrameworkUtil.getBundle(this.getClass()).getSymbolicName(), exception.getMessage(), exception);
      
    } catch (CoreException exception) {
      status = new Status(IStatus.ERROR, FrameworkUtil.getBundle(this.getClass()).getSymbolicName(), exception.getMessage(), exception);
    }

    return status;
  }

  /**
   * Returns list of disabled rule IDs
   * 
   * @param validationRuleSetFile
   * @return
   * @throws CoreException
   * @throws FileNotFoundException
   */
  private List<String> readRules(String ruleSetFile) throws FileNotFoundException, CoreException {
    List<String> results = new ArrayList<String>();

    // read epf file
    Path path = new Path(ruleSetFile);
    BufferedInputStream input = new BufferedInputStream(new FileInputStream(path.toFile()));
    IExportedPreferences exportedPrefs = Platform.getPreferencesService().readPreferences(input);
    Preferences instanceNode = exportedPrefs.node("/instance"); //$NON-NLS-1$
    try {
      Preferences validationNode = instanceNode.node("org.eclipse.emf.validation"); //$NON-NLS-1$
      for (String s : validationNode.keys()) {

        boolean isDisabled = validationNode.getBoolean(s, false);

        if (isDisabled) {
          String[] split = s.split("/"); //$NON-NLS-1$
          results.add(split[1]);
        }
      }

    } catch (BackingStoreException exception1) {
      String loggerMessage = "ValidationCommandLine.readRules(..) _ "; //$NON-NLS-1$
      logger.warn(loggerMessage, exception1);
    }

    return results;
  }

  /**
   * @param project
   * @param uris
   * @throws CoreException
   */
  private List<EObject> loadEObjects(Project project, List<String> uris) throws CoreException {
    List<EObject> results = new ArrayList<EObject>();
    for (String uriFragment : uris) {
      String idSegment = getIdSegment(uriFragment);
      ModelElement element = CapellaQueries.getInstance().getGetElementsQueries().getElementById(project, idSegment);
      results.add(element);
    }
    return results;
  }

  /**
   * @param uriFragment
   * @return
   */
  private String getIdSegment(String uriFragment) {
    URI uri = URI.createURI(uriFragment);
    return uri.fragment();
  }

  private List<String> toListOfURIString(String validationContext) {
    List<String> list = new ArrayList<String>();
    for (String s : validationContext.split("\\|")) { //$NON-NLS-1$
      list.add(s.trim());
    }
    return list;
  }

  /**
   * @param uri
   * @return
   */
  private Project loadSemanticRootElement(URI uri) {
    SessionManager sessionManager = SessionManager.INSTANCE;
    Session session = sessionManager.getSession(uri, new NullProgressMonitor());
    return SessionHelper.getCapellaProject(session);
  }

  /**
   * @param outputFolder
   */
  private void storeResultsToFile(IFolder outputFolder) {
    Collection<IMarker> markers = LightMarkerRegistry.getInstance().getMarkers();
    try {
      Collection<IMarker> validationMarkers = filterValidationMarkers(markers);

      String result = null;
      result = toHTML(validationMarkers);
      String fileName = Messages.resultsFileName;
      IFile file = outputFolder.getFile(new Path(fileName));
      ByteArrayInputStream outputContent = new ByteArrayInputStream(result.getBytes());
      if (file.exists()) {
        file.setContents(outputContent, true, false, null);
      } else {
        file.create(outputContent, false, null);
      }

    } catch (CoreException exception) {
      String loggerMessage = "ValidationComandlineApp.storeResultsToFile(..) _ "; //$NON-NLS-1$
      logger.error(new EmbeddedMessage(loggerMessage, IReportManagerDefaultComponents.VALIDATION));
    }

  }

  /**
   * @param markers
   * @return
   * @throws CoreException
   */
  private Collection<IMarker> filterValidationMarkers(Collection<IMarker> markers) throws CoreException {
    Collection<IMarker> result = new ArrayList<IMarker>();
    for (IMarker iMarker : markers) {
      if (ICapellaValidationConstants.CAPELLA_MARKER_ID.equals(iMarker.getType())) {
        result.add(iMarker);
      }
    }
    return result;
  }

  /**
   * @param markers
   * @return
   * @throws CoreException
   */
  private String toHTML(Collection<IMarker> markers) throws CoreException {
    StringBuilder res = new StringBuilder();
    res.append("<html> \n"); //$NON-NLS-1$
    res.append("<head> \n"); //$NON-NLS-1$
    res.append("<title>Problems saved on " + new java.util.Date() + "</title> \n"); //$NON-NLS-1$ //$NON-NLS-2$
    res.append("<head> \n"); //$NON-NLS-1$
    res.append("<body> \n"); //$NON-NLS-1$
    res.append("<table border=\"1\"> \n"); //$NON-NLS-1$
    res.append("<tr> <th> Message</th>\n ");//$NON-NLS-1$
    res.append("<th> Level</th> \n");//$NON-NLS-1$
    res.append("<th> Rule id</th> \n");//$NON-NLS-1$
    res.append("<th> Origin</th> \n");//$NON-NLS-1$
    res.append("<th> Resource</th> \n");//$NON-NLS-1$
    res.append("<th> Time</th> </tr> \n");//$NON-NLS-1$
    final DateFormat format = DateFormat.getTimeInstance();
    for (IMarker iMarker : markers) {
      res.append("<tr>"); //$NON-NLS-1$

      // message
      res.append("<td>"); //$NON-NLS-1$
      res.append(iMarker.getAttribute(IMarker.MESSAGE));
      res.append("</td>"); //$NON-NLS-1$

      // severity level
      res.append("<td>"); //$NON-NLS-1$
      res.append(getSeverityLabel((Integer) iMarker.getAttribute(IMarker.SEVERITY)));
      res.append("</td>"); //$NON-NLS-1$

      // ruleId
      res.append("<td>"); //$NON-NLS-1$

      String ruleId = MarkerViewHelper.getRuleID(iMarker, false);
      if (ruleId == null) {
        ruleId = MarkerViewHelper.getSource(iMarker);
      }

      res.append(ruleId);
      res.append("</td>"); //$NON-NLS-1$

      // origin
      res.append("<td>"); //$NON-NLS-1$
      res.append(getCategory(iMarker));
      res.append("</td>"); //$NON-NLS-1$

      // resource
      res.append("<td>"); //$NON-NLS-1$
      res.append(iMarker.getAttribute(MarkerViewUtil.PATH_ATTRIBUTE));
      res.append("</td>"); //$NON-NLS-1$

      // creation time
      res.append("<td>"); //$NON-NLS-1$
      res.append(format.format(new Date(iMarker.getCreationTime())));
      res.append("</td>"); //$NON-NLS-1$

      res.append("</tr>"); //$NON-NLS-1$
      // System.err.println(iMarker);
    }
    return res.toString();

  }

  /**
   * @param attribute
   * @return
   */
  private String getSeverityLabel(Integer severity) {
    switch (severity.intValue()) {
    case IMarker.SEVERITY_ERROR:
      return "Error"; //$NON-NLS-1$
    case IMarker.SEVERITY_WARNING:
      return "Warning"; //$NON-NLS-1$
    case IMarker.SEVERITY_INFO:
      return "Info"; //$NON-NLS-1$
    }
    return ""; //$NON-NLS-1$
  }

  /**
   * @param iMarker
   * @return
   * @throws CoreException
   */
  private String getCategory(IMarker iMarker) throws CoreException {
    String result = ""; //$NON-NLS-1$
    Diagnostic diagnostic = (Diagnostic) iMarker.getAdapter(Diagnostic.class);
    if (diagnostic instanceof ConstraintStatusDiagnostic) {
      Set<Category> cats = ((ConstraintStatusDiagnostic) diagnostic).getConstraintStatus().getConstraint()
          .getDescriptor().getCategories();
      if (!cats.isEmpty()) {
        result = cats.iterator().next().getQualifiedName();
      }
    }
    return result;

  }
  
  @Override
  public void checkArgs(IApplicationContext context) throws CommandLineException {
    super.checkArgs(context);
    if (argHelper.getOutputFolder() == null) {
      logger.error(org.polarsys.capella.core.commandline.core.Messages.outputfolder_mandatory);
    }
  }
}
