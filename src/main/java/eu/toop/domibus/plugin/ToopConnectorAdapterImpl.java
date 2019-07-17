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

import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.common.MessageStatusChangeEvent;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static eu.toop.domibus.plugin.Tracker.kvp;

/**
 *
 * @author Muhammet Yildiz
 */
public class ToopConnectorAdapterImpl extends AbstractBackendConnector<Submission, Submission> implements Statics {

  private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ToopConnectorAdapterImpl.class);

  public static Set<String> submittedMessageIds = new HashSet<>();

  @Autowired
  private Settings settings;

  @Autowired
  private Tracker tracker;

  @Autowired
  private ConnectorNotifier connectorNotifier;

  @Autowired
  private ToopAs4Transformer messageRetrievalTransformer;

  @Autowired
  private ToopAs4Transformer messageSubmissionTransformer;

  public ToopConnectorAdapterImpl() {
    super("Toop AS4 Backend Interface Plugin");
  }

  @Override
  public MessageSubmissionTransformer<Submission> getMessageSubmissionTransformer() {
    return messageSubmissionTransformer;
  }

  @Override
  public MessageRetrievalTransformer<Submission> getMessageRetrievalTransformer() {
    return messageRetrievalTransformer;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void deliverMessage(final String messageId) {
    Submission request;
    LOG.info("Deliver message: " + messageId);
    try {
      LOG.info("Download message: " + messageId);
      request = super.downloadMessage(messageId, null);

      String action = request.getAction();
      LOG.info("Received action: " + action);
      if (request == null) {
        LOG.error("Failed to download the message " + messageId);
      }
      LOG.info("Message downloaded successfully " + messageId);
      LOG.info("Number of payloads" + request.getPayloads().size());


      tracker.writeInfo("deliverMessage",
          kvp("Action", action),
          kvp("As4. MessageId", messageId),
          kvp("From          ", Util.getFromPartyIds(request)),
          kvp("To            ", Util.getToPartyIds(request))
      );


      switch (action) {
        case ACTION_SUBMIT:
          handleSubmit(request);
          break;
        default:
          handleDeliver(request);
          break;
      }
    } catch (Exception e) {
      LOG.error("Error when delivering message! ", e);
    }
  }

  /**
   * When a receipt is received from C3 this function is called.
   *
   * @param messageId
   */
  @Override
  public void messageSendSuccess(String messageId) {
    LOG.info("Handle messageSendSuccess for " + messageId);


    tracker.writeInfo("messageSendSuccess",
        kvp("As4. MessageId", messageId)
    );

    if (!submittedMessageIds.remove(messageId))
      return;

    connectorNotifier.sendBackRelayResult(this, messageId, true);
  }

  /**
   * Couldn't send message to C3
   *
   * @param messageId
   */
  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void messageSendFailed(String messageId) {
    tracker.writeInfo("messageSendFailed",
        kvp("As4. MessageId", messageId)
    );

    LOG.info("Handle messageSendFailed for " + messageId);

    if (!submittedMessageIds.remove(messageId))
      return;

    connectorNotifier.sendBackRelayResult(this, messageId, false);
  }

  /**
   * Delivery from C3 to be sent to C4.
   *
   * <table>
   * <thead>
   * <tr><th>Message Property Name</th><th>Required</th>
   * <th>Taken from header element</th></tr></thead>
   * <tr><td>ToPartyRole</td><td>Y</td><td><code>//eb:To/eb:Role</code></td></tr>
   * <tr><td>ToPartyIdType</td><td>N</td><td><code>//eb:To/eb:PartyId/@type</code></td></tr>
   * <tr><td>ToPartyId</td><td>Y</td><td><code>//eb:To/eb:PartyId</code></td></tr>
   * <tr><td>ServiceType</td><td>N</td><td><code>//eb:CollaborationInfo/eb:Service/@type</code></td></tr>
   * <tr><td>Service</td><td>Y</td><td><code>//eb:CollaborationInfo/eb:Service</code></td></tr>
   * <tr><td>RefToMessageId</td><td>N</td><td><code>//eb:MessageInfo/eb:RefToMessageId</code></td></tr>
   * <tr><td>MessageId</td><td>Y</td><td><code>//eb:MessageInfo/eb:MessageId</code></td></tr>
   * <tr><td>FromPartyRole</td><td>Y</td><td><code>//eb:From/eb:Role</code></td></tr>
   * <tr><td>FromPartyIdType</td><td>N</td><td><code>//eb:From/eb:PartyId/@type</code></td></tr>
   * <tr><td>FromPartyId</td><td>Y</td><td><code>//eb:From/eb:PartyId</code></td></tr>
   * <tr><td>ConversationId</td><td>Y</td><td><code>//eb:CollaborationInfo/eb:ConversationId</code></td></tr>
   * <tr><td>Action</td><td>Y</td><td><code>//eb:CollaborationInfo/eb:Action</code></td></tr>
   * </table>
   *
   * @param request
   */
  private void handleDeliver(Submission request) throws Exception {

    tracker.writeInfo("handleDeliver",
        kvp("Action", request.getAction()),
        kvp("As4. MessageId", request.getMessageId()),
        kvp("From          ", Util.getFromPartyIds(request)),
        kvp("To            ", Util.getToPartyIds(request))
    );

    // log
    LOG.info("Handle deliver " + request.getMessageId());
    Submission toDeliver = new Submission();
    toDeliver.setAction(ACTION_DELIVER);
    toDeliver.setService(settings.getToopInterfaceService());
    toDeliver.setFromRole(settings.getGatewayRole());
    toDeliver.setToRole(settings.getBackendRole());
    toDeliver.addFromParty(settings.getGatewayPartyID(), UNREGISTERED_TYPE);
    toDeliver.addToParty(settings.getConnectorPartyID(), UNREGISTERED_TYPE);


    addPropertyIfNotNull(toDeliver, request.getRefToMessageId(), PROP_REFTOMESSAGEID);
    toDeliver.addMessageProperty(PROP_MESSAGEID, request.getMessageId());
    toDeliver.addMessageProperty(PROP_CONVERSATIONID, request.getConversationId());
    toDeliver.addMessageProperty(PROP_SERVICE, request.getService());
    addPropertyIfNotNull(toDeliver, request.getServiceType(), PROP_SERVICE_TYPE);
    toDeliver.addMessageProperty(PROP_ACTION, request.getAction());

    Submission.Party toPartyId = request.getToParties().iterator().next();
    toDeliver.addMessageProperty(PROP_TOPARTYID, toPartyId.getPartyId());
    toDeliver.addMessageProperty(PROP_TOPARTYROLE, request.getToRole());
    addPropertyIfNotNull(toDeliver, toPartyId.getPartyIdType(), PROP_TOPARTYID_TYPE);
    Submission.Party fromPartyId = request.getFromParties().iterator().next();
    toDeliver.addMessageProperty(PROP_FROMPARTYID, fromPartyId.getPartyId());
    toDeliver.addMessageProperty(PROP_FROMPARTYROLE, request.getFromRole());
    addPropertyIfNotNull(toDeliver, fromPartyId.getPartyIdType(), PROP_FROMPARTYID_TYPE);

    for (Submission.TypedProperty typedProperty : request.getMessageProperties()) {
      if (PROP_ORIGINALSENDER.equals(typedProperty.getKey()) || PROP_FINALRECIPIENT.equals(typedProperty.getKey())) {
        toDeliver.addMessageProperty(typedProperty.getKey(), typedProperty.getValue(), typedProperty.getType());
      }
    }

    LOG.info("Add payloads for " + request.getMessageId());
    for (Submission.Payload payload : request.getPayloads()) {
      byte[] bytes = new byte[0];
      try {
        bytes = IOUtils.toByteArray(payload.getPayloadDatahandler().getInputStream());
      } catch (IOException e) {
        LOG.info("Exception reading the payload input stream: " + payload.getContentId());
      }
      String payloadId = payload.getContentId();
      String mimeType = "application/gzip";
      DataHandler dataHandler = new DataHandler(new ByteArrayDataSource(bytes, mimeType));
      Submission.Payload copyPayload = new Submission.Payload(payloadId, dataHandler, payload.getPayloadProperties(), payload.isInBody(), payload.getDescription(), payload.getSchemaLocation());
      toDeliver.addPayload(copyPayload);
    }

    LOG.info("Submit " + toDeliver.getMessageId());
    super.submit(toDeliver);
  }

  private void addPropertyIfNotNull(Submission submission, String value, String propName) {
    if (value != null) {
      submission.addMessageProperty(propName, value);
    }
  }

  private void handleSubmit(Submission request) {

    LOG.info("Handle Submit action on " + request.getMessageId());
    Submission toSubmit = new Submission();
    Collection<Submission.TypedProperty> properties = request.getMessageProperties();
    for (Submission.TypedProperty property : properties) {
      LOG.info(property.getKey() + " " + property.getType() + " " + property.getValue());
      switch (property.getKey()) {
        case PROP_MESSAGEID: //Toop
          toSubmit.setMessageId(property.getValue());
          break;
        case PROP_REFTOMESSAGEID:
          toSubmit.setRefToMessageId(property.getValue());
          break;
        case PROP_CONVERSATIONID: //Toop
          toSubmit.setConversationId(property.getValue());
          break;
        case PROP_TOPARTYROLE: //Toop
          toSubmit.setToRole(property.getValue());
          break;
        case PROP_TOPARTYID: //Toop
          LOG.info(property.getValue() + (StringUtils.isEmpty(property.getType()) ? UNREGISTERED_TYPE : property.getType()));
          toSubmit.addToParty(property.getValue(), StringUtils.isEmpty(property.getType()) ? UNREGISTERED_TYPE : property.getType());
          break;
        case PROP_SERVICE: //Toop
          toSubmit.setService(property.getValue());
          break;
        case PROP_ACTION: //Toop
          toSubmit.setAction(property.getValue());
          break;
        case PROP_ORIGINALSENDER:
          toSubmit.addMessageProperty(PROP_ORIGINALSENDER, property.getValue(), null);
          break;
        case PROP_FINALRECIPIENT:
          toSubmit.addMessageProperty(PROP_FINALRECIPIENT, property.getValue(), null);
          break;
        default:
          toSubmit.addMessageProperty(property.getKey(), property.getValue(), null);
          break;
      }
    }

    LOG.info("Add payloads for " + request.getMessageId());

    for (Submission.Payload payload : request.getPayloads()) {
      byte[] bytes = new byte[0];
      try {
        bytes = IOUtils.toByteArray(payload.getPayloadDatahandler().getInputStream());
      } catch (IOException e) {
        LOG.info("Exception reading the payload input stream: " + payload.getContentId());
      }
      String payloadId = payload.getContentId();
      DataHandler dataHandler = new DataHandler(new ByteArrayDataSource(bytes, null));
      Submission.Payload copyPayload = new Submission.Payload(payloadId, dataHandler, payload.getPayloadProperties(), payload.isInBody(), payload.getDescription(), payload.getSchemaLocation());
      toSubmit.addPayload(copyPayload);
    }

    toSubmit.addFromParty(settings.getGatewayPartyID(), UNREGISTERED_TYPE);
    toSubmit.setFromRole(settings.getGatewayRole());

    tracker.writeInfo("handleSubmit",
        kvp("Action", request.getAction()),
        kvp("As4. MessageId", request.getMessageId()),
        kvp("From          ", Util.getFromPartyIds(request)),
        kvp("To            ", Util.getToPartyIds(request)),
        kvp("New Message            ", "---------"),
        kvp("MessageId", toSubmit.getMessageId()),
        kvp("Action", toSubmit.getAction()),
        kvp("From          ", Util.getFromPartyIds(toSubmit)),
        kvp("To            ", Util.getToPartyIds(toSubmit)));

    try {
      super.submit(toSubmit);
      submittedMessageIds.add(toSubmit.getMessageId());
      connectorNotifier.sendBackSubmissionSuccess(this, request, toSubmit);
    } catch (Exception ex) {
      LOG.error(ex.getMessage(), ex);
      connectorNotifier.sendBackSubmissionFailure(this, request, toSubmit, ex.getMessage());
    }
  }

  @Override
  public void messageStatusChanged(MessageStatusChangeEvent event) {
    super.messageStatusChanged(event);
  }

  @Override
  public void messageReceiveFailed(MessageReceiveFailureEvent messageReceiveFailureEvent) {
    LOG.error("Message receive failed");
    LOG.error(messageReceiveFailureEvent.getMessageId());
    LOG.error(messageReceiveFailureEvent.getEndpoint());
  }
}
