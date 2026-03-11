/*
 * Copyright (C) 2021
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program;
 * If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.edyp.epims.transfer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListUtils {

  protected static Logger logger = LoggerFactory.getLogger(ListUtils.class);
  /**
   *  create specified number of sublist from specified List.
   *  Same number of elements in each list (lasts may be smaller)
   * @param completeList the List to split
   * @param nbSubList number of sublist to create
   * @return a list of all sublsit, same type as completeList
   */
  public static <T> List<List<T>> splitInto(List<T> completeList, int nbSubList) {

    int initialSize = completeList.size();
    int[] sizeOfSublist = new int[nbSubList];

    //Init with fixed size
    Arrays.fill(sizeOfSublist, initialSize / nbSubList);
    //Add remaining elements into first lists
    for (int i = 0; i < initialSize % nbSubList; i++) {
      sizeOfSublist[i]++;
    }

    //Create sublist
    int lastElem = 0;
    List<List<T>> subLists = new ArrayList<>();
    for (int i = 0; i < nbSubList; i++) {
      subLists.add(completeList.subList(lastElem, lastElem + sizeOfSublist[i]));
      lastElem += sizeOfSublist[i];
    }
//    subLists.forEach(sl -> logger.debug(" Sublist size "+sl.size()) );
    return subLists;
  }
}
