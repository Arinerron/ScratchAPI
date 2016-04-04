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

import edu.mit.scratch.exceptions.ScratchStatisticalException;

public class ScratchStatistics {
    public static int getProjectCount() throws ScratchStatisticalException {
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
            
            final HttpUriRequest update = RequestBuilder.get().setUri("https://api.scratch.mit.edu/projects/count/all")
                    .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .addHeader("Referer", "https://scratch.mit.edu").addHeader("Origin", "https://scratch.mit.edu")
                    .addHeader("Accept-Encoding", "gzip, deflate, sdch").addHeader("Accept-Language", "en-US,en;q=0.8")
                    .addHeader("Content-Type", "application/json").addHeader("X-Requested-With", "XMLHttpRequest")
                    .build();
            try {
                resp = httpClient.execute(update);
            } catch (final IOException e) {
                e.printStackTrace();
                throw new ScratchStatisticalException();
            }
            
            BufferedReader rd;
            try {
                rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
            } catch (UnsupportedOperationException | IOException e) {
                e.printStackTrace();
                throw new ScratchStatisticalException();
            }
            
            final StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null)
                result.append(line);
            final JSONObject jsonOBJ = new JSONObject(result.toString().trim());
            
            final int preCount = (Integer) jsonOBJ.get("count");
            return preCount;
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new ScratchStatisticalException();
        } catch (final Exception e) {
            e.printStackTrace();
            throw new ScratchStatisticalException();
        }
    }
}
