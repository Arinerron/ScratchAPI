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

import org.apache.http.Header;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.mit.scratch.exceptions.ScratchLoginException;
import edu.mit.scratch.exceptions.ScratchUserException;

public class Scratch {
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64)"
            + " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.111 Safari/" + "537.36";
    public static final char CLOUD = '☁';
    
    public static String CLOUD_SERVER = "cloud.scratch.mit.edu";
    public static int CLOUD_PORT = 531;
    
    public static ScratchSession createSession(final String username, String password) throws ScratchLoginException {
        try {
            final RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT) // Changed due to deprecation
                    .build();
            
            final CookieStore cookieStore = new BasicCookieStore();
            final BasicClientCookie lang = new BasicClientCookie("scratchlanguage", "en");
            lang.setDomain(".scratch.mit.edu");
            lang.setPath("/");
            cookieStore.addCookie(lang);
            
            final CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig)
                    .setUserAgent(Scratch.USER_AGENT).setDefaultCookieStore(cookieStore).build();
            CloseableHttpResponse resp;
            
            final HttpUriRequest csrf = RequestBuilder.get().setUri("https://scratch.mit.edu/csrf_token/")
                    .addHeader("Accept", "*/*").addHeader("Referer", "https://scratch.mit.edu")
                    .addHeader("X-Requested-With", "XMLHttpRequest").build();
            resp = httpClient.execute(csrf);
            resp.close();
            
            String csrfToken = null;
            for (final Cookie c : cookieStore.getCookies())
                if (c.getName().equals("scratchcsrftoken"))
                    csrfToken = c.getValue();
            
            final JSONObject loginObj = new JSONObject();
            loginObj.put("username", username);
            loginObj.put("password", password);
            loginObj.put("captcha_challenge", "");
            loginObj.put("captcha_response", "");
            loginObj.put("embed_captcha", false);
            loginObj.put("timezone", "America/New_York");
            loginObj.put("csrfmiddlewaretoken", csrfToken);
            final HttpUriRequest login = RequestBuilder.post().setUri("https://scratch.mit.edu/accounts/login/")
                    .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                    .addHeader("Referer", "https://scratch.mit.edu").addHeader("Origin", "https://scratch.mit.edu")
                    .addHeader("Accept-Encoding", "gzip, deflate").addHeader("Accept-Language", "en-US,en;q=0.8")
                    .addHeader("Content-Type", "application/json").addHeader("X-Requested-With", "XMLHttpRequest")
                    .addHeader("X-CSRFToken", csrfToken).setEntity(new StringEntity(loginObj.toString())).build();
            resp = httpClient.execute(login);
            password = null;
            final BufferedReader rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
            
            final StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null)
                result.append(line);
            final JSONObject jsonOBJ = new JSONObject(result.toString().substring(1, result.toString().length() - 1));
            if ((int) jsonOBJ.get("success") != 1)
                throw new ScratchLoginException();
            String ssi = null;
            String sct = null;
            String e = null;
            final Header[] headers = resp.getAllHeaders();
            for (final Header header : headers)
                if (header.getName().equals("Set-Cookie")) {
                    final String value = header.getValue();
                    final String[] split = value.split("; ");
                    for (final String s : split) {
                        final String[] split2 = s.split("=");
                        final String key = split2[0];
                        final String val = split2[1];
                        if (key.equals("scratchsessionsid"))
                            ssi = val;
                        else if (key.equals("scratchcsrftoken"))
                            sct = val;
                        else if (key.equals("expires"))
                            e = val;
                    }
                }
            resp.close();
            
            return new ScratchSession(ssi, sct, e, username);
        } catch (final IOException e) {
            e.printStackTrace();
            throw new ScratchLoginException();
        }
    }
    
    @NotWorking
    public static ScratchSession register(final String username, final String password, final String gender,
            final int birthMonth, final String birthYear, final String country, final String email)
            throws ScratchUserException {
        // Long if statement to verify all fields are valid
        if ((username.length() < 3) || (username.length() > 20) || (password.length() < 6) || (gender.length() < 2)
                || (birthMonth < 1) || (birthMonth > 12) || (birthYear.length() != 4) || (country.length() == 0)
                || (email.length() < 5)) {
            throw new ScratchUserException(); // TDL: Specify reason for failure
        } else {
            try {
                final RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();
                
                final CookieStore cookieStore = new BasicCookieStore();
                final BasicClientCookie lang = new BasicClientCookie("scratchlanguage", "en");
                lang.setDomain(".scratch.mit.edu");
                lang.setPath("/");
                cookieStore.addCookie(lang);
                
                CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig)
                        .setUserAgent(Scratch.USER_AGENT).setDefaultCookieStore(cookieStore).build();
                CloseableHttpResponse resp;
                
                final HttpUriRequest csrf = RequestBuilder.get().setUri("https://scratch.mit.edu/csrf_token/")
                        .addHeader("Accept", "*/*").addHeader("Referer", "https://scratch.mit.edu")
                        .addHeader("X-Requested-With", "XMLHttpRequest").build();
                try {
                    resp = httpClient.execute(csrf);
                } catch (final IOException e) {
                    e.printStackTrace();
                    throw new ScratchUserException();
                }
                try {
                    resp.close();
                } catch (final IOException e) {
                    throw new ScratchUserException();
                }
                
                String csrfToken = null;
                for (final Cookie c : cookieStore.getCookies())
                    if (c.getName().equals("scratchcsrftoken"))
                        csrfToken = c.getValue();
                
                /*
                 * try {
                 * username = URLEncoder.encode(username, "UTF-8");
                 * password = URLEncoder.encode(password, "UTF-8");
                 * birthMonth = Integer.parseInt(URLEncoder.encode("" +
                 * birthMonth, "UTF-8"));
                 * birthYear = URLEncoder.encode(birthYear, "UTF-8");
                 * gender = URLEncoder.encode(gender, "UTF-8");
                 * country = URLEncoder.encode(country, "UTF-8");
                 * email = URLEncoder.encode(email, "UTF-8");
                 * } catch (UnsupportedEncodingException e1) {
                 * e1.printStackTrace();
                 * }
                 */
                
                final BasicClientCookie csrfCookie = new BasicClientCookie("scratchcsrftoken", csrfToken);
                csrfCookie.setDomain(".scratch.mit.edu");
                csrfCookie.setPath("/");
                cookieStore.addCookie(csrfCookie);
                final BasicClientCookie debug = new BasicClientCookie("DEBUG", "true");
                debug.setDomain(".scratch.mit.edu");
                debug.setPath("/");
                cookieStore.addCookie(debug);
                
                httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig).setUserAgent(Scratch.USER_AGENT)
                        .setDefaultCookieStore(cookieStore).build();
                
                /*
                 * final String data = "username=" + username + "&password=" +
                 * password + "&birth_month=" + birthMonth + "&birth_year=" +
                 * birthYear + "&gender=" + gender + "&country=" + country +
                 * "&email=" + email +
                 * "&is_robot=false&should_generate_admin_ticket=false&usernames_and_messages=%3Ctable+class%3D'banhistory'%3E%0A++++%3Cthead%3E%0A++++++++%3Ctr%3E%0A++++++++++++%3Ctd%3EAccount%3C%2Ftd%3E%0A++++++++++++%3Ctd%3EEmail%3C%2Ftd%3E%0A++++++++++++%3Ctd%3EReason%3C%2Ftd%3E%0A++++++++++++%3Ctd%3EDate%3C%2Ftd%3E%0A++++++++%3C%2Ftr%3E%0A++++%3C%2Fthead%3E%0A++++%0A%3C%2Ftable%3E%0A&csrfmiddlewaretoken="
                 * + csrfToken;
                 * System.out.println(data);
                 */
                
                final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.addTextBody("birth_month", birthMonth + "", ContentType.TEXT_PLAIN);
                builder.addTextBody("birth_year", birthYear, ContentType.TEXT_PLAIN);
                builder.addTextBody("country", country, ContentType.TEXT_PLAIN);
                builder.addTextBody("csrfmiddlewaretoken", csrfToken, ContentType.TEXT_PLAIN);
                builder.addTextBody("email", email, ContentType.TEXT_PLAIN);
                builder.addTextBody("gender", gender, ContentType.TEXT_PLAIN);
                builder.addTextBody("is_robot", "false", ContentType.TEXT_PLAIN);
                builder.addTextBody("password", password, ContentType.TEXT_PLAIN);
                builder.addTextBody("should_generate_admin_ticket", "false", ContentType.TEXT_PLAIN);
                builder.addTextBody("username", username, ContentType.TEXT_PLAIN);
                builder.addTextBody("usernames_and_messages",
                        "<table class=\"banhistory\"> <thead> <tr> <td>Account</td> <td>Email</td> <td>Reason</td> <td>Date</td> </tr> </thead> </table>",
                        ContentType.TEXT_PLAIN);
                
                final HttpUriRequest createAccount = RequestBuilder.post()
                        .setUri("https://scratch.mit.edu/accounts/register_new_user/")
                        .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                        .addHeader("Referer", "https://scratch.mit.edu/accounts/standalone-registration/")
                        .addHeader("Origin", "https://scratch.mit.edu").addHeader("Accept-Encoding", "gzip, deflate")
                        .addHeader("DNT", "1").addHeader("Accept-Language", "en-US,en;q=0.8")
                        .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                        .addHeader("X-Requested-With", "XMLHttpRequest").addHeader("X-CSRFToken", csrfToken)
                        .addHeader("X-DevTools-Emulate-Network-Conditions-Client-Id",
                                "54255D9A-9771-4CAC-9052-50C8AB7469E0")
                        .setEntity(builder.build()).build();
                resp = httpClient.execute(createAccount);
                System.out.println("REGISTER:" + resp.getStatusLine());
                final BufferedReader rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
                
                final StringBuffer result = new StringBuffer();
                String line = "";
                while ((line = rd.readLine()) != null)
                    result.append(line);
                System.out.println("exact:" + result.toString() + "\n" + resp.getStatusLine().getReasonPhrase() + "\n"
                        + resp.getStatusLine());
                if (resp.getStatusLine().getStatusCode() != 200)
                    throw new ScratchUserException();
                resp.close();
            } catch (final Exception e) {
                e.printStackTrace();
                throw new ScratchUserException();
            }
            try {
                return Scratch.createSession(username, password);
            } catch (final Exception e) {
                e.printStackTrace();
                throw new ScratchUserException();
            }
        }
    }
    
    /*
     * TODO:
     * - Get list of featured projects
     * - Get list of newest projects
     * - Get list of curated projects
     * - Get list of team members (?)
     * - Get list of top-remixed projects
     * - Get list of top-loved projects
     * - Get list of featured studios
     * - Get current project curator
     * - Get Scratch design studio
     * - Get list of projects by following
     * - Get list of projects by following's loved projects
     * - Get list of projects in studios I'm following
     */
    protected static String consume(final CloseableHttpResponse r) throws IllegalStateException, IOException {
        final InputStream in = r.getEntity().getContent();
        final StringBuffer str = new StringBuffer();
        final byte[] b = new byte[64];
        int len;
        while ((len = in.read(b)) != -1)
            str.append(new String(b, 0, len));
        in.close();
        return str.toString();
    }
    
    public static List<ScratchUser> getUsers(final int limit, final int offset) throws ScratchUserException {
        if ((offset < 0) || (limit < 0))
            throw new ScratchUserException();
        
        final List<ScratchUser> users = new ArrayList<>();
        
        try {
            final RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();
            
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
                    .setUri("https://scratch.mit.edu/api/v1/user/?format=json&limit=" + limit + "&offset=" + offset)
                    .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .addHeader("Referer", "https://scratch.mit.edu").addHeader("Origin", "https://scratch.mit.edu")
                    .addHeader("Accept-Encoding", "gzip, deflate, sdch").addHeader("Accept-Language", "en-US,en;q=0.8")
                    .addHeader("Content-Type", "application/json").addHeader("X-Requested-With", "XMLHttpRequest")
                    .build();
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
            final JSONObject jsonOBJ = new JSONObject(result.toString().trim());
            
            final Iterator<?> keys = jsonOBJ.keys();
            
            while (keys.hasNext()) {
                final String key = "" + keys.next();
                final Object o = jsonOBJ.get(key);
                final String val = "" + o;
                
                if (key.equals("objects")) {
                    final JSONArray jsonArray = (JSONArray) o;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        final JSONObject jsonOBJ2 = jsonArray.getJSONObject(i);
                        users.add(new ScratchUser("" + jsonOBJ2.get("username")));
                    }
                }
            }
            
            return users;
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new ScratchUserException();
        } catch (final Exception e) {
            e.printStackTrace();
            throw new ScratchUserException();
        }
    }
}
