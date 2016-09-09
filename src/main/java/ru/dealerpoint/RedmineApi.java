package ru.dealerpoint;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by dmitry on 06.09.16.
 */
public class RedmineApi {
    private String redmineUrl;
    private String apiKey;

    public RedmineApi(String redmineUrl, String apiKey) {
        this.setRedmineUrl(redmineUrl);
        this.setApiKey(apiKey);
    }

    public boolean checkAccess() {
        try {
            UserData userData = this.getCurrentUser();
            return userData != null;
        } catch (Exception e) {
            return false;
        }
    }

    private String getRequest(String url) throws IOException {
        return getRequest(url, null);
    }

    private String getRequest(String url, String params) throws IOException {
        String endPoint = redmineUrl + url + "?key=" + apiKey;
        if (params != null && !params.isEmpty()) {
            endPoint += "&" + params;
        }

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(endPoint);
        CloseableHttpResponse response = httpclient.execute(httpGet);
        try {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();

            if (statusLine.getStatusCode() >= 300)
                throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());

            if (entity == null)
                throw new ClientProtocolException("Response contains no content");

            return EntityUtils.toString(entity);
        } finally {
            response.close();
        }
    }

    public UserData getCurrentUser() throws IOException {
        String response = getRequest("/users/current.json");
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(UserData.class, new NestedDeserializer<UserData>("user"))
                .create();
        return gson.fromJson(response, UserData.class);
    }

    public ArrayList<IssueData> getIssues() throws IOException {
        return getIssues(null);
    }

    public ArrayList<IssueData> getIssues(Long queryId) throws IOException {
        Type type = new TypeToken<ArrayList<IssueData>>(){}.getType();

        String response;
        if (queryId != null) {
            response = getRequest("/issues.json", "query_id=" + queryId.toString());
        } else {
            response = getRequest("/issues.json");
        }
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(type, new NestedDeserializer<ArrayList<IssueData>>("issues"))
                .create();
        return gson.fromJson(response, type);
    }

    public ArrayList<ItemData> getQueries() {
        Type type = new TypeToken<ArrayList<ItemData>>(){}.getType();
        try {
            String response = getRequest("/queries.json");
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(type, new NestedDeserializer<ArrayList<ItemData>>("queries"))
                    .create();
            return gson.fromJson(response, type);
        } catch (Exception e) {
            return new ArrayList<ItemData>();
        }
    }

    public String getRedmineUrl() {
        return this.redmineUrl;
    }

    public void setRedmineUrl(String redmineUrl) {
        this.redmineUrl = StringUtils.stripEnd(redmineUrl, "/");
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
