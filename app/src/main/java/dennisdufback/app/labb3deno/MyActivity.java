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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MyActivity extends Activity {
    private EditText editText;
    //    private TextView result;
    private String input;
    private Integer jsonID;
    private Integer ID = 0;
    private ViewGroup listLayout;

    List<String> names;
    Map<Integer, List<String>> searchResults = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
//        result = (TextView) findViewById(R.id.textViewNames);
        listLayout = (ViewGroup) findViewById(R.id.listLayout);
        listLayout.isVerticalScrollBarEnabled();

        editText = (EditText) findViewById(R.id.editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.toString().matches("[[:alpha:]]+") || s.toString().isEmpty()) {
                    input = s.toString();
                    loadData();
                    ID++;
                }
                else{
                    Toast prompt = Toast.makeText(getApplicationContext(),"Bara bokstäver i namnet",Toast.LENGTH_SHORT);
                    prompt.setGravity(Gravity.TOP,0,100);
                    prompt.show();
                    closeContextMenu();
                }
                if(editText.getText().toString().isEmpty()){
                    listLayout.removeAllViewsInLayout();
                    listLayout.invalidate();
                    listLayout.requestLayout();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


    }

    private void addName(String itemText){

        View itemLayout = LayoutInflater.from(this).inflate(R.layout.item_layout, listLayout,false);
        final ListItem name = (ListItem) itemLayout.findViewById(R.id.list_item);

        name.setListText(itemText);
        listLayout.addView(itemLayout);
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),name.getListText(),Toast.LENGTH_SHORT).show();
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


    private void loadWithThread(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                searchResults.put(ID,new ArrayList<String>());
                doNetworkCall(input, ID);
                listLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        ;
                        listLayout.removeAllViewsInLayout();
//                        result.setText("");
//                        result.append("ID: " + ID.toString() + " input: " + input + "\n");
                        List<String> temp = searchResults.get(ID);
                        for (int i = 0; i < temp.size(); i++) {
//                            result.append(temp.get(i) + "\n");
                            addName(temp.get(i));
                        }
                    }
                });
            }
        });
        t.start();
    }



    private void doNetworkCall(String input, Integer ID){

        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet("http://flask-afteach.rhcloud.com/getnames/"+ ID +"/" + input);
            HttpResponse response = httpclient.execute(httpget);

            BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer sb = new StringBuffer("");
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line + "\n");
            }
            in.close();
            String result = sb.toString();

            JSONObject jObject = new JSONObject(result);
            JSONArray jNames = jObject.getJSONArray("result");
            Integer jID = jObject.getInt("id");
            jsonID = jID;
            names = new ArrayList<>();
            for(int i = 0; i < jNames.length(); i++){
                names.add(jNames.get(i).toString());
            }
            searchResults.put(ID, names);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
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