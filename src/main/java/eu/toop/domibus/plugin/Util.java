package eu.toop.domibus.plugin;

import eu.domibus.plugin.Submission;

import java.util.Iterator;

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
