/*******************************************************************************
 * Copyright (c) 2021 THALES GLOBAL SERVICES.
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
package org.polarsys.capella.core.platform.sirius.ui.session;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.egit.core.RepositoryCache;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sirius.business.api.resource.ResourceDescriptor;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.resource.AirdResource;
import org.eclipse.sirius.viewpoint.DAnalysis;
import org.polarsys.capella.common.helpers.EcoreUtil2;
import org.polarsys.capella.core.model.handler.command.CapellaResourceHelper;
import org.polarsys.capella.core.platform.sirius.ui.actions.CapellaActionsActivator;

public class GitConflictHelper {

  private GitConflictHelper() {
    // Private constructor
  }

  /**
   * @param project
   * @return all aird and capella files in conflict in a project
   */
  public static Set<IFile> getFilesInConflict(IProject project) {
    return getFiles(project, GitConflictHelper::hasConflict);
  }

  /**
   * @param project
   * @param predicate
   * @return all aird and capella files matching the given predicate in the given project
   */
  public static Set<IFile> getFiles(IProject project, Predicate<IFile> predicate) {
    Set<IFile> conflictFiles = new HashSet<>();
    try {
      project.accept(new IResourceVisitor() {
        @Override
        public boolean visit(IResource resource) throws CoreException {
          if (resource instanceof IFile
              && (CapellaResourceHelper.isAirdResource(resource, false)
                  || CapellaResourceHelper.isCapellaResource(resource) || CapellaResourceHelper.isAfmResource(resource))
              && predicate.test((IFile) resource)) {
            conflictFiles.add((IFile) resource);
          }
          return true;
        }
      });
    } catch (CoreException e) {
      // Do nothing
    }
    return conflictFiles;
  }

  /**
   * @param resource
   * @return the corresponding IFile of the given resource and, if the resource is an {@link AirdResource}, return also
   *         its semantic resources
   */
  private static Set<IFile> getRelatedFiles(Resource resource) {
    Set<IFile> relatedFiles = new HashSet<>();

    IFile file = EcoreUtil2.getFile(resource);
    if (file != null) {
      relatedFiles.add(file);
    }

    if (resource instanceof AirdResource) {
      EObject rootEObj = resource.getContents().get(0);
      if (rootEObj instanceof DAnalysis) {
        ((DAnalysis) rootEObj).getSemanticResources().stream().map(GitConflictHelper::getFile).filter(Objects::nonNull)
            .forEach(x -> relatedFiles.add(x));
      }
    }

    return relatedFiles;
  }

  /**
   * @param resource
   * @return the corresponding IFile or null if not a platform resource one
   */
  private static IFile getFile(ResourceDescriptor descriptor) {
    if (descriptor != null) {
      URI resourceURI = descriptor.getResourceURI();
      if (resourceURI != null && resourceURI.isPlatformResource()) {
        IFile semanticFile = ResourcesPlugin.getWorkspace().getRoot()
            .getFile(new Path(resourceURI.toPlatformString(true)));
        return semanticFile;
      }
    }
    return null;
  }

  /**
   * 
   * @param res
   * @return check if the resource itself is in a Git repository, if the resource is an {@link AirdResource}, check also
   *         its semantic resources
   */
  public static boolean isInGitRepository(Resource res) {
    IFile file = EcoreUtil2.getFile(res);
    if (isInGitRepository(file)) {
      return true;
    }
    if (res instanceof AirdResource) {
      EObject rootEObj = res.getContents().get(0);
      if (rootEObj instanceof DAnalysis) {
        for (ResourceDescriptor resDesc : ((DAnalysis) rootEObj).getSemanticResources()) {
          IFile resfile = getFile(resDesc);
          if (isInGitRepository(resfile)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * 
   * @param file
   * @return whether the file has unresolved conflicts
   */
  @SuppressWarnings("restriction")
  public static boolean hasConflict(IFile file) {
    if (file != null) {
      java.nio.file.Path filePath = Paths.get(file.getLocationURI());
      for (Repository repo : RepositoryCache.getInstance().getAllRepositories()) {
        java.nio.file.Path repoPath = Paths.get(repo.getWorkTree().toURI());
        if (filePath.startsWith(repoPath)) {
          java.nio.file.Path relativizedPath = repoPath.relativize(filePath);
          try {
            // org.eclipse.emf.diffmerge.connector.git.ext.GitHelper.isConflicting(Repository, String) with NPE checks
            DirCache readDirCache = repo.readDirCache();
            if (readDirCache != null) {
              DirCacheEntry entry = readDirCache.getEntry(relativizedPath.toString().replace("\\", "/"));
              if (entry != null) {
                return entry.getStage() > 0;
              }
            }
          } catch (NoWorkTreeException | IOException e) {
            // Do nothing
          }
        }
      }
    }
    return false;
  }

  /**
   * 
   * @param file
   * @return whether the file is in a Git repository
   */
  @SuppressWarnings("restriction")
  public static boolean isInGitRepository(IFile file) {
    if (file != null) {
      java.nio.file.Path filePath = Paths.get(file.getLocationURI());
      for (Repository repo : RepositoryCache.getInstance().getAllRepositories()) {
        java.nio.file.Path repoPath = Paths.get(repo.getWorkTree().toURI());
        if (filePath.startsWith(repoPath)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * @param session
   * @return whether all resources from the given session are not in conflict state
   */
  public static IStatus checkConflictFiles(Session session) {
    Set<IFile> inConflictFiles = getFilesInConflict(session);
    if (!inConflictFiles.isEmpty()) {
      return new Status(IStatus.ERROR, CapellaActionsActivator.getDefault().getPluginId(),
          NLS.bind(Messages.GitConflictHelper_ResourcesInConflictState,
              inConflictFiles.stream().map(IFile::getName).collect(Collectors.joining(", "))));
    }
    return Status.OK_STATUS;
  }

  /**
   * @param session
   * @return whether a resource from the given session is in a Git repository
   */
  public static boolean isInGitRepository(Session session) {
    return session.getAllSessionResources().stream().anyMatch(GitConflictHelper::isInGitRepository);
  }

  /**
   * @return from the given session all the files that match the given predicate. This will lookup on all semantic
   *         resources and their related projects for matching files
   */
  public static Set<IFile> getFiles(Session session, Predicate<IFile> file) {
    Set<IFile> sessionRelatedFiles = session.getAllSessionResources().stream().flatMap(x -> getRelatedFiles(x).stream())
        .collect(Collectors.toSet());

    Stream<IProject> relatedProjects = sessionRelatedFiles.stream().map(IResource::getProject).distinct();
    Stream<IFile> conflictsFromRelatedProjects = relatedProjects.flatMap(x -> getFiles(x, c -> true).stream());

    return Stream.concat(sessionRelatedFiles.stream(), conflictsFromRelatedProjects).filter(file)
        .collect(Collectors.toSet());
  }

  /**
   * @return from the given session all the files that are in conflict state. This will lookup on all semantic resources
   *         and their related projects for conflict files
   */
  public static Set<IFile> getFilesInConflict(Session session) {
    return getFiles(session, GitConflictHelper::hasConflict);
  }

  /**
   * @param project
   * @return whether all files in the project are not in conflict state
   */
  public static IStatus checkConflictFiles(IProject project) {
    Set<IFile> inConflictFiles = getFilesInConflict(project);
    if (!inConflictFiles.isEmpty()) {
      return new Status(IStatus.ERROR, CapellaActionsActivator.getDefault().getPluginId(),
          NLS.bind(Messages.GitConflictHelper_ResourcesInConflictState,
              inConflictFiles.stream().map(IFile::getName).collect(Collectors.joining(", "))));
    }
    return Status.OK_STATUS;
  }

}
