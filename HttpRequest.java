
/**
 * HttpRequest - HTTP request container and parser
 *
 * $Id: HttpRequest.java,v 1.2 2003/11/26 18:11:53 kangasha Exp $
 *
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class HttpRequest {
  /** Help variables */
  final static String CRLF = "\r\n";
  final static int HTTP_PORT = 80;
  final static int BUF_SIZE = 8192;
  final static int MAX_OBJECT_SIZE = 100000;

  /** Store the request parameters */
  String method;
  String URI;
  String version;
  String headers = "";
  char[] postData  = new char[MAX_OBJECT_SIZE];
  int    postDataLength = -1;

  /** Server and port */
  private String host;
  private int port;

  /** Create HttpRequest by reading it from the client socket */
  public HttpRequest(BufferedReader from) {
    String firstLine = "";
    String[] tmp;
    try {
      firstLine = from.readLine();
      if(firstLine != null ){
        //        Request-Line   = Method SP Request-URI SP HTTP-Version CRLF
        tmp = firstLine.split(" ");
        method = tmp[0]; /* Fill in */
        URI = tmp[1]; /* Fill in */
        version = tmp[2];/* Fill in */
      }
      else{
        return;
      }

    } catch (IOException e) {
      System.out.println("Error reading request line: " + e);
    }
    try {
      String line = from.readLine();
      while (line.length() != 0) {
        headers += line + CRLF;
        /* We need to find host header to know which server to
         * contact in case the request URI is not complete. */
        if (line.startsWith("Host:")) {
          tmp = line.split(" ");
          if (tmp[1].indexOf(':') > 0) {
            String[] tmp2 = tmp[1].split(":");
            host = tmp2[0];
            port = Integer.parseInt(tmp2[1]);
          } else {
            host = tmp[1];
            port = HTTP_PORT;
          }
        }
        if (method.equals("POST")) {
          if (line.startsWith("Content-Length:")) {
            tmp = line.split(" ");
            postDataLength = Integer.parseInt(tmp[1]);
            System.out.println("POST: " + line + " length: " + postDataLength);
            char buf[] = new char[BUF_SIZE];
            int res = from.read( buf, 0, postDataLength ); /* Fill in */
            if (res == -1) {
              break;
            }
            /* Copy the bytes into body. Make sure we don't exceed
             * the maximum object size. */
            for (int i = 0;
                i < res;
                i++) {
              postData[i] = buf[i];
                }
          }
        }
        line = from.readLine();
      }
    } catch (IOException e) {
      System.out.println("Error reading from socket: " + e);
      return;
    }
  }

  /** Return host for which this request is intended */
  public String getHost() {
    return host;
  }

  /** Return port for server */
  public int getPort() {
    return port;
  }

  /** Return URI for server */
  public String getURI() {
    return URI;
  }

  /**
   * Convert request into a string for easy re-sending.
   */
  public String toString() {
    String req = "";
    req = method + " " + URI + " " + version + CRLF;
    req += headers;
    if (method.equals("POST")){
      req += "Content-Length: " + postDataLength + CRLF;
      req += postData + CRLF;
    }
    /* This proxy does not support persistent connections */
    req += "Connection: close" + CRLF;
    req += CRLF;

    return req;
  }
}
