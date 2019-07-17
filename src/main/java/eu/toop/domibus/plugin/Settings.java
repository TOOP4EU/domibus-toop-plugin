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
 * This bean exposes the settings read from the plugin configuration
 * xml file
 *
 * @author Muhammet Yildiz
 */
public class Settings {
  private String connectorPartyID;
  private String gatewayPartyID;
  private String gatewayRole;
  private String toopInterfaceService;
  private String backendRole;


  public String getConnectorPartyID() {
    return connectorPartyID;
  }

  public void setConnectorPartyID(String connectorPartyID) {
    this.connectorPartyID = connectorPartyID;
  }

  public String getGatewayPartyID() {
    return gatewayPartyID;
  }

  public void setGatewayPartyID(String gatewayPartyID) {
    this.gatewayPartyID = gatewayPartyID;
  }

  public String getGatewayRole() {
    return gatewayRole;
  }

  public void setGatewayRole(String gatewayRole) {
    this.gatewayRole = gatewayRole;
  }

  public String getToopInterfaceService() {
    return toopInterfaceService;
  }

  public void setToopInterfaceService(String toopInterfaceService) {
    this.toopInterfaceService = toopInterfaceService;
  }

  public String getBackendRole() {
    return backendRole;
  }

  public void setBackendRole(String backendRole) {
    this.backendRole = backendRole;
  }
}
