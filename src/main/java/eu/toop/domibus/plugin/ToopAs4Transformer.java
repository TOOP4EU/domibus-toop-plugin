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

/**
 *
 * @author Muhammet Yildiz
 */
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
