package androidappforyandex.com.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import androidappforyandex.com.photogallery.model.GalleryItem;

/**
 * Created by EdgeTech on 15.04.2018.
 * class that handles the networking in PhotoGallery
 *
 * FlickrFetchr will start off small with only two methods: getUrlBytes(String) and
 * getUrlString(String). The getUrlBytes(String) method fetches raw data from a URL and
 * returns it as an array of bytes. The getUrlString(String) method converts the result from
 * getUrlBytes(String) to a String.
 */

public class FlickrFetchr {

    private static final String TAG = "FlickrFetchr";
    private static final String API_KEY = "23275965a3f41093fe4cf9129b32d1d0";

    public byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new  byte[1024];
            while ((bytesRead = in.read(buffer)) > 0){
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    //method that builds an appropriate request URL and fetches its contents.
    public List<GalleryItem> fetchItems() {

        List<GalleryItem> items = new ArrayList<>();

        try{
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .build().toString();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);

            //parse JSON text into corresponding Java objects using the JSONObject(String) constructor.
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);

        }catch (IOException e){
            Log.e(TAG, "Failed to fetch items", e);
        } catch (JSONException je){
            Log.e(TAG, "Failed to parse JSON", je);
        }
        return items;
    }

    //method that pulls out information for each photo
    private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws IOException, JSONException{

        //This array contains a collection of JSONObjects, each representing metadata for a single photo
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for (int i = 0; i < photoJsonArray.length(); i++){
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setmCaption(photoJsonObject.getString("id"));
            item.setmCaption(photoJsonObject.getString("title"));

            if (!photoJsonObject.has("url_s")){
                continue;
            }

            item.setmUrl(photoJsonObject.getString("url_s"));
            items.add(item);
        }
    }
}
