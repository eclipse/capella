/*******************************************************************************
 * Copyright (c) 2019, 2020 THALES GLOBAL SERVICES.
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
package org.polarsys.capella.common.ui.toolkit.widgets.filter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.dialogs.SearchPattern;

public class CapellaPatternFilter extends PatternFilter {
  private String pattern;

  SearchPattern matcher;

  private boolean caseSensitiveEnabled = false;

  protected String getText(Viewer viewer, Object element) {
    return ((ILabelProvider) ((StructuredViewer) viewer).getLabelProvider()).getText(element);
  }

  @Override
  protected boolean isLeafMatch(Viewer viewer, Object element) {
    String labelText = getText(viewer, element);
    if (labelText == null) {
      return false;
    }
    return wordMatches(labelText);
  }

  @Override
  public void setPattern(String patternString) {
    super.setPattern(patternString);
    if (patternString != null) {
      this.pattern = patternString;

      // As the matcher of PatternFilter use by default "true" for "ignoreCase" and "false" for "ignoreWildCard"
      // We create a another matcher here so that we can provide search options for user.
      if (!patternString.endsWith(" ")) {
        // So that if search for "Air", the results will include texts containing "Aircraft" or "Airplane"
        patternString += "*";
      }
      if(caseSensitiveEnabled)
        matcher = new SearchPattern(SearchPattern.RULE_PATTERN_MATCH & SearchPattern.RULE_CASE_SENSITIVE);
      else
        matcher = new SearchPattern(SearchPattern.RULE_PATTERN_MATCH);
      
      matcher.setPattern(patternString);
    }
  }

  public String getPattern() {
    return pattern;
  }

  @Override
  protected boolean wordMatches(String text) {
    if (pattern == null || pattern.isEmpty()) {
      return true;
    }

    if (text == null) {
      return false;
    }

    return matcher.matches(text);
  }

  public void setCaseSensitiveEnabled(boolean caseSensitiveEnabled) {
    this.caseSensitiveEnabled = caseSensitiveEnabled;
  }

  /**
   * By default, the cache is used only for {@link FilteredTree.NotifyingTreeViewer} and the
   * {@link PatternFilter#setUseCache()} is not public accessed. Use java reflection to be able to activate the caches
   * to improve performance while searching in big Capella model
   * 
   * If activating the cache for a viewer, DO NOT forget to clear cache before refreshing a viewer. Otherwise, a
   * potential {@link ArrayIndexOutOfBoundsException} is thrown at {@link StructuredViewer#notifyFilteredOut()}
   * 
   * @see https://jira.appcelerator.org/browse/APSTUD-1074
   * @see PatternFilter#setUseCache()
   */
  public void doSetUseCache(boolean useCache) {
    try {
      // As we are not using the default NotifyingTreeViewer of FilteredTree, the cache is not activated.
      // Use java reflection to activate it here
      Method setUseCacheMethod = PatternFilter.class.getDeclaredMethod("setUseCache", boolean.class); //$NON-NLS-1$
      setUseCacheMethod.setAccessible(true);
      setUseCacheMethod.invoke(this, useCache);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
  }

  /**
   * Use java reflection to be able to clear the caches.
   * 
   * @see PatternFilter#clearCaches()
   */
  public void doClearCaches() {
    try {
      Method clearCachesMethod = PatternFilter.class.getDeclaredMethod("clearCaches");
      clearCachesMethod.setAccessible(true);
      clearCachesMethod.invoke(this);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }
}
