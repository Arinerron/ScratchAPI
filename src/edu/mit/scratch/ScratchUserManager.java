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
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONObject;

import edu.mit.scratch.exceptions.ScratchUserException;

public class ScratchUserManager {
	private ScratchSession session = null;
	private int message_count = 0;

	public ScratchUserManager(ScratchSession session) throws ScratchUserException {
		if (session != null)
			this.session = session;
		else
			throw new ScratchUserException();
	}

	public void clearMessages() throws ScratchUserException {
		try {
			final RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY)
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
					.setUri("https://scratch.mit.edu/messages/ajax/messages-clear/")
					.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
					.addHeader("Referer", "https://scratch.mit.edu").addHeader("Origin", "https://scratch.mit.edu")
					.addHeader("Accept-Encoding", "gzip, deflate, sdch").addHeader("Accept-Language", "en-US,en;q=0.8")
					.addHeader("Content-Type", "application/json").addHeader("X-Requested-With", "XMLHttpRequest")
					.addHeader("Cookie",
							"scratchsessionsid=" + this.session.getSessionID() + "; scratchcsrftoken="
									+ this.session.getCSRFToken())
					.addHeader("X-CSRFToken", this.session.getCSRFToken()).build();
			try {
				resp = httpClient.execute(update);
			} catch (final IOException e) {
				e.printStackTrace();
				throw new ScratchUserException();
			}
		} catch (Exception e) {
			throw new ScratchUserException();
		}
	}

	public int getMessageCount() {
		try {
			this.update();
		} catch (ScratchUserException e) {
		}
		return this.message_count;
	}

	public ScratchUserManager update() throws ScratchUserException {
		try {
			final RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY)
					.build();

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

			final CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig)
					.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64)"
							+ " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.111 Safari/" + "537.36")
					.setDefaultCookieStore(cookieStore).build();
			CloseableHttpResponse resp;

			final HttpUriRequest update = RequestBuilder.get()
					.setUri("https://scratch.mit.edu/messages/ajax/get-message-count/")
					.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
					.addHeader("Referer", "https://scratch.mit.edu").addHeader("Origin", "https://scratch.mit.edu")
					.addHeader("Accept-Encoding", "gzip, deflate, sdch").addHeader("Accept-Language", "en-US,en;q=0.8")
					.addHeader("Content-Type", "application/json").addHeader("X-Requested-With", "XMLHttpRequest")
					.addHeader("Cookie",
							"scratchsessionsid=" + this.session.getSessionID() + "; scratchcsrftoken="
									+ this.session.getCSRFToken())
					.addHeader("X-CSRFToken", this.session.getCSRFToken()).build();
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
			System.out.println("msgdata:" + result.toString());
			final JSONObject jsonOBJ = new JSONObject(result.toString().trim());

			final Iterator<?> keys = jsonOBJ.keys();

			while (keys.hasNext()) {
				final String key = "" + keys.next();
				final Object o = jsonOBJ.get(key);
					final String val = "" + o;

					switch (key) {
					case "msg_count":
						this.message_count = Integer.parseInt(val);
						break;
					default:
						System.out.println("Missing reference:" + key);
						break;
				}
			}
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new ScratchUserException();
		} catch (final IOException e) {
			e.printStackTrace();
			throw new ScratchUserException();
		}

		return this;
	}
}
