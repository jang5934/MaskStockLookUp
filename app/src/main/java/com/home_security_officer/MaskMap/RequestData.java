package com.home_security_officer.MaskMap;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

public class RequestData extends AsyncTask<Integer, Void, ArrayList<Seller>>  {

    private ArrayList<Seller> sellers;
    private float latitude;
    private float longitude;
    private int search_range;

    public RequestData(float lat, float lng, int range) {
        latitude = lat;
        longitude = lng;
        search_range = range;
    }

    @Override
    protected ArrayList<Seller> doInBackground(Integer... params) {

        sellers = new ArrayList<>();

        try {
            URL url = new URL("https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1/storesByGeo/json?lat="
                    + Float.toString(latitude) + "&lng="
                    + Float.toString(longitude) + "&m=" + Integer.toString(search_range));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();

            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            jsonParsing(builder.toString());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sellers;
    }

    private void jsonParsing(String json)
    {
        try{
            JSONObject jsonObject = new JSONObject(json);

            JSONArray sellerArray = jsonObject.getJSONArray("stores");

            for(int i = 0; i < sellerArray.length(); i++)
            {
                JSONObject sellerObject = sellerArray.getJSONObject(i);

                Seller seller = new Seller();

                seller.setAddr(sellerObject.getString("addr"));
                seller.setCode(sellerObject.getString("code"));
                seller.setCreated_at(sellerObject.getString("created_at"));
                seller.setLat(Double.parseDouble(sellerObject.getString("lat")));
                seller.setLng(Double.parseDouble(sellerObject.getString("lng")));
                seller.setName(sellerObject.getString("name"));
                seller.setRemain_stat(sellerObject.getString("remain_stat"));
                seller.setStock_at(sellerObject.getString("stock_at"));
                seller.setType(sellerObject.getString("type"));

                sellers.add(seller);
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Seller> getSellers() {
        return sellers;
    }
}
