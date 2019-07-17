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

import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 *
 * @author Muhammet Yildiz
 */
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
