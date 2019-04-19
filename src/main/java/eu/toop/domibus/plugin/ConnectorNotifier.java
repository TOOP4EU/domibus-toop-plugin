package eu.toop.domibus.plugin;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResult;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.Submission;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static eu.toop.domibus.plugin.Tracker.kvp;

/**
 * This class contains the methods that are used for sending back the notifications to the
 * toop connector.
 */
public class ConnectorNotifier implements Statics {

  private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ConnectorNotifier.class);


  @Autowired
  private Settings settings;


  @Autowired
  private Tracker tracker;

  private boolean enableNotifications;

  /**
   * Sends a SubmissionResult notification back to the configured
   * connector
   *
   * @param toopConnectorAdapter
   * @param request
   * @param toSubmit
   */
  public void sendBackSubmissionSuccess(ToopConnectorAdapterImpl toopConnectorAdapter, Submission request, Submission toSubmit) {
    sendBackSubmissionResult(toopConnectorAdapter, request, toSubmit, true, null);
  }

  /**
   * Sends a SubmissionResult that contains error information
   * to the toop-connector
   *
   * @param toopConnectorAdapter
   * @param request
   * @param toSubmit
   * @param message
   */
  public void sendBackSubmissionFailure(ToopConnectorAdapterImpl toopConnectorAdapter, Submission request, Submission toSubmit, String message) {
    sendBackSubmissionResult(toopConnectorAdapter, request, toSubmit, false, message);
  }

  /**
   * <table>
   * <tr><th>MessageProperty</th><th>required?</th><th>Desc</th></tr>
   * <tr><td>RefToMessageId</td><td>Y</td><td>Value of the //eb:MessageId element of the received Submit message</td></tr>
   * <tr><td>Result</td><td>Y</td><td>Accepted or Error</td></tr>
   * <tr><td>MessageId</td><td>C</td><td>The value of the //eb:MessageId element of the  AS4 message resulting from this submission</td></tr>
   * <tr><td>Description</td><td>C</td><td>Free text describing the reason why the submission was rejected.</td></tr>
   * </table>
   *
   * @param toopConnectorAdapter
   * @param request
   * @param as4Message
   * @param b
   * @param message
   */
  void sendBackSubmissionResult(ToopConnectorAdapterImpl toopConnectorAdapter, Submission request, Submission as4Message, boolean b, String message) {
    //create a submission result and send it to the connector

    String targetAction = ACTION_SUBMISSION_RESULT;
    tracker.writeInfo("sendBackSubmissionResult",
        kvp("Action", targetAction),
        kvp("Req. MessageId", request.getMessageId()),
        kvp("As4. MessageId", as4Message.getMessageId()),
        kvp("From          ", settings.getGatewayPartyID()),
        kvp("To            ", settings.getConnectorPartyID()),
        kvp("Success       ", b + "")
    );


    if (!enableNotifications) {
      LOG.warn("Notifications disabled");
      return;
    }

    Submission toDeliver = new Submission();

    toDeliver.setRefToMessageId(as4Message.getMessageId());

    fillCommonValues(targetAction, toDeliver);

    toDeliver.addMessageProperty(PROP_REFTOMESSAGEID, request.getMessageId());
    toDeliver.addMessageProperty(PROP_MESSAGEID, as4Message.getMessageId());
    toDeliver.addMessageProperty(PROP_ORIGINALSENDER, settings.getGatewayPartyID());
    toDeliver.addMessageProperty(PROP_FINALRECIPIENT, settings.getConnectorPartyID());

    if (b) {
      toDeliver.addMessageProperty("Result", "Accepted");
    } else {
      toDeliver.addMessageProperty("Result", "Error");
      if (message != null)
        toDeliver.addMessageProperty(PROP_DESCRIPTION, message);
    }

    try {
      LOG.info("Submit " + toDeliver.getMessageId());
      toopConnectorAdapter.submit(toDeliver);
    } catch (Exception e) {
      LOG.error("Error when notifying message! ", e);
    }
  }

  private void fillCommonValues(String targetAction, Submission toDeliver) {
    toDeliver.setAction(targetAction);
    toDeliver.setService(settings.getToopInterfaceService());
    toDeliver.addFromParty(settings.getGatewayPartyID(), UNREGISTERED_TYPE);
    toDeliver.setFromRole(settings.getGatewayRole());
    toDeliver.addToParty(settings.getConnectorPartyID(), UNREGISTERED_TYPE);
    toDeliver.setToRole(settings.getBackendRole());
  }

  public void sendBackRelayResult(ToopConnectorAdapterImpl toopConnectorAdapter, String messageId, boolean b) {
    String targetAction = ACTION_NOTIFY;

    tracker.writeInfo("sendBackRelayResult",
        kvp("Action", targetAction),
        kvp("As4. MessageId", messageId),
        kvp("From          ", settings.getGatewayPartyID()),
        kvp("To            ", settings.getConnectorPartyID())
    );

    if (!enableNotifications) {
      LOG.warn("Notifications disabled");
      return;
    }

    Submission toDeliver = new Submission();

    fillCommonValues(targetAction, toDeliver);

    toDeliver.setRefToMessageId(messageId);
    toDeliver.addMessageProperty(PROP_MESSAGEID, messageId);
    //TODO: Fix this with respect to the spec
    toDeliver.addMessageProperty(PROP_REFTOMESSAGEID, messageId);
    toDeliver.addMessageProperty(PROP_ORIGINALSENDER, settings.getGatewayPartyID());
    toDeliver.addMessageProperty(PROP_FINALRECIPIENT, settings.getConnectorPartyID());

    LOG.info("Add the last error for " + messageId);

    if (b) {
      toDeliver.addMessageProperty("Result", "Receipt");
    } else {
      toDeliver.addMessageProperty("Result", "Error");
      List<ErrorResult> errors = toopConnectorAdapter.getErrorsForMessage(messageId);
      LOG.info("errors size  " + errors.size());
      ErrorResult lastError = errors.get(errors.size() - 1);
      toDeliver.addMessageProperty(PROP_ERRORCODE, lastError.getErrorCode().getErrorCodeName());
      ErrorCode.EbMS3ErrorCode errorCode = ErrorCode.EbMS3ErrorCode.findErrorCodeBy(lastError.getErrorCode().getErrorCodeName());
      toDeliver.addMessageProperty(PROP_SHORT_DESCRIPTION, errorCode.getShortDescription());
      toDeliver.addMessageProperty(PROP_SEVERITY, errorCode.getSeverity());
    }

    try {
      LOG.info("Submit " + toDeliver.getMessageId());
      toopConnectorAdapter.submit(toDeliver);
    } catch (Exception e) {
      LOG.error("Error when notifying message! ", e);
    }
  }


  public boolean isEnableNotifications() {
    return enableNotifications;
  }

  public void setEnableNotifications(boolean enableNotifications) {
    this.enableNotifications = enableNotifications;
  }
}
