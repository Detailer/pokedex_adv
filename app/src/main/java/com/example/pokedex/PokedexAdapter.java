package com.example.pokedex;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class PokedexAdapter extends RecyclerView.Adapter<PokedexAdapter.PokedexViewHolder> implements Filterable {
    public static class PokedexViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout containerView;
        public TextView textView;

        PokedexViewHolder(View view){
            super(view);
            containerView = view.findViewById(R.id.pokedex_row);
            textView = view.findViewById(R.id.pokedex_row_textview);

            containerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pokemon current = (Pokemon) containerView.getTag();
                    Intent intent = new Intent(v.getContext(), PokemonActivity.class);
                    intent.putExtra("url", current.getUrl());
                    v.getContext().startActivity(intent);
                }
            });
        }
    }

    private List<Pokemon>  pokemon = new ArrayList<>();
    private RequestQueue requestQueue;

    PokedexAdapter(Context context)
    {
        requestQueue = Volley.newRequestQueue(context);
        LoadPokemon();
    }
    public void LoadPokemon(){
        String url = "https://pokeapi.co/api/v2/pokemon?limit=150";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray results = response.getJSONArray("results");
                    for (int i = 0; i < results.length(); i++){
                        JSONObject result = results.getJSONObject(i);
                        String name = result.getString("name");
                        pokemon.add(new Pokemon(
                                name.substring(0, 1).toUpperCase() + name.substring(1),
                                result.getString("url")
                        ));
                    }
                    notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e("cs50", "JSON error: ", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon List Error");
            }
        });
        requestQueue.add(request);
    }
    @Override
    public Filter getFilter() {
        return new PokemonFilter();
    }

    List <Pokemon> filtered = new ArrayList<>(pokemon);
    private class PokemonFilter extends Filter{
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            filtered.clear();
            FilterResults results = new FilterResults();
            for(int i = 0; i < pokemon.size(); i++){
                String tmp = pokemon.get(i).getName();
                if(tmp.contains(charSequence)){
                    filtered.add(pokemon.get(i));
                }
            }
            results.values =  filtered;
            results.count = filtered.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public PokedexViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pokedex_row, parent, false);

        return new PokedexViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PokedexViewHolder holder, int position) {
            Pokemon current = filtered.get(position);
            holder.textView.setText(current.getName());
            holder.containerView.setTag(current);
    }

    @Override
    public int getItemCount() {
        if (filtered.size() != 0)
        {
            return filtered.size();
        }
        else
        {
            return 0;
        }

    }
}
