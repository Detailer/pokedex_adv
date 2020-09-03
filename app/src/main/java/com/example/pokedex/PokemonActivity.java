package com.example.pokedex;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URL;

import static android.app.PendingIntent.getActivity;

public class PokemonActivity extends AppCompatActivity {
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView type1TextView;
    private TextView type2TextView;
    private String url;
    private RequestQueue requestQueue;
    private TextView buttonView;
    private ImageView imgView;
    private TextView descView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        url = getIntent().getStringExtra("url");
        nameTextView = findViewById(R.id.pokemon_name);
        numberTextView = findViewById(R.id.pokemon_number);
        type1TextView = findViewById(R.id.pokemon_type1);
        type2TextView = findViewById(R.id.pokemon_type2);
        buttonView = findViewById(R.id.catch_button);
        imgView = findViewById(R.id.pokemon_img);
        descView = findViewById(R.id.pokemon_desc);
        load();

    }

    public void load(){
        type1TextView.setText("");
        type2TextView.setText("");
        // CALL API FOR POKEMON DETAILS
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @SuppressLint("ApplySharedPref")
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String poke_name = response.getString("name");
                    poke_name =  poke_name.substring(0, 1).toUpperCase() + poke_name.substring(1).toLowerCase();
                    nameTextView.setText(poke_name);
                    numberTextView.setText(String.format("#%03d", response.getInt("id")));
                    boolean chk = getPreferences(Context.MODE_PRIVATE).getBoolean(nameTextView.getText().toString(), false);
                    Log.d("cs50",  "CHECK INITIAL: " + chk + "," + nameTextView.getText().toString());
                    if(chk){
                        buttonView.setText("Release");
                    }
                    else{
                        buttonView.setText("Catch");
                    }
                    // GET URL FOR SECOND API CALL
                    String url1 = "https://pokeapi.co/api/v2/pokemon-species/" + response.getInt("id");
                    // CALL API FOR POKEMON DESC.
                    JsonObjectRequest request1 = new JsonObjectRequest(Request.Method.GET, url1, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray desc_array = response.getJSONArray("flavor_text_entries");
                                JSONObject desc_obj = desc_array.getJSONObject(0);
                                String desc = desc_obj.getString("flavor_text");
                                descView.setText(desc);
                            } catch (JSONException e1) {
                                Log.e("cs50", "Pokemon Description Error!");
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("cs50", "JSON Desc Details Error!");
                        }
                    });
                    requestQueue.add(request1);
                    JSONObject images = response.getJSONObject("sprites");
                    JSONObject img_object = images.getJSONObject("other");
                    JSONObject other_img_arr = img_object.getJSONObject("official-artwork");
                    String img_url = other_img_arr.getString("front_default");
                    Log.d("cs50", "IMAGE LINK : " + img_url);

                    new DownloadSpriteTask().execute(img_url);
                    JSONArray typeEntries = response.getJSONArray("types");
                    for (int i = 0 ; i < response.length(); i++){
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        int slot = typeEntry.getInt("slot");
                        String type = typeEntry.getJSONObject("type").getString("name");
                        type = type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();
                        if(slot == 1){
                            type1TextView.setText(type);
                        }
                        else if(slot == 2){
                            type2TextView.setText(type);
                        }
                    }

                } catch (JSONException e) {
                    Log.e("cs50", "Pokemon JSON error: ", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon details Error");
            }
        });
        requestQueue.add(request);
    }

    public void toggleCatch(View view){
        boolean chk = getPreferences(Context.MODE_PRIVATE).getBoolean(nameTextView.getText().toString(), false);
        Log.d("cs50",  "toggleCatch: " + chk);
        if(chk){
            getPreferences(Context.MODE_PRIVATE).edit().putBoolean(nameTextView.getText().toString(), false).apply();
            buttonView.setText("Catch");
            boolean chk1 = getPreferences(Context.MODE_PRIVATE).getBoolean(nameTextView.getText().toString(), false);
            Log.d("cs50",  "toggleCatch: " + chk1);
        }
        else{
            getPreferences(Context.MODE_PRIVATE).edit().putBoolean(nameTextView.getText().toString(), true).apply();
            buttonView.setText("Release");
            boolean chk1 = getPreferences(Context.MODE_PRIVATE).getBoolean(nameTextView.getText().toString(), false);
            Log.d("cs50",  "toggleCatch: " + chk1);
        }
    }

    private class DownloadSpriteTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                return BitmapFactory.decodeStream(url.openStream());
            }
            catch (IOException e) {
                Log.e("cs50", "Download sprite error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imgView.setImageBitmap(bitmap);
        }
    }
}