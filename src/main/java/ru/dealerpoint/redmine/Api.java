package ru.dealerpoint.redmine;

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
import ru.dealerpoint.NestedDeserializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class Api {
    private String redmineUrl;
    private String apiKey;

    public Api(String redmineUrl, String apiKey) {
        this.setRedmineUrl(redmineUrl);
        this.setApiKey(apiKey);
    }

    public boolean checkAccess() {
        try {
            User user = this.getCurrentUser();
            return user != null;
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

    public User getCurrentUser() throws IOException {
        String response = getRequest("/users/current.json");
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(User.class, new NestedDeserializer<User>("user"))
                .create();
        return gson.fromJson(response, User.class);
    }

    public IssuesData getIssues(Long queryId, int pageIndex) throws IOException {
        Type type = new TypeToken<ArrayList<Issue>>(){}.getType();

        String response;
        if (queryId != null) {
            response = getRequest("/issues.json", "query_id=" + queryId.toString());
        } else {
            response = getRequest("/issues.json");
        }

        return new IssuesData(response);
    }

    public ArrayList<Item> getQueries() {
        Type type = new TypeToken<ArrayList<Item>>(){}.getType();
        try {
            String response = getRequest("/queries.json");
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(type, new NestedDeserializer<ArrayList<Item>>("queries"))
                    .create();
            return gson.fromJson(response, type);
        } catch (Exception e) {
            return new ArrayList<Item>();
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
