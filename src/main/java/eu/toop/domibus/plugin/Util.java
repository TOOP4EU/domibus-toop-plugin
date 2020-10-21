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
import java.util.Set;

/**
 *
 * @author Muhammet Yildiz
 */
public class Util {
  public static String getFromPartyIds(Submission submission) {
    final Set<Submission.Party> parties = submission.getFromParties();

    return getPartyIds(parties);
  }

  public static String getToPartyIds(Submission submission) {
    final Set<Submission.Party> parties = submission.getToParties();
    return getPartyIds(parties);
  }


  private static String getPartyIds(Set<Submission.Party> parties) {
    StringBuffer froms = new StringBuffer();
    Iterator<Submission.Party> iterator = parties.iterator();
    while (iterator.hasNext()) {
      final Submission.Party party = iterator.next();
      froms.append(party.getPartyId()).append("::").append(party.getPartyIdType());
    }

    return froms.toString();
  }
}
