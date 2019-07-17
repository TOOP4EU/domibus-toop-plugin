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

/**
 * This interface contains static final values
 *
 * @author Muhammet Yildiz
 */
public interface Statics {
  String ACTION_DELIVER = "Deliver";
  String ACTION_NOTIFY = "Notify";
  String ACTION_SUBMISSION_RESULT = "SubmissionResult";
  String ACTION_SUBMIT = "Submit";
  String PROP_MESSAGEID = "MessageId"; //
  String PROP_CONVERSATIONID = "ConversationId"; //
  String PROP_FROMPARTYID = "FromPartyId"; //
  String PROP_FROMPARTYID_TYPE = "FromPartyIdType"; //
  String PROP_FROMPARTYROLE = "FromPartyRole";
  String PROP_TOPARTYID = "ToPartyId"; //
  String PROP_TOPARTYID_TYPE = "ToPartyIdType"; //
  String PROP_TOPARTYROLE = "ToPartyRole"; //
  String PROP_TOPARTYCERT = "ToPartyCertificate"; //
  String PROP_SERVICE = "Service";  //
  String PROP_SERVICE_TYPE = "ServiceType";  //
  String PROP_ACTION = "Action";  //
  String PROP_ORIGINALSENDER = "originalSender"; //
  String PROP_FINALRECIPIENT = "finalRecipient"; //
  String PROP_TARGETURL = "TargetURL"; //
  String PROP_SIGNALTYPE = "SignalType";
  String PROP_SIGNALTYPE_RECEIPT = "Receipt";
  String PROP_SIGNALTYPE_ERROR = "Error";
  String PROP_ERRORCODE = "ErrorCode";
  String PROP_SHORT_DESCRIPTION = "ShortDescription";
  String PROP_DESCRIPTION = "Description";
  String PROP_SEVERITY = "Severity";
  String PROP_REFTOMESSAGEID = "RefToMessageId";
  String UNREGISTERED_TYPE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
}
