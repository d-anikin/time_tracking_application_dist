package ru.dealerpoint.redmine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import ru.dealerpoint.NestedDeserializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class Api {
    private String redmineUrl;
    private String apiKey;
    private Session session = null;

    static public String getVersion() {
        return "0.0.3";
    }

    public Api(String redmineUrl, String apiKey) {
        this(redmineUrl, apiKey, null);
    }

    public Api(String redmineUrl, String apiKey, Session session) {
        this.setRedmineUrl(redmineUrl);
        this.setApiKey(apiKey);
        this.session = session;
    }

    /* private */
    private String makeRequest(HttpUriRequest request) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = httpclient.execute(request);
        try {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();

            if (statusLine.getStatusCode() == 401)
                throw new ApiWrongKeyException("API access key is wrong!");

            if (statusLine.getStatusCode() == 403)
                throw new ApiAuthorizationException("You are not authorized to access this action!");

            if (statusLine.getStatusCode() >= 300)
                throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());

            if (entity == null)
                throw new ClientProtocolException("Response contains no content");

            return EntityUtils.toString(entity);
        } finally {
            response.close();
        }
    }

    private String getEndPoint(String url) {
        String endPoint = redmineUrl + url + "?key=" + apiKey;
        if (session != null && session.getTtaSession() != null)
            endPoint += "&tta_session=" + session.getTtaSession();
        return endPoint;
    }

    private String getRequest(String url) throws IOException {
        return getRequest(url, null);
    }

    private String getRequest(String url, String[] params) throws IOException {
        String endPoint = getEndPoint(url);
        if (params != null && params.length > 0)
            endPoint += "&" + StringUtils.join(params, '&');

        HttpGet httpGet = new HttpGet(endPoint);
        return makeRequest(httpGet);
    }

    private String postRequest(String url, String json) throws IOException {
        String endPoint = getEndPoint(url);
        HttpPost httpPost = new HttpPost(endPoint);
        httpPost.setHeader("Content-Type", "application/json");
        if (json != null) {
            httpPost.setEntity(new StringEntity(json, "UTF-8"));
        }
        return makeRequest(httpPost);
    }

    private String putRequest(String url, String json) throws IOException {
        String endPoint = getEndPoint(url);
        HttpPut httpPut = new HttpPut(endPoint);
        httpPut.setHeader("Content-Type", "application/json");
        if (json != null) {
            httpPut.setEntity(new StringEntity(json, "UTF-8"));
        }
        return makeRequest(httpPut);
    }

    private String deleteRequest(String url) throws IOException {
        String endPoint = getEndPoint(url);
        HttpDelete httpPut = new HttpDelete(endPoint);
        return makeRequest(httpPut);
    }

    private ArrayList<Item> getItems(String path, String keyName) {
        Type type = new TypeToken<ArrayList<Item>>(){}.getType();
        try {
            String response = getRequest(path);
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(type, new NestedDeserializer<ArrayList<Item>>(keyName))
                    .create();
            return gson.fromJson(response, type);
        } catch (Exception e) {
            return new ArrayList<Item>();
        }
    }

    /* public */
    public Session createSession() throws IOException {
        if (session == null) {
            String response = postRequest("/tta/session.json", null);
            Gson gson = new Gson();
            Session tmpSession = gson.fromJson(response, Session.class);
            if (!tmpSession.getVersion().equals(Api.getVersion()))
                throw new ApiWrongVersionException("Please update your application to continue!");
            session = tmpSession;
        }
        return session;
    }

    public void destroySession() throws IOException {
        deleteRequest("/tta/session.json");
        session = null;
    }

    public User getCurrentUser() throws IOException {
        String response = getRequest("/users/current.json");
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(User.class, new NestedDeserializer<User>("user"))
                .create();
        return gson.fromJson(response, User.class);
    }

    public ArrayList<Issue> getIssues() throws IOException {
        Type type = new TypeToken<ArrayList<Issue>>(){}.getType();

        String response;
        response = getRequest("/tta/issues.json");
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(type, new NestedDeserializer<ArrayList<Issue>>("issues"))
                .create();
        return gson.fromJson(response, type);
    }

    public Issue getIssue(Long id) {
        try {
            String response = getRequest("/issues/" + id.toString() + ".json");
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Issue.class, new NestedDeserializer<Issue>("issue"))
                    .create();
            return gson.fromJson(response, Issue.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void idle() throws IOException {
        String response = getRequest("/tta/idle.json");
    }

    public TimeEntry startWork(Long issueId) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("issue_id", issueId);

        String response = postRequest("/tta/time_entry/start.json", new Gson().toJson(json));
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(TimeEntry.class, new NestedDeserializer<TimeEntry>("time_entry"))
                .create();
        return gson.fromJson(response, TimeEntry.class);
    }

    public TimeEntry updateWork(TimeEntry timeEntry) throws IOException {
        String response =
                putRequest(
                        "/tta/time_entry/" + timeEntry.getId().toString() + ".json",
                        new Gson().toJson(timeEntry)
                );
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(TimeEntry.class, new NestedDeserializer<TimeEntry>("time_entry"))
                .create();
        return gson.fromJson(response, TimeEntry.class);
    }

    public TimeEntry stopWork(TimeEntry timeEntry) throws IOException {
        String response =
                putRequest(
                        "/tta/time_entry/" + timeEntry.getId().toString() + "/stop.json",
                        new Gson().toJson(timeEntry)
                );
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(TimeEntry.class, new NestedDeserializer<TimeEntry>("time_entry"))
                .create();
        return gson.fromJson(response, TimeEntry.class);
    }

    public ArrayList<Item> getQueries() {
        return getItems("/queries.json", "queries");
    }

    public ArrayList<Item> getActivities() {
        return getItems("/enumerations/time_entry_activities.json", "time_entry_activities");
    }

    public String getRedmineUrl() {
        return this.redmineUrl;
    }

    public void setRedmineUrl(String redmineUrl) {
        String str = StringUtils.stripEnd(redmineUrl, "/");
        if (StringUtils.startsWithIgnoreCase(str, "http://") || StringUtils.startsWithIgnoreCase(str, "https://")) {
            this.redmineUrl = str;
        } else {
            this.redmineUrl = "http://" + str;
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
