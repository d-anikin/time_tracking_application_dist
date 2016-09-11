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

import javax.swing.text.StyledEditorKit;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class Api {
    private String redmineUrl;
    private String apiKey;

    static public String getVersion() {
        return "0.0.1";
    }

    public Api(String redmineUrl, String apiKey) {
        this.setRedmineUrl(redmineUrl);
        this.setApiKey(apiKey);
    }

    private String getRequest(String url) throws IOException {
        return getRequest(url, null);
    }

    private String getRequest(String url, String[] params) throws IOException {
        String endPoint = redmineUrl + url + "?key=" + apiKey;
        if (params != null && params.length > 0) {
            endPoint += "&" + StringUtils.join(params, '&');
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

    public Details getDetails() {
        try {
            String response = getRequest("/time_tracking_application/details.json");
            Gson gson = new Gson();
            return gson.fromJson(response, Details.class);
        } catch (IOException e) {
            return null;
        }
    }

    public Boolean checkAccess() {
        Details details = getDetails();
        return (details != null && details.getVersion().equals(Api.getVersion()));
    }

    public User getCurrentUser() throws IOException {
        String response = getRequest("/users/current.json");
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(User.class, new NestedDeserializer<User>("user"))
                .create();
        return gson.fromJson(response, User.class);
    }

    public IssuesData getIssues(Long queryId, int offset) throws IOException {
        Type type = new TypeToken<ArrayList<Issue>>(){}.getType();

        String response;
        if (queryId != null) {
            String[] params = {
                    "query_id=" + queryId.toString(),
                    "offset=" + offset,
                    "limit=100"
            };
            response = getRequest("/issues.json", params);
        } else {
            response = getRequest("/issues.json");
        }

        return new IssuesData(response);
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

    public Issue startWork(Long id) {
        return null;
    }

    public Issue stopWork(Long id) {
        return null;
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
        this.redmineUrl = StringUtils.stripEnd(redmineUrl, "/");
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
