package edu.mit.scratch;

/*
 * 
 * +------+----------------+------+
 * |######|  [ScratchAPI]  |######|
 * +------+----------------+------+
 * 
 * Copyright (c) 2016 ScratchAPI Developers
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 * 
 * "ScratchAPI Developers" means anybody who contributed code to the
 * project.
 *
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.mit.scratch.exceptions.ScratchProjectException;

public class ScratchCloudSession {
    
    private ScratchSession session = null;
    private String cloudToken = null;
    private String hashToken = null;
    private int projectID = 0;
    private boolean running = true;
    private final List<ScratchCloudListener> listeners = new ArrayList<>();
    
    private PrintWriter out = null;
    private Socket socket = null;
    private BufferedReader in = null;
    private Thread thread = null;
    
    public ScratchCloudSession(final ScratchSession session, final String cloudToken, final int projectID)
            throws ScratchProjectException {
        this.session = session;
        this.cloudToken = cloudToken; // if !work, md5 cloudToken
        this.projectID = projectID;
        this.hashToken = this.MD5(this.getCloudToken());
        
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        
        try {
            socket = new Socket(Scratch.CLOUD_SERVER, Scratch.CLOUD_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            this.socket = socket;
            this.out = out;
            this.in = in;
            
            this.thread = new Thread(() -> {
                while (ScratchCloudSession.this.running) {
                    String line = "";
                    try {
                        line = ScratchCloudSession.this.in.readLine();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                    if (line != null)
                        if (!line.equals("null") && !line.equals("{}"))
                            ScratchCloudSession.this.handleLine(line);
                    
                }
            });
            this.thread.start();
            
            this.handshake();
        } catch (final Exception e) {
            e.printStackTrace();
            throw new ScratchProjectException();
        }
        
    }
    
    public void addCloudListener(final ScratchCloudListener listener) {
        this.listeners.add(listener);
    }
    
    public boolean removeCloudListener(final ScratchCloudListener listener) {
        return this.listeners.remove(listener);
    }
    
    public void close() {
        try {
            this.running = false;
            this.socket.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        this.out.close();
        try {
            this.in.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
    
    public void handleLine(final String line) {
        final JSONObject object = new JSONObject(line);
        
        final String method = object.getString("method");
        if ("set".equals(method)) { // inverted the .equals to prevent NullPointerExceptions
            final int projectID = object.getInt("project_id");
            final String name = object.getString("name");
            final String value = object.getString("value");
            
            for (final ScratchCloudListener listener : this.listeners)
                listener.onSet(projectID, name, value);
        } else { // TODO: Add `end`.
            System.out.println("Unknown method: " + method + "\nfor line: " + line);
        }
    }
    
    public ScratchSession getScratchSession() {
        return this.session;
    }
    
    public String getCloudToken() {
        return this.cloudToken;
    }
    
    public int getProjectID() {
        return this.projectID;
    }
    
    protected void handshake() {
        final String[] options = {};
        this.request("handshake", options);
    }
    
    public void set(final String key, final int value) { // Will this work if changed to String? :P
        final String[] options = { this.concat("name", key), this.concat("value", value + "") };
        this.request("set", options);
    }
    
    public void rename(final String name, final String newname) {
        final String[] options = { this.concat("name", name), this.concat("new_name", newname) };
        this.request("rename", options);
    }
    
    public void create(final String name, final int value) { // Maybe this will also work if String? :)
        final String[] options = { this.concat("name", name), this.concat("value", value + "") };
        this.request("create", options);
    }
    
    public void delete(final String name) {
        final String[] options = { this.concat("name", name) };
        this.request("delete", options);
    }
    
    private void request(final String method, final String[] options) {
        this.hashToken = this.MD5(this.hashToken);
        
        final JSONObject object = new JSONObject();
        object.put("token", this.getCloudToken());
        object.put("token2", this.hashToken);
        object.put("user", this.getScratchSession().getUsername());
        object.put("project_id", this.getProjectID());
        object.put("method", method);
        
        for (final String option : options) {
            final int index = option.indexOf(':');
            final String key = option.substring(0, index);
            final String val = option.substring(index + 1);
            
            object.put(key, (key.equals("value") ? Integer.parseInt(val) : val)); // later remove isInteger, replace with `val`
        } //
        
        final byte ptext[] = (object.toString() + "\r\n").getBytes(StandardCharsets.UTF_8); // that's an odd encoding... Change to `StandardCharsets.ISO_8859_1` if !work
        final String readyRequest = new String(ptext, StandardCharsets.UTF_8);
        
        this.out.print(readyRequest); // println if !work
        this.out.flush();
    }
    
    public static boolean isInteger(final String s) {
        return ScratchCloudSession.isInteger(s, 10);
    }
    
    public static boolean isInteger(final String s, final int radix) {
        if (s.isEmpty())
            return false;
        for (int i = 0; i < s.length(); i++) {
            if ((i == 0) && (s.charAt(i) == '-')) {
                if (s.length() == 1)
                    return false;
                else
                    continue;
            }
            if (Character.digit(s.charAt(i), radix) < 0)
                return false;
        }
        return true;
    }
    
    private String concat(final String first, final String second) {
        return first + ":" + second;
    }
    
    private String MD5(final String original) {
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(original.getBytes());
            final byte[] digest = md.digest();
            final StringBuffer sb = new StringBuffer();
            for (final byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return ""; // The code should NEVER reach here unless jdk is bugged.
    }
    
    public String get(final String key) throws ScratchProjectException {
        try {
            final RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();
            
            final CookieStore cookieStore = new BasicCookieStore();
            final BasicClientCookie lang = new BasicClientCookie("scratchlanguage", "en");
            lang.setDomain(".scratch.mit.edu");
            lang.setPath("/");
            cookieStore.addCookie(lang);
            
            final CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig)
                    .setUserAgent(Scratch.USER_AGENT).setDefaultCookieStore(cookieStore).build();
            CloseableHttpResponse resp;
            
            final HttpUriRequest update = RequestBuilder.get()
                    .setUri("https://scratch.mit.edu/varserver/" + this.getProjectID())
                    .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*//*;q=0.8")
                    .addHeader("Referer", "https://scratch.mit.edu").addHeader("Origin", "https://scratch.mit.edu")
                    .addHeader("Accept-Encoding", "gzip, deflate, sdch").addHeader("Accept-Language", "en-US,en;q=0.8")
                    .addHeader("Content-Type", "application/json").addHeader("X-Requested-With", "XMLHttpRequest")
                    .build();
            try {
                resp = httpClient.execute(update);
            } catch (final IOException e) {
                e.printStackTrace();
                throw new ScratchProjectException();
            }
            
            BufferedReader rd;
            try {
                rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
            } catch (UnsupportedOperationException | IOException e) {
                e.printStackTrace();
                throw new ScratchProjectException();
            }
            
            final StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null)
                result.append(line);
            final JSONObject jsonOBJ = new JSONObject(result.toString().trim());
            
            final Iterator<?> keys = ((JSONArray) jsonOBJ.get("variables")).iterator();
            
            while (keys.hasNext()) {
                final JSONObject o = new JSONObject(StringEscapeUtils.unescapeJson("" + keys.next()));
                final String k = o.get("name") + "";
                
                if (k.equals(key))
                    return o.get("value") + "";
            }
        } catch (final Exception e) {
            e.printStackTrace();
            throw new ScratchProjectException();
        }
        
        return null;
    }
    
    public List<String> getVariables() throws ScratchProjectException {
        final List<String> list = new ArrayList<>();
        
        try {
            final RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();
            
            final CookieStore cookieStore = new BasicCookieStore();
            final BasicClientCookie lang = new BasicClientCookie("scratchlanguage", "en");
            lang.setDomain(".scratch.mit.edu");
            lang.setPath("/");
            cookieStore.addCookie(lang);
            
            final CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig)
                    .setUserAgent(Scratch.USER_AGENT).setDefaultCookieStore(cookieStore).build();
            CloseableHttpResponse resp;
            
            final HttpUriRequest update = RequestBuilder.get()
                    .setUri("https://scratch.mit.edu/varserver/" + this.getProjectID())
                    .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*//*;q=0.8")
                    .addHeader("Referer", "https://scratch.mit.edu").addHeader("Origin", "https://scratch.mit.edu")
                    .addHeader("Accept-Encoding", "gzip, deflate, sdch").addHeader("Accept-Language", "en-US,en;q=0.8")
                    .addHeader("Content-Type", "application/json").addHeader("X-Requested-With", "XMLHttpRequest")
                    .build();
            try {
                resp = httpClient.execute(update);
            } catch (final IOException e) {
                e.printStackTrace();
                throw new ScratchProjectException();
            }
            
            BufferedReader rd;
            try {
                rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
            } catch (UnsupportedOperationException | IOException e) {
                e.printStackTrace();
                throw new ScratchProjectException();
            }
            
            final StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null)
                result.append(line);
            final JSONObject jsonOBJ = new JSONObject(result.toString().trim());
            
            final Iterator<?> keys = ((JSONArray) jsonOBJ.get("variables")).iterator();
            
            while (keys.hasNext()) {
                final JSONObject o = new JSONObject(StringEscapeUtils.unescapeJson("" + keys.next()));
                final String k = o.get("name") + "";
                list.add(k);
            }
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new ScratchProjectException();
        } catch (final IOException e) {
            e.printStackTrace();
            throw new ScratchProjectException();
        }
        
        return list;
    }
}
