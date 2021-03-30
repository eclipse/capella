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
 *    Thales Global Services - initial API and implementation
 *******************************************************************************/
package org.polarsys.capella.test.semantic.queries.ju.testcases;

import org.polarsys.capella.test.semantic.queries.ju.model.SemanticQueries;

import junit.framework.Test;

public class DataTypeElementsNullValue extends SemanticQueries {
  String QUERY = "org.polarsys.capella.core.semantic.queries.DataTypeElements_nullValue";

  @Override
  protected String getQueryCategoryIdentifier() {
    return QUERY;
  }

  @Override
  public void test() throws Exception {
    testQuery(SA_ENUMERATION_4, SA_NULL_ENUMERATIONLITERAL_1);
    testQuery(SA_NUMERICTYPE_3, SA_NUMERIC_TYPE_NULL_NUMERICREFERENCE_1);
    testQuery(SA_PHYSICALQUANTITY_2_UNIT_1, SA_PHYSICAL_QUANTITY_NULL_NUMERICREFERENCE_1);
    testQuery(SA_STRINGTYPE_8, SA_STRINGTYPE_NULL);
  }

  public static Test suite() {
    return new DataTypeElementsNullValue();
  }
}
