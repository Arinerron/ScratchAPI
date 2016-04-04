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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.mit.scratch.exceptions.ScratchProjectException;
import edu.mit.scratch.exceptions.ScratchUserException;

public class ScratchUser {
    private String username = null;
    
    public ScratchUser(final String username) {
        this.username = username;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public void setFollowing(final ScratchSession session, final boolean following) throws ScratchUserException {
        try {
            final RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY)
                    .build();
            
            final BasicCookieStore cookieStore = new BasicCookieStore();
            final BasicClientCookie lang = new BasicClientCookie("scratchlanguage", "en");
            final BasicClientCookie sessionid = new BasicClientCookie("scratchsessionsid", session.getSessionID());
            final BasicClientCookie token = new BasicClientCookie("scratchcsrftoken", session.getCSRFToken());
            final BasicClientCookie debug = new BasicClientCookie("DEBUG", "true");
            lang.setDomain(".scratch.mit.edu");
            lang.setPath("/");
            sessionid.setDomain(".scratch.mit.edu");
            sessionid.setPath("/");
            token.setDomain(".scratch.mit.edu");
            token.setPath("/");
            debug.setDomain(".scratch.mit.edu");
            debug.setPath("/");
            cookieStore.addCookie(lang);
            cookieStore.addCookie(sessionid);
            cookieStore.addCookie(token);
            cookieStore.addCookie(debug);
            
            final CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig)
                    .setUserAgent(Scratch.USER_AGENT).setDefaultCookieStore(cookieStore).build();
            CloseableHttpResponse resp;
            
            final HttpUriRequest update = RequestBuilder.put()
                    .setUri("https://scratch.mit.edu/site-api/users/followers/" + this.getUsername() + "/"
                            + ((following) ? "add" : "remove") + "/?usernames=" + session.getUsername())
                    .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                    .addHeader("Referer", "https://scratch.mit.edu/users/" + this.getUsername() + "/")
                    .addHeader("Origin", "https://scratch.mit.edu").addHeader("Accept-Encoding", "gzip, deflate, sdch")
                    .addHeader("Accept-Language", "en-US,en;q=0.8").addHeader("Content-Type", "application/json")
                    .addHeader("Content-Encoding", "gzip")
                    .addHeader("X-Requested-With", "XMLHttpRequest").addHeader("Cookie", "scratchsessionsid="
                            + session.getSessionID() + "; scratchcsrftoken=" + session.getCSRFToken())
                    .addHeader("X-CSRFToken", session.getCSRFToken()).build();
            try {
                resp = httpClient.execute(update);
                if (resp.getStatusLine().getStatusCode() != 200)
                    throw new ScratchUserException();
            } catch (final Exception e) {
                e.printStackTrace();
                throw new ScratchUserException();
            }
            
            BufferedReader rd;
            try {
                rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
            } catch (UnsupportedOperationException | IOException e) {
                e.printStackTrace();
                throw new ScratchUserException();
            }
            
            final StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null)
                result.append(line);
            // result = your json data
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new ScratchUserException();
        } catch (final IOException e) {
            e.printStackTrace();
            throw new ScratchUserException();
        }
    }
    
    public boolean comment(final ScratchSession session, final String comment) throws ScratchProjectException {
        final RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY)
                .build();
        
        final CookieStore cookieStore = new BasicCookieStore();
        final BasicClientCookie lang = new BasicClientCookie("scratchlanguage", "en");
        final BasicClientCookie sessid = new BasicClientCookie("scratchsessionsid", session.getSessionID());
        final BasicClientCookie token = new BasicClientCookie("scratchcsrftoken", session.getCSRFToken());
        final BasicClientCookie debug = new BasicClientCookie("DEBUG", "true");
        lang.setDomain(".scratch.mit.edu");
        lang.setPath("/");
        sessid.setDomain(".scratch.mit.edu");
        sessid.setPath("/");
        token.setDomain(".scratch.mit.edu");
        token.setPath("/");
        debug.setDomain(".scratch.mit.edu");
        debug.setPath("/");
        cookieStore.addCookie(lang);
        cookieStore.addCookie(sessid);
        cookieStore.addCookie(token);
        cookieStore.addCookie(debug);
        
        final CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig)
                .setUserAgent(Scratch.USER_AGENT).setDefaultCookieStore(cookieStore).build();
        CloseableHttpResponse resp;
        
        final JSONObject obj = new JSONObject();
        obj.put("content", comment);
        obj.put("parent_id", "");
        obj.put("commentee_id", "");
        final String strData = obj.toString();
        
        HttpUriRequest update = null;
        try {
            update = RequestBuilder.post()
                    .setUri("https://scratch.mit.edu/site-api/comments/user/" + this.getUsername() + "/add/")
                    .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .addHeader("Referer", "https://scratch.mit.edu/users/" + this.getUsername() + "/")
                    .addHeader("Origin", "https://scratch.mit.edu/").addHeader("Accept-Encoding", "gzip, deflate, sdch")
                    .addHeader("Accept-Language", "en-US,en;q=0.8").addHeader("Content-Type", "application/json")
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .addHeader("Cookie",
                            "scratchsessionsid=" + session.getSessionID() + "; scratchcsrftoken="
                                    + session.getCSRFToken())
                    .addHeader("X-CSRFToken", session.getCSRFToken()).setEntity(new StringEntity(strData)).build();
        } catch (final UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        try {
            resp = httpClient.execute(update);
            final BufferedReader rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
            
            final StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null)
                result.append(line);
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
        
        return false;
    }
    
    public int getMessageCount() throws ScratchUserException {
        try {
            final RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY)
                    .build();
            
            final CookieStore cookieStore = new BasicCookieStore();
            final BasicClientCookie lang = new BasicClientCookie("scratchlanguage", "en");
            final BasicClientCookie debug = new BasicClientCookie("DEBUG", "true");
            debug.setDomain(".scratch.mit.edu");
            debug.setPath("/");
            lang.setPath("/");
            lang.setDomain(".scratch.mit.edu");
            cookieStore.addCookie(lang);
            cookieStore.addCookie(debug);
            
            final CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig)
                    .setUserAgent(Scratch.USER_AGENT).setDefaultCookieStore(cookieStore).build();
            CloseableHttpResponse resp;
            
            final HttpUriRequest update = RequestBuilder.get()
                    .setUri("https://api.scratch.mit.edu/users/" + this.getUsername() + "/messages/count")
                    .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .addHeader("Referer", "https://scratch.mit.edu/users/" + this.getUsername() + "/")
                    .addHeader("Origin", "https://scratch.mit.edu").addHeader("Accept-Encoding", "gzip, deflate, sdch")
                    .addHeader("Accept-Language", "en-US,en;q=0.8").addHeader("Content-Type", "application/json")
                    .addHeader("X-Requested-With", "XMLHttpRequest").build();
            try {
                resp = httpClient.execute(update);
            } catch (final IOException e) {
                e.printStackTrace();
                throw new ScratchUserException();
            }
            
            BufferedReader rd;
            try {
                rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
            } catch (UnsupportedOperationException | IOException e) {
                e.printStackTrace();
                throw new ScratchUserException();
            }
            
            final StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null)
                result.append(line);
            final JSONObject jsonOBJ2 = new JSONObject(result.toString().trim());
            
            return jsonOBJ2.getInt("count");
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new ScratchUserException();
        } catch (final Exception e) {
            e.printStackTrace();
            throw new ScratchUserException();
        }
    }
    
    public ScratchUser update() {
        return this;
    }
    
    @Deprecated
    public List<ScratchProject> getProjects() throws ScratchUserException { // DEPRECATED
        final List<ScratchProject> ids = new ArrayList<>();
        
        try {
            final RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY)
                    .build();
            
            final CookieStore cookieStore = new BasicCookieStore();
            final BasicClientCookie lang = new BasicClientCookie("scratchlanguage", "en");
            final BasicClientCookie debug = new BasicClientCookie("DEBUG", "true");
            debug.setDomain(".scratch.mit.edu");
            debug.setPath("/");
            lang.setPath("/");
            lang.setDomain(".scratch.mit.edu");
            cookieStore.addCookie(lang);
            cookieStore.addCookie(debug);
            
            final CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig)
                    .setUserAgent(Scratch.USER_AGENT).setDefaultCookieStore(cookieStore).build();
            CloseableHttpResponse resp;
            
            final HttpUriRequest update = RequestBuilder.get()
                    .setUri("https://api.scratch.mit.edu/users/" + this.getUsername() + "/projects")
                    .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .addHeader("Referer", "https://scratch.mit.edu/users/" + this.getUsername() + "/")
                    .addHeader("Origin", "https://scratch.mit.edu").addHeader("Accept-Encoding", "gzip, deflate, sdch")
                    .addHeader("Accept-Language", "en-US,en;q=0.8").addHeader("Content-Type", "application/json")
                    .addHeader("X-Requested-With", "XMLHttpRequest").build();
            try {
                resp = httpClient.execute(update);
            } catch (final IOException e) {
                e.printStackTrace();
                throw new ScratchUserException();
            }
            
            BufferedReader rd;
            try {
                rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
            } catch (UnsupportedOperationException | IOException e) {
                e.printStackTrace();
                throw new ScratchUserException();
            }
            
            final StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null)
                result.append(line);
            final JSONArray jsonOBJ2 = new JSONArray(result.toString().trim());
            
            for (int i = 0; i < jsonOBJ2.length(); i++) {
                final JSONObject jsonOBJ = jsonOBJ2.getJSONObject(i);
                
                final Iterator<?> keys = jsonOBJ.keys();
                
                while (keys.hasNext()) {
                    final String key = "" + keys.next();
                    final Object o = jsonOBJ.get(key);
                    final String val = "" + o;
                    
                    if (key.equalsIgnoreCase("id"))
                        ids.add(new ScratchProject(Integer.parseInt(val)));
                }
            }
            
            return ids;
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new ScratchUserException();
        } catch (final Exception e) {
            e.printStackTrace();
            throw new ScratchUserException();
        }
    }
    
    public List<ScratchProject> getFavoriteProjects(final int limit, final int offset) throws ScratchUserException {
        final List<ScratchProject> ids = new ArrayList<>();
        
        try {
            final RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY)
                    .build();
            
            final CookieStore cookieStore = new BasicCookieStore();
            final BasicClientCookie lang = new BasicClientCookie("scratchlanguage", "en");
            final BasicClientCookie debug = new BasicClientCookie("DEBUG", "true");
            debug.setDomain(".scratch.mit.edu");
            debug.setPath("/");
            lang.setPath("/");
            lang.setDomain(".scratch.mit.edu");
            cookieStore.addCookie(lang);
            cookieStore.addCookie(debug);
            
            final CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig)
                    .setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64)"
                            + " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.111 Safari/" + "537.36")
                    .setDefaultCookieStore(cookieStore).build();
            CloseableHttpResponse resp;
            
            final HttpUriRequest update = RequestBuilder.get()
                    .setUri("https://api.scratch.mit.edu/users/" + this.getUsername() + "/favorites?limit=" + limit
                            + "&offset=" + offset)
                    .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .addHeader("Referer", "https://scratch.mit.edu/users/" + this.getUsername() + "/")
                    .addHeader("Origin", "https://scratch.mit.edu").addHeader("Accept-Encoding", "gzip, deflate, sdch")
                    .addHeader("Accept-Language", "en-US,en;q=0.8").addHeader("Content-Type", "application/json")
                    .addHeader("X-Requested-With", "XMLHttpRequest").build();
            try {
                resp = httpClient.execute(update);
            } catch (final IOException e) {
                e.printStackTrace();
                throw new ScratchUserException();
            }
            
            BufferedReader rd;
            try {
                rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
            } catch (UnsupportedOperationException | IOException e) {
                e.printStackTrace();
                throw new ScratchUserException();
            }
            
            final StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null)
                result.append(line);
            final JSONArray jsonOBJ2 = new JSONArray(result.toString().trim());
            
            for (int i = 0; i < jsonOBJ2.length(); i++) {
                final JSONObject jsonOBJ = jsonOBJ2.getJSONObject(i);
                
                final Iterator<?> keys = jsonOBJ.keys();
                
                while (keys.hasNext()) {
                    final String key = "" + keys.next();
                    final Object o = jsonOBJ.get(key);
                    final String val = "" + o;
                    
                    if (key.equalsIgnoreCase("id"))
                        ids.add(new ScratchProject(Integer.parseInt(val)));
                }
            }
            
            return ids;
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new ScratchUserException();
        } catch (final Exception e) {
            e.printStackTrace();
            throw new ScratchUserException();
        }
    }
}
