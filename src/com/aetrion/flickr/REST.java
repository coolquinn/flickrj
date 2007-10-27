/*
 * Copyright (c) 2005 Aetrion LLC.
 */
package com.aetrion.flickr;

import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.auth.AuthUtilities;
import com.aetrion.flickr.util.DebugInputStream;
import com.aetrion.flickr.util.DebugOutputStream;
import com.aetrion.flickr.util.IOUtilities;
import com.aetrion.flickr.util.UrlUtilities;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;

/**
 * Transport implementation using the REST interface.
 *
 * @author Anthony Eden
 * @version $Id: REST.java,v 1.19 2007/10/27 22:54:33 x-mago Exp $
 */
public class REST extends Transport {

    private static final String UTF8 = "UTF-8";
    public static final String PATH = "/services/rest/";

    private DocumentBuilder builder;

    /**
     * Construct a new REST transport instance.
     *
     * @throws ParserConfigurationException
     */
    public REST() throws ParserConfigurationException {
        setTransportType(REST);
        setPath(PATH);
        setResponseClass(RESTResponse.class);
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builder = builderFactory.newDocumentBuilder();
    }

    /**
     * Construct a new REST transport instance using the specified host endpoint.
     *
     * @param host The host endpoint
     * @throws ParserConfigurationException
     */
    public REST(String host) throws ParserConfigurationException {
        this();
        setHost(host);
    }

    /**
     * Construct a new REST transport instance using the specified host and port endpoint.
     *
     * @param host The host endpoint
     * @param port The port
     * @throws ParserConfigurationException
     */
    public REST(String host, int port) throws ParserConfigurationException {
        this();
        setHost(host);
        setPort(port);
    }

    /**
     * Invoke an HTTP GET request on a remote host.  You must close the InputStream after you are done with.
     *
     * @param path The request path
     * @param parameters The parameters (collection of Parameter objects)
     * @return The Response
     * @throws IOException
     * @throws SAXException
     */
    public Response get(String path, List parameters) throws IOException, SAXException {
        URL url = UrlUtilities.buildUrl(getHost(), getPort(), path, parameters);
        if (Flickr.debugRequest) System.out.println("GET: " + url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        InputStream in = null;
        try {
            if (Flickr.debugStream) {
                in = new DebugInputStream(conn.getInputStream(), System.out);
            } else {
                in = conn.getInputStream();
            }

            Document document = builder.parse(in);
            Response response = (Response) responseClass.newInstance();
            response.parse(document);
            return response;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e); // TODO: Replace with a better exception
        } catch (InstantiationException e) {
            throw new RuntimeException(e); // TODO: Replace with a better exception
        } finally {
            IOUtilities.close(in);
        }
    }

    /**
     * Invoke an HTTP POST request on a remote host.
     *
     * @param path The request path
     * @param parameters The parameters (collection of Parameter objects)
     * @param multipart Use multipart
     * @return The Response object
     * @throws IOException
     * @throws SAXException
     */
    public Response post(String path, List parameters, boolean multipart) throws IOException, SAXException {
        AuthUtilities.addAuthToken(parameters);

        RequestContext requestContext = RequestContext.getRequestContext();
        URL url = UrlUtilities.buildPostUrl(getHost(), getPort(), path);

        HttpURLConnection conn = null;
        try {
            String boundary = "---------------------------7d273f7a0d3";

            conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            if (multipart) {
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            }
            conn.connect();

            DataOutputStream out = null;
            try {
                if (Flickr.debugRequest) {
                    out = new DataOutputStream(new DebugOutputStream(conn.getOutputStream(), System.out));
                } else {
                    out = new DataOutputStream(conn.getOutputStream());
                }

                // construct the body
                if (multipart) {
                    out.writeBytes("--" + boundary + "\r\n");
                    Iterator iter = parameters.iterator();
                    while (iter.hasNext()) {
                        Parameter p = (Parameter) iter.next();
                        writeParam(p.getName(), p.getValue(), out, boundary);
                    }
                    Auth auth = requestContext.getAuth();
                    if (auth != null) {
                        writeParam(
                            "api_sig",
                            AuthUtilities.getMultipartSignature(parameters),
                            out,
                            boundary
                        );
                    }
                } else {
                    Iterator iter = parameters.iterator();
                    while (iter.hasNext()) {
                        Parameter p = (Parameter) iter.next();
                        out.writeBytes(p.getName());
                        out.writeBytes("=");
                        try {
                            out.writeBytes(
                                URLEncoder.encode(
                                    String.valueOf(p.getValue()),
                                    UTF8
                                )
                            );
                		} catch(UnsupportedEncodingException e) {
                		    // Should never happen, but just in case
                		}
                        if (iter.hasNext()) out.writeBytes("&");
                    }

                    Auth auth = requestContext.getAuth();
                    if (auth != null) {
                        out.writeBytes("&auth_token=");
                        out.writeBytes(auth.getToken());
                        out.writeBytes("&api_sig=");
                        out.writeBytes(AuthUtilities.getSignature(parameters));
                    }
                }
                out.flush();
            } finally {
                IOUtilities.close(out);
            }

            InputStream in = null;
            try {
                if (Flickr.debugStream) {
                    in = new DebugInputStream(conn.getInputStream(), System.out);
                } else {
                    in = conn.getInputStream();
                }
                Document document = builder.parse(in);
                Response response = (Response) responseClass.newInstance();
                response.parse(document);
                return response;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e); // TODO: Replace with a better exception
            } catch (InstantiationException e) {
                throw new RuntimeException(e); // TODO: Replace with a better exception
            } finally {
                IOUtilities.close(in);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void writeParam(String name, Object value, DataOutputStream out, String boundary)
            throws IOException {
        if (value instanceof InputStream) {
            out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"image.jpg\";\r\n");
            out.writeBytes("Content-Type: image/jpeg" + "\r\n\r\n");
            InputStream in = (InputStream) value;
            byte[] buf = new byte[512];
            int res = -1;
            while ((res = in.read(buf)) != -1) {
                out.write(buf);
            }
            out.writeBytes("\r\n" + "--" + boundary + "\r\n");
        } else if (value instanceof byte[]) {
            out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"image.jpg\";\r\n");
            out.writeBytes("Content-Type: image/jpeg" + "\r\n\r\n");
            out.write((byte[]) value);
            out.writeBytes("\r\n" + "--" + boundary + "\r\n");
        } else {
            out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
            out.writeBytes(String.valueOf(value));
            out.writeBytes("\r\n" + "--" + boundary + "\r\n");
        }
    }

}
