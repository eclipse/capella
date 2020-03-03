/*******************************************************************************
 * Copyright (c) 2020 THALES GLOBAL SERVICES.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/
package org.polarsys.capella.core.ui.search.searchfor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.sirius.diagram.DiagramPackage;
import org.eclipse.sirius.viewpoint.ViewpointPackage;
import org.polarsys.capella.core.ui.search.CapellaSearchConstants;

public class MetaClassesParticipantsItemProvider extends AbstractMetaModelParticipantsItemProvider {
  private boolean showAbstract = false;
  private boolean showSemantics = true;
  private Set<Object> eClassifierList = null;
  private Set<Object> diagramElements = null;
  protected Map<String, Set<Object>> elements;

  public MetaClassesParticipantsItemProvider(AbstractCapellaSearchForContainerArea area) {
    eClassifierList = MetaClassesUtil.getInstance().getClassifiers();
    diagramElements = getDiagramElements();
    elements = new HashMap<String, Set<Object>>();
    elements.put(CapellaSearchConstants.ModelElements_Key, eClassifierList);
    elements.put(CapellaSearchConstants.DiagramElements_Key, diagramElements);
  }

  @Override
  public Object[] getElements(Object inputElement) {
    if (inputElement != null) {
      return elements.keySet().stream().filter(x -> !isFiltered(x)).collect(Collectors.toSet()).toArray();
    }
    return new Object[0];
  }

  @Override
  public boolean hasChildren(Object element) {
    if (element instanceof EAttribute)
      return false;
    return getChildren(element).length > 0;
  }

  @Override
  public Object[] getChildren(Object parentElement) {
    if (parentElement instanceof String && elements.keySet().contains(parentElement)) {
      return elements.get((String) parentElement).stream().filter(x -> !isFiltered(x)).collect(Collectors.toSet()).toArray();
    }
    return super.getChildren(parentElement);
  }

  @Override
  public Object getParent(Object element) {
    if (element instanceof EClass) {
      if (elements.get(CapellaSearchConstants.ModelElements_Key).contains(element)) {
        return CapellaSearchConstants.ModelElements_Key;
      }
      if (elements.get(CapellaSearchConstants.DiagramElements_Key).contains(element)) {
        return CapellaSearchConstants.DiagramElements_Key;
      }
    }
    return null;
  }

  public boolean isShowAbstract() {
    return showAbstract;
  }

  public void setShowAbstract(boolean showAbstract) {
    this.showAbstract = showAbstract;
  }

  public boolean isShowSemantics() {
    return showSemantics;
  }

  public void setShowSemantics(boolean showSemantics) {
    this.showSemantics = showSemantics;
  }

  protected boolean isFiltered(Object cls) {
    if(!showSemantics && !showAbstract)
      return true;
    if(cls instanceof EClass) {
      EClass eCls = (EClass) cls;
      if(showSemantics && !showAbstract && eCls.isAbstract()) {
        return true;
      }
      if(!showSemantics && showAbstract && !eCls.isAbstract()) {
        return true;
      }
    }
    return false;
  }

  public Set<Object> getDiagramElements() {
    Set<Object> result = new HashSet<Object>();
    result.add(DiagramPackage.eINSTANCE.getNote());
    result.add(ViewpointPackage.eINSTANCE.getDRepresentationDescriptor());
    return result;
  }
}
