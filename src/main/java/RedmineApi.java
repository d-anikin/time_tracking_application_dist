import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

    public UserData getCurrentUser() throws IOException {
        String url = redmineUrl + "/users/current.json?key=" + apiKey;
        System.out.println(url);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = httpclient.execute(httpGet);
        try {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();

            if (statusLine.getStatusCode() >= 300)
                throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());

            if (entity == null)
                throw new ClientProtocolException("Response contains no content");

            Gson gson = new GsonBuilder()
                            .registerTypeAdapter(UserData.class, new NestedDeserializer<UserData>("user"))
                            .create();
            return gson.fromJson(EntityUtils.toString(entity), UserData.class);
        } finally {
            response.close();
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

    public static void main(String[] args) throws IOException {
        RedmineApi redmineApi = new RedmineApi("http://agile.dealerpoint.biz", "e584a1aeb2eb8247bd4d535da499999a889ef315");
        System.out.println("Checking " + redmineApi.checkAccess());
    }
}
