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
import java.util.Iterator;

import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONObject;

import edu.mit.scratch.exceptions.ScratchProjectException;

//@SuppressWarnings("unused")
public class ScratchProject {
    protected int ID = 0;

    protected String love_count = "";
    protected String resource_uri = "";
    protected String thumbnail = "";
    protected String title = "";
    protected String view_count = "";
    protected String creator = "";
    protected String share_date = "";
    protected String description = "";
    protected String favorite_count = "";

    public ScratchProject(final int id) {
        this.ID = id;
    }

    public boolean comment(ScratchSession session, String comment) throws ScratchProjectException {
        final RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT)
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
                .setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64)"
                        + " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.111 Safari/" + "537.36")
                .setDefaultCookieStore(cookieStore).build();
        CloseableHttpResponse resp;
        
        JSONObject obj = new JSONObject();
        obj.put("content", comment);
        obj.put("parent_id", "");
        obj.put("commentee_id", "");
        String strData = obj.toString();
        
        HttpUriRequest update = null;
        try {
            update = RequestBuilder.post()
                    .setUri("https://scratch.mit.edu/site-api/comments/project/" + this.getProjectID() + "/add/")
                    .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .addHeader("Referer", "https://scratch.mit.edu/projects/" + this.getProjectID() + "/")
                    .addHeader("Origin", "https://scratch.mit.edu/").addHeader("Accept-Encoding", "gzip, deflate, sdch")
                    .addHeader("Accept-Language", "en-US,en;q=0.8").addHeader("Content-Type", "application/json")
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .addHeader("Cookie",
                            "scratchsessionsid=" + session.getSessionID() + "; scratchcsrftoken=" + session.getCSRFToken())
                    .addHeader("X-CSRFToken", session.getCSRFToken())
                    .setEntity(new StringEntity(strData)).build();
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        try {
            resp = httpClient.execute(update);
            System.out.println("cmntadd:" + resp.getStatusLine());
            final BufferedReader rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));

            final StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null)
                result.append(line);
            System.out.println("cmtline:" + result.toString());
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

    public void setLoved(ScratchSession session, boolean loved) throws ScratchProjectException {
        final RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT)
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
                .setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64)"
                        + " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.111 Safari/" + "537.36")
                .setDefaultCookieStore(cookieStore).build();
        CloseableHttpResponse resp;

        final HttpUriRequest update = RequestBuilder.put()
                .setUri("https://scratch.mit.edu/site-api/users/lovers/" + this.getProjectID() + "/" + (loved ? "add" : "remove") + "/?usernames=" + session.getUsername())
                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("DNT", "1")
                .addHeader("Referer", "https://scratch.mit.edu/projects/" + this.getProjectID() + "/")
                .addHeader("Origin", "https://scratch.mit.edu/").addHeader("Accept-Encoding", "gzip, deflate, sdch")
                .addHeader("Accept-Language", "en-US,en;q=0.8").addHeader("Content-Type", "application/json")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("Cookie",
                        "scratchsessionsid=" + session.getSessionID() + "; scratchcsrftoken=" + session.getCSRFToken())
                .addHeader("X-CSRFToken", session.getCSRFToken()).build();
        try {
            resp = httpClient.execute(update);
            if(resp.getStatusLine().getStatusCode() != 200)
                throw new ScratchProjectException();
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
    }
    
    public void setFavorited(ScratchSession session, boolean favorited) throws ScratchProjectException {
        final RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT)
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
                .setUserAgent(Scratch.USER_AGENT)
                .setDefaultCookieStore(cookieStore).build();
        CloseableHttpResponse resp;

        final HttpUriRequest update = RequestBuilder.put()
                .setUri("https://scratch.mit.edu/site-api/users/favoriters/" + this.getProjectID() + "/" + (favorited ? "add" : "remove") + "/?usernames=" + session.getUsername())
                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("DNT", "1")
                .addHeader("Referer", "https://scratch.mit.edu/projects/" + this.getProjectID() + "/")
                .addHeader("Origin", "https://scratch.mit.edu/").addHeader("Accept-Encoding", "gzip, deflate, sdch")
                .addHeader("Accept-Language", "en-US,en;q=0.8").addHeader("Content-Type", "application/json")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("Cookie",
                        "scratchsessionsid=" + session.getSessionID() + "; scratchcsrftoken=" + session.getCSRFToken())
                .addHeader("X-CSRFToken", session.getCSRFToken()).build();
        try {
            resp = httpClient.execute(update);
            if(resp.getStatusLine().getStatusCode() != 200)
                throw new ScratchProjectException();
            final BufferedReader rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));

            final StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null)
                result.append(line);
        } catch (final IOException e) {
            e.printStackTrace();
            throw new ScratchProjectException();
        }
    }
    
    public ScratchUser getCreator() {
        return new ScratchUser(this.creator);
    }

    public String getDescription() {
        return this.description;
    }

    public int getFavoriteCount() {
        return Integer.parseInt(this.favorite_count);
    }

    public int getLoveCount() {
        return Integer.parseInt(this.love_count);
    }

    public int getProjectID() {
        return this.ID;
    }

    public ScratchProjectManager getProjectManager(ScratchSession session) {
        return new ScratchProjectManager(session, this.getProjectID());
    }

    public String getResourceURL() {
        return this.resource_uri;
    }

    public String getShareDate() {
        return this.share_date;
    }

    public String getThumbnailURL() {
        return this.thumbnail;
    }

    public String getTitle() {
        return this.title;
    }

    public int getViewCount() {
        return Integer.parseInt(this.view_count);
    }

    public ScratchProject update() throws ScratchProjectException {
        try {
            final RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT)
                    .build();

            final CookieStore cookieStore = new BasicCookieStore();
            final BasicClientCookie lang = new BasicClientCookie("scratchlanguage", "en");
            lang.setDomain(".scratch.mit.edu");
            lang.setPath("/");
            cookieStore.addCookie(lang);

            final CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig)
                    .setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64)"
                            + " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.111 Safari/" + "537.36")
                    .setDefaultCookieStore(cookieStore).build();
            CloseableHttpResponse resp;

            final HttpUriRequest update = RequestBuilder.get()
                    .setUri("https://scratch.mit.edu/api/v1/project/" + this.getProjectID() + "/?format=json")
                    .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .addHeader("Referer", "https://scratch.mit.edu").addHeader("Origin", "https://scratch.mit.edu")
                    .addHeader("Accept-Encoding", "gzip, deflate, sdch").addHeader("Accept-Language", "en-US,en;q=0.8")
                    .addHeader("Content-Type", "application/json").addHeader("X-Requested-With", "XMLHttpRequest")
                    .build();
            try {
                resp = httpClient.execute(update);
                System.out.println(resp.getStatusLine().toString());
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
            System.out.println("projdata:" + result.toString());
            final JSONObject jsonOBJ = new JSONObject(result.toString().trim());

            final Iterator<?> keys = jsonOBJ.keys();

            while (keys.hasNext()) {
                final String key = "" + keys.next();
                final Object o = jsonOBJ.get(key);
                if (o instanceof JSONObject)
                    this.creator = "" + ((JSONObject) o).get("username");
                else {
                    final String val = "" + o;

                    switch (key) {
                    case "creator":
                        this.creator = val;
                        break;
                    case "datetime_shared":
                        this.share_date = val;
                        break;
                    case "description":
                        this.description = val;
                        break;
                    case "favorite_count":
                        this.favorite_count = val;
                        break;
                    case "id":
                        this.ID = Integer.parseInt(val);
                        break;
                    case "love_count":
                        this.love_count = val;
                        break;
                    case "resource_uri":
                        this.resource_uri = val;
                        break;
                    case "thumbnail":
                        this.thumbnail = val;
                        break;
                    case "title":
                        this.title = val;
                        break;
                    case "view_count":
                        this.view_count = val;
                        break;
                    default:
                        System.out.println("Missing reference:" + key);
                        break;
                    }
                }
            }
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new ScratchProjectException();
        } catch (final IOException e) {
            e.printStackTrace();
            throw new ScratchProjectException();
        }

        return this;
    }
}
