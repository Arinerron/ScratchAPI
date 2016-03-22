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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.mit.scratch.exceptions.ScratchProjectException;

@Deprecated
public class ScratchCloudManager {

    private ScratchCloudSession session = null;
    private int id = 0;
    
    @Deprecated
    public ScratchCloudManager(ScratchCloudSession session, int id) {
        this.session = session;
        this.id = id;
    }
    /*
    @Deprecated
    private String consume(CloseableHttpResponse r)
            throws IllegalStateException, IOException {
        InputStream in = r.getEntity().getContent();
        StringBuffer str = new StringBuffer();
        byte[] b = new byte[64];
        int len;
        while ((len = in.read(b)) != -1)
            str.append(new String(b, 0, len));
        in.close();
        return str.toString();
    }
    
    @Deprecated
    public String get(String key) throws ScratchProjectException {
        try {
            @SuppressWarnings("deprecation")
            final RequestConfig globalConfig = RequestConfig.custom()
            .setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY).build();

            final CookieStore cookieStore = new BasicCookieStore();
            final BasicClientCookie lang = new BasicClientCookie(
                    "scratchlanguage", "en");
            lang.setDomain(".scratch.mit.edu");
            lang.setPath("/");
            cookieStore.addCookie(lang);

            final CloseableHttpClient httpClient = HttpClients
                    .custom()
                    .setDefaultRequestConfig(globalConfig)
                    .setUserAgent(
                            "Mozilla/5.0 (Windows NT 6.1; WOW64)"
                                    + " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.111 Safari/"
                                    + "537.36")
                                    .setDefaultCookieStore(cookieStore).build();
            CloseableHttpResponse resp;

            final HttpUriRequest update = RequestBuilder
                    .get()
                    .setUri("https://scratch.mit.edu/varserver/" + this.id)
                    .addHeader("Accept",
                            "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*//*;q=0.8")
                            .addHeader("Referer", "https://scratch.mit.edu")
                            .addHeader("Origin", "https://scratch.mit.edu")
                            .addHeader("Accept-Encoding", "gzip, deflate, sdch")
                            .addHeader("Accept-Language", "en-US,en;q=0.8")
                            .addHeader("Content-Type", "application/json")
                            .addHeader("X-Requested-With", "XMLHttpRequest").build();
            try {
                resp = httpClient.execute(update);
                System.out.println("getvar:" + resp.getStatusLine().toString());
            } catch (final IOException e) {
                e.printStackTrace();
                throw new ScratchProjectException();
            }

            BufferedReader rd;
            try {
                rd = new BufferedReader(new InputStreamReader(resp.getEntity()
                        .getContent()));
            } catch (UnsupportedOperationException | IOException e) {
                e.printStackTrace();
                throw new ScratchProjectException();
            }

            final StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null)
                result.append(line);
            final JSONObject jsonOBJ = new JSONObject(result.toString().trim());

            final Iterator<?> keys = ((JSONArray) jsonOBJ.get("variables"))
                    .iterator();

            while (keys.hasNext()) {
                JSONObject o = new JSONObject(StringEscapeUtils.unescapeJson(""
                        + keys.next()));
                /*
                 * final String key = "" + keys.next(); final JSONObject o =
                 * (JSONObject)jsonOBJ.get(key);
                 */
                //**String k = o.get("name") + "";
            /*    if (k.equals(key))
                    return o.get("value") + "";
            }
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new ScratchProjectException();
        } catch (final IOException e) {
            e.printStackTrace();
            throw new ScratchProjectException();
        }

        return null;
    }
    
    public List<String> getVariables() throws ScratchProjectException {
        List<String> list = new ArrayList<>();

        try {
            @SuppressWarnings("deprecation")
            final RequestConfig globalConfig = RequestConfig.custom()
            .setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY).build();

            final CookieStore cookieStore = new BasicCookieStore();
            final BasicClientCookie lang = new BasicClientCookie(
                    "scratchlanguage", "en");
            lang.setDomain(".scratch.mit.edu");
            lang.setPath("/");
            cookieStore.addCookie(lang);

            final CloseableHttpClient httpClient = HttpClients
                    .custom()
                    .setDefaultRequestConfig(globalConfig)
                    .setUserAgent(
                            "Mozilla/5.0 (Windows NT 6.1; WOW64)"
                                    + " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.111 Safari/"
                                    + "537.36")
                                    .setDefaultCookieStore(cookieStore).build();
            CloseableHttpResponse resp;

            final HttpUriRequest update = RequestBuilder
                    .get()
                    .setUri("https://scratch.mit.edu/varserver/" + this.id)
                    .addHeader("Accept",
                            "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*//*;q=0.8")
                            .addHeader("Referer", "https://scratch.mit.edu")
                            .addHeader("Origin", "https://scratch.mit.edu")
                            .addHeader("Accept-Encoding", "gzip, deflate, sdch")
                            .addHeader("Accept-Language", "en-US,en;q=0.8")
                            .addHeader("Content-Type", "application/json")
                            .addHeader("X-Requested-With", "XMLHttpRequest").build();
            try {
                resp = httpClient.execute(update);
            } catch (final IOException e) {
                e.printStackTrace();
                throw new ScratchProjectException();
            }

            BufferedReader rd;
            try {
                rd = new BufferedReader(new InputStreamReader(resp.getEntity()
                        .getContent()));
            } catch (UnsupportedOperationException | IOException e) {
                e.printStackTrace();
                throw new ScratchProjectException();
            }

            final StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null)
                result.append(line);
            final JSONObject jsonOBJ = new JSONObject(result.toString().trim());

            final Iterator<?> keys = ((JSONArray) jsonOBJ.get("variables"))
                    .iterator();

            while (keys.hasNext()) {
                JSONObject o = new JSONObject(StringEscapeUtils.unescapeJson(""
                        + keys.next()));
                /*
                 * final String key = "" + keys.next(); final JSONObject o =
                 * (JSONObject)jsonOBJ.get(key);
                 *//*
                String k = o.get("name") + "";
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

    @Deprecated
    public void set(String key, String value) throws ScratchProjectException {
        try {
            @SuppressWarnings("deprecation")
            final RequestConfig globalConfig = RequestConfig.custom()
            .setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY).build();

			final CookieStore cookieStore = new BasicCookieStore();
			final BasicClientCookie lang = new BasicClientCookie("scratchlanguage", "en");
			final BasicClientCookie sessid = new BasicClientCookie("scratchsessionsid", this.session.getSessionID());
			final BasicClientCookie token = new BasicClientCookie("scratchcsrftoken", this.session.getCSRFToken());
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

            final CloseableHttpClient httpClient = HttpClients
                    .custom()
                    .setDefaultRequestConfig(globalConfig)
                    .setUserAgent(
                            "Mozilla/5.0 (Windows NT 6.1; WOW64)"
                                    + " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.111 Safari/"
                                    + "537.36")
                                    .setDefaultCookieStore(cookieStore).build();
            CloseableHttpResponse resp;

            HttpUriRequest project = RequestBuilder.get()
                    .setUri("https://scratch.mit.edu/projects/" + this.id + "/")
                    .addHeader("Accept", "text/html")
                    .addHeader("Referer", "https://scratch.mit.edu").build();
            resp = httpClient.execute(project);
            String projectStr = this.consume(resp);
            Pattern p = Pattern.compile("cloudToken: '([a-zA-Z0-9\\-]+)'");
            Matcher m = p.matcher(projectStr);
            m.find();
            String cloudToken = m.group(1);

            HttpUriRequest unfreeze = RequestBuilder
                    // 403?
                    .get()
                    .setUri("https://scratch.mit.edu/projects/unfreeze-cloud-data/")
                    .addHeader(
                            "Referer",
                            ": https://scratch.mit.edu/projects/" + this.id
                            + "/")
                            .addHeader("Origin", "https://scratch.mit.edu")
                            .addHeader("Content-Type",
                                    "application/x-www-form-urlencoded")
                                    .addHeader("X-Requested-With", "XMLHttpRequest")
                                    .addHeader("X-CSRFToken", this.session.getCSRFToken())
                                    .addHeader("Accept", "*//*")
                                    .setEntity(new StringEntity("project_id=" + this.id))
                                    .build();

            resp = httpClient.execute(unfreeze);
            System.out.println("cloudfreeze:" + resp.getStatusLine());
            resp.close();

            JSONObject cloudObj = new JSONObject();
            cloudObj.put("user", this.session.getUsername());
            cloudObj.put("token", cloudToken);
            cloudObj.put("token2", DigestUtils.md5Hex(cloudToken));
            cloudObj.put("name", key);
            cloudObj.put("value", value);
            cloudObj.put("project_id", this.id);
            cloudObj.put("method", "set");
            HttpUriRequest cloud = RequestBuilder
                    .post()
                    .setUri("https://scratch.mit.edu/varserver")
                    .addHeader(
                            "Referer",
                            "https://cdn.scratch.mit.edu/scratchr2/"
                                    + "static/__ea3e2384bc6d0e5ec08c3b21be943f8d__/Scratch.swf")
                                    .addHeader("Content-Type",
                                            "application/x-www-form-urlencoded")
                                            .setEntity(
                                                    new ByteArrayEntity(cloudObj.toString().getBytes()))
                                                    .build();
            resp = httpClient.execute(cloud);
            System.out.println("cloudset:" + resp.getStatusLine());
            resp.close();

        } catch (IOException e) {
            e.printStackTrace();
            throw new ScratchProjectException();
        }*/
   // }
}
