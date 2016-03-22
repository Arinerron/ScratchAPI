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

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.json.JSONObject;

import edu.mit.scratch.exceptions.ScratchProjectException;
import edu.mit.scratch.exceptions.ScratchUserException;

public class ScratchSession {
    private String session_id, token, expires, username = null;

    public ScratchSession(final String sessionid, final String token,
            final String expires, final String username) {
        this.session_id = sessionid;
        this.token = token;
        this.expires = expires;
        this.username = username;
    }
    
    public void logout() throws ScratchUserException {
		final RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY)
				.build();

		final CookieStore cookieStore = new BasicCookieStore();
		final BasicClientCookie lang = new BasicClientCookie("scratchlanguage", "en");
		final BasicClientCookie sessid = new BasicClientCookie("scratchsessionsid", this.getSessionID());
		final BasicClientCookie token = new BasicClientCookie("scratchcsrftoken", this.getCSRFToken());
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
		
    	final JSONObject loginObj = new JSONObject();
		loginObj.put("csrftoken", this.getCSRFToken());
		try {
			final HttpUriRequest logout = RequestBuilder.post().setUri("https://scratch.mit.edu/accounts/logout/")
					.addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
					.addHeader("Referer", "https://scratch.mit.edu").addHeader("Origin", "https://scratch.mit.edu")
					.addHeader("Accept-Encoding", "gzip, deflate").addHeader("Accept-Language", "en-US,en;q=0.8")
					.addHeader("Content-Type", "application/json").addHeader("X-Requested-With", "XMLHttpRequest")
					.addHeader("X-CSRFToken", this.getCSRFToken()).setEntity(new StringEntity(loginObj.toString())).build();
				resp = httpClient.execute(logout);
		} catch (Exception e) {
			throw new ScratchUserException();
		}
		
    	this.session_id = null;
    	this.token = null;
    	this.expires = null;
    	this.username = null;
    }

    public String getCSRFToken() {
        return this.token;
    }

    public String getExpiration() {
        return this.expires;
    }

    public String getSessionID() {
        return this.session_id;
    }

    public ScratchUserManager getUserManager() throws ScratchUserException {
        return new ScratchUserManager(this);
    }

    public String getUsername() {
        return this.username;
    }
	
	public ScratchCloudSession getCloudSession(final int projectID) throws ScratchProjectException {
		final RequestConfig globalConfig = RequestConfig.custom()
	            .setCookieSpec(CookieSpecs.DEFAULT).build();

				final CookieStore cookieStore = new BasicCookieStore();
				final BasicClientCookie lang = new BasicClientCookie("scratchlanguage", "en");
				final BasicClientCookie sessid = new BasicClientCookie("scratchsessionsid", this.getSessionID());
				final BasicClientCookie token = new BasicClientCookie("scratchcsrftoken", this.getCSRFToken());
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
	                    .setUserAgent(Scratch.USER_AGENT)
	                                    .setDefaultCookieStore(cookieStore).build();
	            CloseableHttpResponse resp = null;

	            HttpUriRequest project = RequestBuilder.get()
	                    .setUri("https://scratch.mit.edu/projects/" + projectID + "/")
	                    .addHeader("Accept", "text/html")
	                    .addHeader("Referer", "https://scratch.mit.edu").build();
	            try {
					resp = httpClient.execute(project);
				} catch (IOException e) {
					e.printStackTrace();
					throw new ScratchProjectException();
				}
	            String projectStr = null;
				try {
					projectStr = Scratch.consume(resp);
				} catch (IllegalStateException | IOException e) {
					e.printStackTrace();
					throw new ScratchProjectException();
				}
	            final Pattern p = Pattern.compile("cloudToken: '([a-zA-Z0-9\\-]+)'");
	            final Matcher m = p.matcher(projectStr);
	            m.find();
	            final String cloudToken = m.group(1);
	            
	            return new ScratchCloudSession(this, cloudToken, projectID);
	}

    public ScratchUser you() {
        return new ScratchUser(this.username);
    }
}
