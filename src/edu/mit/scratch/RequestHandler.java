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

import edu.mit.scratch.exceptions.ScratchException;

public class RequestHandler {
	@Deprecated
	public static CloseableHttpResponse get(final ScratchSession session, final String url, final String[][] headers) throws ScratchException { // SET Referer HEADER!
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
				.setUserAgent(Scratch.USER_AGENT)
				.setDefaultCookieStore(cookieStore).build();
		CloseableHttpResponse resp;

		final HttpUriRequest update = RequestBuilder.post()
				.setUri(url)
				.addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
				.addHeader("DNT", "1")
				.addHeader("Origin", "https://scratch.mit.edu/").addHeader("Accept-Encoding", "gzip, deflate, sdch")
				.addHeader("Accept-Language", "en-US,en;q=0.8").addHeader("Content-Type", "application/json")
				.addHeader("X-Requested-With", "XMLHttpRequest")
				.addHeader("Cookie",
						"scratchsessionsid=" + session.getSessionID() + "; scratchcsrftoken=" + session.getCSRFToken())
				.addHeader("X-CSRFToken", session.getCSRFToken()).build();
		for(final String[] header : headers)
			try{ update.addHeader(header[0], header[1]); } catch(Exception e) {}
		try {
			resp = httpClient.execute(update);
			return resp;
		} catch (final IOException e) {
			throw new ScratchException();
		}
	}
	
	public static String getResponseString(CloseableHttpResponse resp) throws ScratchException {
		BufferedReader rd;
		try {
			rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
		} catch (UnsupportedOperationException | IOException e) {
			throw new ScratchException();
		}

		final StringBuffer result = new StringBuffer();
		String line = "";
		try {
			while ((line = rd.readLine()) != null)
				result.append(line);
		} catch (IOException e) {
			throw new ScratchException();
		}
		
		return result.toString().trim(); // .trim() shouldn't cause a problem?
	}
}
