package ru.dealerpoint.redmine;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class IssuesData {
    private int totalCount;
    private int offset;
    private int limit;
    private ArrayList<Issue> items;

    public ArrayList<Issue> getItems() {
        return items;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getPageCount() {
        return totalCount / limit + 1;
    }

    public IssuesData(String jsonString) {
        Type listType = new TypeToken<ArrayList<Issue>>(){}.getType();
        JsonParser parser = new JsonParser();
        JsonObject data = parser.parse(jsonString).getAsJsonObject();
        totalCount = data.get("total_count").getAsInt();
        offset = data.get("offset").getAsInt();
        limit = data.get("limit").getAsInt();
        items = new Gson().fromJson(data.get("issues"), listType);
    }
}
