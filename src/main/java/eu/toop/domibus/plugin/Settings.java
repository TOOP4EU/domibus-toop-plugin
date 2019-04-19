package eu.toop.domibus.plugin;

/**
 * This bean exposes the settings read from the plugin configuration
 * xml file
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
