/**
 * Copyright (C) 2018-2019 toop.eu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.toop.domibus.plugin;

import eu.domibus.plugin.Submission;

import java.util.Iterator;

/**
 *
 * @author Muhammet Yildiz
 */
public class Util {
  public static String getFromPartyIds(Submission submission) {
    StringBuffer froms = new StringBuffer();
    Iterator<Submission.Party> iterator = submission.getFromParties().iterator();
    while (iterator.hasNext()){
      froms.append(iterator.next().getPartyId()).append(',');
    }

    return froms.toString();
  }

  public static String getToPartyIds(Submission submission) {
    StringBuffer froms = new StringBuffer();
    Iterator<Submission.Party> iterator = submission.getToParties().iterator();
    while (iterator.hasNext()){
      froms.append(iterator.next().getPartyId()).append(',');
    }

    return froms.toString();
  }
}
