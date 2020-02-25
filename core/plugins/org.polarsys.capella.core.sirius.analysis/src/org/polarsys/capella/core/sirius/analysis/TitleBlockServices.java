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
package org.polarsys.capella.core.sirius.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.common.tools.api.interpreter.EvaluationException;
import org.eclipse.sirius.common.tools.internal.interpreter.FeatureInterpreter;
import org.eclipse.sirius.diagram.AbstractDNode;
import org.eclipse.sirius.diagram.DDiagram;
import org.eclipse.sirius.diagram.DDiagramElement;
import org.eclipse.sirius.diagram.DEdge;
import org.eclipse.sirius.diagram.DNodeContainer;
import org.eclipse.sirius.diagram.DNodeList;
import org.eclipse.sirius.diagram.DSemanticDiagram;
import org.eclipse.sirius.diagram.DragAndDropTarget;
import org.eclipse.sirius.diagram.description.NodeMapping;
import org.eclipse.sirius.viewpoint.DRepresentation;
import org.eclipse.sirius.viewpoint.description.DAnnotation;
import org.eclipse.sirius.viewpoint.description.DescriptionFactory;
import org.polarsys.capella.core.data.information.impl.ClassImpl;
import org.polarsys.capella.core.data.information.impl.DataPkgImpl;

public class TitleBlockServices {
  private static TitleBlockServices service = null;

  static Map<String, String> propertiesName = new HashMap<String, String>();
  static Map<String, String> propertiesContent = new HashMap<String, String>();

  private static final String TITLE_BLOCK = "TitleBlock";
  private static final String NAME = "Name:";
  private static final String CONTENT = "Content:";
  private static final String VISIBILITY = "Visibility";
  private static final String IS_ELEMENT_TITLE_BLOCK = "Is Element Title Block";
  private static final String TRUE = "True";
  private static final String FALSE = "False";

  public static TitleBlockServices getService() {
    if (service == null) {
      init();
      service = new TitleBlockServices();
    }
    return service;
  }

  public static void init() {
    propertiesName.put("TB_0_0", "Name");
    propertiesName.put("TB_0_1", "Documentation");
    propertiesName.put("TB_1_0", "Contextual elements");
    propertiesContent.put("TB_0_0", "feature:name");
    propertiesContent.put("TB_0_1", "");// "feature:documentation");
    propertiesContent.put("TB_1_0", "");
  }

  public boolean isDiagram(EObject container) {
    return (container instanceof DataPkgImpl);
  }

  public void createDiagramTitleBlock(EObject elementView, EObject diagram) {
    if (elementView instanceof DSemanticDiagram && isUniqueDiagramTitleBlock(diagram)) {
      createTitleBlock(elementView, diagram);
    }
  }

  public void createElementTitleBlock(EObject elementView, EObject diagram) {
    if (!(elementView instanceof DSemanticDiagram)) {
      createTitleBlock(elementView, diagram);
    }
  }

  public boolean isDiagramTitleBlock(DAnnotation titleBlock) {
    List<EObject> references = titleBlock.getReferences().stream().filter(x -> !(x instanceof DAnnotation))
        .collect(Collectors.toList());
    if (references.isEmpty()) {
      return true;
    }
    return false;
  }

  private void createTitleBlock(EObject elementView, EObject diagram) {
    DRepresentation representation = null;
    if ((diagram instanceof DRepresentation)) {
      representation = (DRepresentation) diagram;
    }

    if (representation != null) {
      // todo - read from properties
      int numLines = 2;
      int numCols = 2;

      DAnnotation annotation = DescriptionFactory.eINSTANCE.createDAnnotation();
      annotation.setSource(TITLE_BLOCK);
      annotation.getDetails().put(VISIBILITY, TRUE);

      List<DAnnotation> annotationLines = new ArrayList<DAnnotation>();
      for (int i = 0; i < numLines; i++) {
        DAnnotation annotationLine = DescriptionFactory.eINSTANCE.createDAnnotation();
        annotationLine.setSource("TitleBlockLine");

        // addColumnsToLine(annotationLine, representation, numCols); start
        List<DAnnotation> annotationCols = new ArrayList<DAnnotation>();
        for (int j = 0; j < numCols; j++) {
          DAnnotation annotationCol = DescriptionFactory.eINSTANCE.createDAnnotation();
          annotationCol.setSource("TB_" + i + "_" + j);
          annotationCol.getDetails().put(NAME, "Name");
          annotationCol.getDetails().put(CONTENT, "feature:name");
          annotationCols.add(annotationCol);
          representation.getEAnnotations().add(annotationCol);
        }
        annotationLine.getReferences().addAll(annotationCols);
        representation.getEAnnotations().add(annotationLine);
        // stop

        annotationLines.add(annotationLine);
      }

      if (!elementView.equals(diagram)) {
        annotation.getReferences().add(((DDiagramElement) elementView).getTarget());
        annotation.getDetails().put(IS_ELEMENT_TITLE_BLOCK, TRUE);
      }
      annotation.getReferences().addAll(annotationLines);
      representation.getEAnnotations().add(annotation);
    }
  }

  public void createTitleBlockLine(EObject titleBlock, EObject diagram) {
    if (!isDiagramLevel(titleBlock)) {
      DRepresentation representation = null;
      if ((diagram instanceof DRepresentation)) {
        representation = (DRepresentation) diagram;
      }
      if (representation != null) {
        DAnnotation annotationLine = DescriptionFactory.eINSTANCE.createDAnnotation();
        annotationLine.setSource("TitleBlockLine");
        if (titleBlock instanceof DAnnotation) {
          int numCols = getNumColumns((DAnnotation) titleBlock);
          if (numCols > 0) {
            ((DAnnotation) titleBlock).getReferences().add(annotationLine);
            addColumnsToLine(annotationLine, representation, numCols, getNumLines((DAnnotation) titleBlock));
          }
        }
      }
    }
  }

  public void createTitleBlockColumn(EObject titleBlock, EObject diagram) {
    if (!isDiagramLevel(titleBlock)) {
      DRepresentation representation = null;
      if ((diagram instanceof DRepresentation)) {
        representation = (DRepresentation) diagram;
      }
      if (representation != null) {
        if (titleBlock instanceof DAnnotation) {
          int numLines = ((DAnnotation) titleBlock).getReferences().size();
          List<EObject> lines = ((DAnnotation) titleBlock).getReferences();
          for (int i = 0; i < numLines; i++) {
            DAnnotation annotationCol = DescriptionFactory.eINSTANCE.createDAnnotation();
            annotationCol.setSource("TitleBlockLineCol");
            if (lines.get(i) instanceof DAnnotation) {
              ((DAnnotation) (lines.get(i))).getReferences().add(annotationCol);
            }
          }
        }
      }
    }
  }

  private boolean isDiagramLevel(EObject titleBlock) {
    for (EObject reference : ((DAnnotation) titleBlock).getReferences()) {
      if (!(reference instanceof DAnnotation)) {
        return false;
      }
    }
    return true;
  }

  private void addColumnsToLine(DAnnotation annotationLine, DRepresentation representation, int numCols,
      int lineNumber) {
    List<DAnnotation> annotationCols = new ArrayList<DAnnotation>();
    for (int j = 0; j < numCols; j++) {
      DAnnotation annotationCol = DescriptionFactory.eINSTANCE.createDAnnotation();
      annotationCol.setSource("TB_" + lineNumber + "_" + j);
      annotationCols.add(annotationCol);
      representation.getEAnnotations().add(annotationCol);
    }
    annotationLine.getReferences().addAll(annotationCols);
    representation.getEAnnotations().add(annotationLine);
  }

  public int getNumLines(DAnnotation titleBlock) {
    return titleBlock.getReferences().size();
  }

  public int getNumColumns(DAnnotation titleBlock) {
    EObject obj = titleBlock.getReferences().get(1);
    if (obj instanceof DAnnotation)
      return ((DAnnotation) obj).getReferences().size();
    return 0;
  }

  public Collection<EObject> computeTitleBlockElements(EObject elem, EObject diagram) {
    Collection<EObject> result = new ArrayList<>();
    if ((diagram instanceof DRepresentation)) {
      DRepresentation representation = (DRepresentation) diagram;
      result = representation.getEAnnotations().stream().filter(x -> x.getReferences().contains(elem))
          .collect(Collectors.toList());
    }
    return result;
  }

  public List<DAnnotation> checkIsTitleBlockContainer(Object elementView) {
    List<DAnnotation> list = new ArrayList<DAnnotation>();
    if ((elementView instanceof DRepresentation)) {
      DRepresentation representation = (DRepresentation) elementView;
      list = representation.getEAnnotations().stream().filter(x -> x.getSource().equals(TITLE_BLOCK))
          .collect(Collectors.toList());
      deleteDanglingTitleBlock(list, elementView);
      // clearEAnnotations(elementView);
      list = list.stream().filter(x -> Objects.nonNull(x.getDetails().get(VISIBILITY)))
          .filter(x -> x.getDetails().get(VISIBILITY).equals(TRUE)).collect(Collectors.toList());
    }
    return list;
  }

  private void deleteDanglingTitleBlock(List<DAnnotation> list, Object elementView) {
    List<DAnnotation> deleteList = new ArrayList<DAnnotation>();
    for (DAnnotation annotation : list) {
      boolean hasExternalElementReference = false;
      for (EObject element : annotation.getReferences()) {
        if (!(element instanceof DAnnotation)) {
          hasExternalElementReference = true;
          boolean elementPresentInDiagram = false;
          List<DDiagramElement> diagramElementsList = ((DSemanticDiagram) elementView).getOwnedDiagramElements();
          for (DDiagramElement diagramElement : diagramElementsList) {
            if (!(diagramElement instanceof DEdge) && diagramElement.getTarget().equals(element)) {
              elementPresentInDiagram = true;
            }
          }
          if (!(elementPresentInDiagram)) {
            annotation.getDetails().put(VISIBILITY, FALSE);
          }
        }
      }
      if (!(hasExternalElementReference)) {
        deleteList.add(annotation);
      }
    }
    deleteList = deleteList.stream().filter(x -> Objects.nonNull(x.getDetails().get(IS_ELEMENT_TITLE_BLOCK)))
        .filter(x -> x.getDetails().get(IS_ELEMENT_TITLE_BLOCK).equals(TRUE)).collect(Collectors.toList());
    CapellaServices.getService().removeElements(deleteList);
  }

  public void clearEAnnotations(Object elementView) {
    List<DAnnotation> annotationsList = new ArrayList<>();
    List<DDiagramElement> diagramElementsList = ((DSemanticDiagram) elementView).getOwnedDiagramElements();
    if (!diagramElementsList.isEmpty()) {
      for (DDiagramElement diagramElement : diagramElementsList) {
        if (diagramElement.getTarget() instanceof DAnnotation) {
          DAnnotation annotation = (DAnnotation) diagramElement.getTarget();
          // annotationsList.add(annotation); // title block
          if (!annotation.getReferences().isEmpty()) {
            for (EObject titleBlockLine : annotation.getReferences()) {
              if (titleBlockLine instanceof DAnnotation) {
                annotationsList.add((DAnnotation) titleBlockLine); // title block lines
                if (!((DAnnotation) titleBlockLine).getReferences().isEmpty()) {
                  for (EObject titleBlockCell : ((DAnnotation) titleBlockLine).getReferences()) {
                    if (titleBlockCell instanceof DAnnotation) {
                      annotationsList.add((DAnnotation) titleBlockCell);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    ((DSemanticDiagram) elementView).getEAnnotations().removeAll(annotationsList);
    System.out.println("test");
  }

  public List<Object> getTitleBlockCellContent(EObject diagram, EObject cell, EObject containerView) {
    init();
    List<Object> list = new ArrayList<Object>();
    if ((diagram instanceof DRepresentation)) {
      if (cell instanceof DAnnotation) {
        DAnnotation annotation = (DAnnotation) ((DNodeContainer) (containerView.eContainer().eContainer())).getTarget();
        FeatureInterpreter interpreter = new FeatureInterpreter();
        try {
          String feature = ((DAnnotation) cell).getDetails().get(CONTENT);
          if (feature != null) {
            EObject objToEvaluate = diagram;
            List<EObject> modelElements = annotation.getReferences().parallelStream()
                .filter(x -> !(x instanceof DAnnotation)).collect(Collectors.toList());
            if (!modelElements.isEmpty()) {
              objToEvaluate = modelElements.get(0);
            }
            Object obj = interpreter.evaluate(objToEvaluate, feature);
            if (obj != null) {
              if (obj instanceof EObject) {
                list.add(obj);
              } else if (obj instanceof String) {
                DAnnotation annotationContent = DescriptionFactory.eINSTANCE.createDAnnotation();
                annotationContent.setSource("abc");
                annotationContent.getDetails().put(CONTENT, (String) obj);
                list.add(annotationContent);
              }
            } else {
              DRepresentation representation = (DRepresentation) diagram;
              list = representation.getOwnedRepresentationElements().stream()
                  .filter(x -> (x.getTarget() instanceof ClassImpl)).collect(Collectors.toList());
            }
          }
        } catch (EvaluationException e) {
          e.printStackTrace();
        }
      }
    }
    return list;
  }

  public String getTitleBlockCellLabel(EObject diagram, EObject cell) {
    init();
    if (cell instanceof DAnnotation) {
      String name = ((DAnnotation) cell).getDetails().get(NAME);
      if (name != null)
        return name;
    }
    return "";
  }

  public String getCellLabel(EObject obj) {
    if (obj instanceof DAnnotation) {
      DAnnotation annotation = (DAnnotation) obj;
      return annotation.getDetails().get(CONTENT);
    }
    return "";
  }

  public boolean isAnnotation(EObject obj) {
    return obj instanceof DAnnotation;
  }

  public boolean isTitleBlockAllowed(EObject element, EObject containerView) {
    if (containerView instanceof DRepresentation) {
      return true;
    }
    return false;
  }

  public List<DAnnotation> getAvailableTitleBlocksToInsert(final EObject elementView, boolean isDiagramTitleBlock) {
    List<DAnnotation> result = new ArrayList<>();
    EList<DAnnotation> eList = ((DDiagram) elementView).getEAnnotations();
    for (DAnnotation elem : eList) {
      if (elem.getSource().equals(TITLE_BLOCK)) {
        result.add(elem);
      }
    }
    List<DAnnotation> finalResult = new ArrayList<>();
    for (DAnnotation annotation : result) {
      if (isDiagramTitleBlock) {
        if (isDiagramTitleBlock(annotation)) {
          finalResult.add(annotation);
        }
      } else {
        if (!isDiagramTitleBlock(annotation)) {
          finalResult.add(annotation);
        }
      }
    }
    return finalResult;
  }

  public List<DAnnotation> getInitialSelectionOfShowHideTitleBlocks(final EObject elementView,
      boolean isDiagramTitleBlock) {
    List<DAnnotation> result = new ArrayList<>(1);
    DSemanticDiagram diagram = (DSemanticDiagram) CapellaServices.getService().getDiagramContainer(elementView);
    if (elementView.equals(diagram)) {
      EList<DAnnotation> diagramElements = diagram.getEAnnotations();
      for (DAnnotation dDiagramElement : diagramElements) {
        if (dDiagramElement.getSource().equals(TITLE_BLOCK)) {
          if (null != dDiagramElement.getDetails().get(VISIBILITY)) {
            if (dDiagramElement.getDetails().get(VISIBILITY).equals(TRUE)) {
              result.add(dDiagramElement);
            }
          }
        }
      }
    }
    List<DAnnotation> finalResult = new ArrayList<>();
    for (DAnnotation annotation : result) {
      if (isDiagramTitleBlock) {
        if (isDiagramTitleBlock(annotation)) {
          finalResult.add(annotation);
        }
      } else {
        if (!isDiagramTitleBlock(annotation)) {
          finalResult.add(annotation);
        }
      }
    }
    return finalResult;
  }

  public EObject showHideTitleBlocks(EObject context, List<DAnnotation> selectedTitleBlocks, DDiagram diagram,
      boolean isDiagram) {
    Map<DAnnotation, DDiagramElement> visibleElements = new HashMap<>();
    List<EObject> allNodes = new ArrayList<>();
    allNodes.addAll(((DSemanticDiagram) context).getOwnedDiagramElements());
    for (EObject aObject : allNodes) {
      if (aObject instanceof DNodeContainer) {
        if (((DNodeContainer) aObject).getTarget() instanceof DAnnotation) {
          if (((DAnnotation) ((DNodeContainer) aObject).getTarget()).getSource().equals(TITLE_BLOCK)) {
            DDiagramElement aNode = ((DDiagramElement) aObject);
            EObject nodeTarget = aNode.getTarget();
            if (nodeTarget instanceof DAnnotation && aNode instanceof DDiagramElement) {
              visibleElements.put((DAnnotation) nodeTarget, aNode);
            }
          }
        }
      }
    }
    for (Entry<DAnnotation, DDiagramElement> me : visibleElements.entrySet()) {
      if (!selectedTitleBlocks.contains(me.getKey())) {
        EObject container = me.getValue().eContainer();
        if (container instanceof DSemanticDiagram) {
          DAnnotation annotation = ((DAnnotation) (me.getValue().getTarget()));
          if (isDiagram) {
            if (isDiagramTitleBlock(annotation)) {
              annotation.getDetails().put(VISIBILITY, FALSE);
              DiagramServices.getDiagramServices().removeAbstractDNodeView((DNodeContainer) me.getValue());
            }
          } else {
            if (!isDiagramTitleBlock(annotation)) {
              annotation.getDetails().put(VISIBILITY, FALSE);
              DiagramServices.getDiagramServices().removeAbstractDNodeView((DNodeContainer) me.getValue());
            }
          }
        }
      }
    }
    for (DAnnotation aTitleBlock : selectedTitleBlocks) {
      if (!visibleElements.containsKey(aTitleBlock)) {

        createTitleBlockView(context, aTitleBlock, diagram);
      }
    }
    return context;
  }

  private AbstractDNode createTitleBlockView(EObject context, DAnnotation titleBlock, DDiagram diagram) {
    String mappingName = IMappingNameConstants.CDB_TITLE_BLOCK;
    NodeMapping mapping = DiagramServices.getDiagramServices().getNodeMapping(diagram, mappingName);
    if (context instanceof DNodeList) {
      if (null != titleBlock.getDetails().get(VISIBILITY)) {
        titleBlock.getDetails().put(VISIBILITY, TRUE);
      }
      return DiagramServices.getDiagramServices().createDNodeListElement(mapping, titleBlock,
          (DragAndDropTarget) context, diagram);
    }
    if (null != titleBlock.getDetails().get(VISIBILITY)) {
      titleBlock.getDetails().put(VISIBILITY, TRUE);
    }
    return DiagramServices.getDiagramServices().createNode(mapping, titleBlock, (DragAndDropTarget) context, diagram);
  }

  public boolean isUniqueDiagramTitleBlock(EObject diagram) {
    Collection<DAnnotation> result = new ArrayList<>();
    if ((diagram instanceof DRepresentation)) {
      DRepresentation representation = (DRepresentation) diagram;
      result = representation.getEAnnotations().stream().filter(x -> x.getSource().equals(TITLE_BLOCK))
          .filter(x -> x.getReferences().size() == 2).collect(Collectors.toList());
    }
    return (result.size() == 0);
  }

}