package eu.toop.domibus.plugin;


import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Iterator;

import static eu.toop.domibus.plugin.Tracker.kvp;

@Service
public class ToopAs4Transformer implements MessageSubmissionTransformer<Submission>, MessageRetrievalTransformer<Submission> {

  private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ToopAs4Transformer.class);

  public ToopAs4Transformer() {
    LOG.debug("Creating ToopAs4Transformer");
  }

  @Override
  public Submission transformFromSubmission(final Submission submission, final Submission target) {
    LOG.debug("Transform from submission");
    Collection<Submission.TypedProperty> messageProperties = submission.getMessageProperties();
    String sNewTargetURL = null;
    String sNewC3Cert = null;
    Iterator<Submission.TypedProperty> iterator = messageProperties.iterator();
    while (iterator.hasNext()) {
      Submission.TypedProperty property = iterator.next();
      if (property.getKey().equals("TargetURL")) {
        sNewTargetURL = property.getValue();
        iterator.remove();
      } else if (property.getKey().equals("ToPartyCertificate")) {
        sNewC3Cert = property.getValue();
        iterator.remove();
      }
    }

    LOG.info("New C3Certificate " + sNewC3Cert);
    LOG.info("New C3URL " + sNewTargetURL);

    //now put in the new values
    if (sNewC3Cert != null)
      submission.addMessageProperty("C3Certificate", sNewC3Cert);

    if (sNewTargetURL != null)
      submission.addMessageProperty("C3URL", sNewTargetURL);

    //return the original one.
    return submission;
  }

  @Override
  public Submission transformToSubmission(final Submission submission) {
    return submission;
  }
}
