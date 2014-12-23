package dennisdufback.app.labb3deno;


import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
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
import java.lang.CharSequence;
import java.lang.Integer;
import java.lang.Override;
import java.lang.Runnable;
import java.lang.String;
import java.lang.StringBuffer;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InteractiveSearcher extends LinearLayout {

    Context context;

   /**
    * Maximum number of results displayed in the list
    */
    private int MAXIMUM_RESULTS = 0;

    /**
     * Integer which matches the network search with the right results
     */
    private int ID = 0;

    private EditText textField;
    private int jsonID;
    private ViewGroup listLayout;   // Viewgroup for our custom ListItem views
//    private List<String> names;
    private Map<Integer, List<String>> searchResults = new HashMap<>();

    public InteractiveSearcher(final Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.searcher_layout, this);


        listLayout = (ViewGroup) findViewById(R.id.listLayout);
        listLayout.setVerticalScrollBarEnabled(true);

        textField = (EditText) findViewById(R.id.textField);
        textField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                // If the user inputs a valid character we load data in a new thread
                if(input.matches("[[:alpha:]]+")) {
                    loadData(input);
                    ID++;
                }
                // Clear the list if the textfield is empty
                else if (textField.getText().length() == 0) {
                    listLayout.removeAllViewsInLayout();
                    updateLayout(listLayout);
                } else {
                    Toast prompt = Toast.makeText(context,"Bara bokstäver i namnet", Toast.LENGTH_SHORT);
                    prompt.setGravity(Gravity.TOP,0,100);
                    prompt.show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    /**
     * Loads data in a new thread with networkcall
     * depending on the typed input in the textfield
     * @param input String with the search phrase to search for
     */
    private void loadData(final String input) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                doNetworkCall(input, ID);
                listLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        // Clear the list
                        listLayout.removeAllViewsInLayout();

                        /*
                         * Store the List of names in a temporary string. These names match their
                         * jsonID to make sure the program doesn't crash if the user inputs
                         * characters in quick succession.
                        */
                        List<String> temp = searchResults.get(jsonID);
                        //
                        for (int i = 0; (i < temp.size()) && i < MAXIMUM_RESULTS; i++) {
                            addWord(temp.get(i));
                        }
                        updateLayout(listLayout);
                    }
                });
            }
        });
        t.start();
    }

    /**
     * Updates the viewGroup
     */
    private void updateLayout(ViewGroup viewGroup){
        viewGroup.invalidate();
        viewGroup.requestLayout();
    }

    /**
     * Adds a word to the list of results
     */
    private void addWord(String itemText){
        // Here we inflate the item_layout.xml file, add a ListItem to it and then add the layout to the viewGroup
        View itemLayout = LayoutInflater.from(context).inflate(R.layout.item_layout, listLayout,false);
        final ListItem word = (ListItem) itemLayout.findViewById(R.id.list_item);

        word.setListText(itemText);
        listLayout.addView(itemLayout);

        // this makes the listItems clickable
        word.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textField.setText(word.getListText());
                textField.setSelection(textField.getText().length());
                word.setBackgroundColor(Color.DKGRAY);
            }
        });

    }

    /**
     * Makes a call to fetch the result of the searchphrase typed in the
     * textfield. Stores them in the HashMap declared in the class
     * @param input String with the searchphrase to search for
     * @param ID The unique ID for the search requested by the URL
     */
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

            List names = new ArrayList<>();

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
    // Set how many names will be shown at most
    public void setMaxNumOfNames(Integer n){
        MAXIMUM_RESULTS = n;
    }

}
