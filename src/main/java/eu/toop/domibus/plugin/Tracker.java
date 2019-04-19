package eu.toop.domibus.plugin;

import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Tracker {
  private String title;

  static class KeyValuePair {
    public final String key;
    public final String value;

    public KeyValuePair(String key, String value) {
      this.key = key;
      this.value = value;
    }
  }

  PrintWriter trackWriter;

  public Tracker(String title, String host, int port) {
    this.title = title;
    try {
      Socket socket = new Socket();
      socket.connect(new InetSocketAddress(host, port));
      trackWriter = new PrintWriter(socket.getOutputStream());
      trackWriter.println("Hello, I am " + title);
      trackWriter.flush();
    } catch (Exception ex) {
      ex.printStackTrace();
      System.out.println("Fallback to syserr");
      trackWriter = new PrintWriter(System.err);
    }
  }

  public void writeInfo(String functionName, KeyValuePair... metadata) {
    trackWriter.println("--------" + title + "---------");
    trackWriter.println("FUNCTION: " + functionName);
    if (metadata != null && metadata.length > 0) {
      for (KeyValuePair metadatum : metadata) {
        trackWriter.println("   " + metadatum.key + ": " + metadatum.value);
      }
    }

    trackWriter.println();
    trackWriter.flush();
  }


  static Tracker.KeyValuePair kvp(String key, String value){
    return new Tracker.KeyValuePair(key, value);
  }
}
