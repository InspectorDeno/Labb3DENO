package dennisdufback.app.labb3deno;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyActivity extends Activity {

    private EditText editText;      // inputs text from user
    private String input;
    private Integer nOfResults = 10;
    private Integer ID = 0;
    private Integer jsonID;
    private ViewGroup listLayout;   // Viewgroup for our custom ListItem views
    private List<String> names;
    private Map<Integer, List<String>> searchResults = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        // layout in wich we add our custom ListItem views
        listLayout = (ViewGroup) findViewById(R.id.listLayout);
        listLayout.isVerticalScrollBarEnabled();

        editText = (EditText) findViewById(R.id.editText);
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // If the user inputs a valid character we load the data in a new thread
                if(s.toString().matches("[[:alpha:]]+")){
                    input = s.toString();
                    loadData();
                    ID++;
                }
                // Clear the list if the search field is empty
                else if(editText.getText().toString().isEmpty()){
                        listLayout.removeAllViewsInLayout();
                        updateLayout(listLayout);
                    }
                // User didn't use valid characters
                else {
                    Toast prompt = Toast.makeText(getApplicationContext(),"Bara bokstäver i namnet",Toast.LENGTH_SHORT);
                    prompt.setGravity(Gravity.TOP,0,100);
                    prompt.show();
                    closeContextMenu();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

    }
    // Updates the viewGroup
    private void updateLayout(ViewGroup viewGroup){
        viewGroup.invalidate();
        viewGroup.requestLayout();
    }

    private void addName(String itemText){
        // Here we inflate the item_layout.xml file, add a ListItem to it and then add the layout to the viewGroup
        View itemLayout = LayoutInflater.from(this).inflate(R.layout.item_layout, listLayout,false);
        final ListItem name = (ListItem) itemLayout.findViewById(R.id.list_item);

        name.setListText(itemText);
        listLayout.addView(itemLayout);

        // this makes the listItems clickable
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(name.getListText());
                editText.setSelection(editText.getText().length());
                name.setBackgroundColor(Color.DKGRAY);
                name.setTextColor(Color.RED);
            }
        });

    }

    private void loadData() {
        loadWithThread();
    }

    private void doNetworkCall(String input, Integer ID){


            // Client used to grab data from a provided URL
            DefaultHttpClient httpclient = new DefaultHttpClient();
            // Provide the URL for the get request
            HttpGet httpget = new HttpGet("http://flask-afteach.rhcloud.com/getnames/"+ ID +"/" + input);
        try {
            // The client calls for the get request to execute and sends the results back
            HttpResponse response = httpclient.execute(httpget);
            // Holds the message sent by the response
            HttpEntity entity = response.getEntity();
            // Get the content sent
            InputStream inputStream = entity.getContent();

            // BufferedReader uses an InputStreamReader that converts the bytes into characters
            // We read UTF-8 encoding form the JSON data
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
            // Storing each line of data in a StringBuilder
            StringBuffer sb = new StringBuffer("");
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line + "\n");
            }
            in.close();

            // Save the results in a String
            String result = sb.toString();

            // Create a JSONObject by passing the JSON data
            JSONObject jObject = new JSONObject(result);
            // Get the Array named result that contains all the names
            JSONArray jNames = jObject.getJSONArray("result");
            // Get the specific ID we used that matches the returned names
            jsonID = jObject.getInt("id");
            names = new ArrayList<>();
            // Add returned names to an ArrayList
            for(int i = 0; i < jNames.length(); i++){
                names.add(jNames.get(i).toString());
            }
            // And store those names together with the ID used in the get request
            searchResults.put(ID, names);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void loadWithThread(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
//                searchResults.put(ID,new ArrayList<String>());
                doNetworkCall(input, ID);
                listLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        // Clear the list
                        listLayout.removeAllViewsInLayout();
                        /* Store the List of names in a temporary string. These names match their
                        jsonID to make sure the program doesn't crash if the user inputs
                        characters in quick succession.*/
                        List<String> temp = searchResults.get(jsonID);
                        //
                        for (int i = 0; (i < temp.size()) && i < nOfResults; i++) {
                            addName(temp.get(i));
                        }
                        updateLayout(listLayout);
                    }
                });
            }
        });
        t.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class NetWorker extends AsyncTask<Void,Void,Void> {


        @Override
        protected Void doInBackground(Void... params) {
            doNetworkCall(input,ID);
            return null;
        }
//
//        protected void onPostExecute(String s) {
//            result.setText(s);
//        }
    }

}