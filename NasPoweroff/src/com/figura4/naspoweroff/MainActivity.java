package com.figura4.naspoweroff;

import java.io.ByteArrayOutputStream;
import java.util.Properties;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends Activity {
	protected EditText console;
    private String[] mPlanetTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mPlanetTitles = getResources().getStringArray(R.array.planets_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mPlanetTitles));
        // Set the list's click listener
        //mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		
		console = (EditText) findViewById(R.id.editConsole);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_settings:
	        	Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
                
	        case R.id.action_exit:
	        	Intent intent = new Intent(Intent.ACTION_MAIN);
	        	intent.addCategory(Intent.CATEGORY_HOME);
	        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        	startActivity(intent);
                break;
	    }
	    return true;
	}
	
	public void PowerOff(View v){
		Log.d("NasPoweroff", "method PowerOff called");
    	console.setText(getResources().getString(R.string.message_connecting));
    	new SshRequestThread().execute();
	}
	
	public class SshRequestThread extends AsyncTask<Void, Void, String> {
	    
		protected String doInBackground(Void...voids) {
	    	
	    	Log.d("NasPoweroff", "method doInBackground called");
	    	
	    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	    	String nasIpAddress = preferences.getString("nas_ip_address", "");
	    	String nasSshUsername = preferences.getString("nas_ssh_username", "");
	    	String nasSshPassword = preferences.getString("nas_ssh_password", "");
	    	String nasSshPort = preferences.getString("nas_ssh_port", "");
	    	String nasCommand = preferences.getString("nas_command", "");
	    	String result = "";
	    	Log.d("NasPoweroff", nasIpAddress + nasSshUsername + nasSshPassword + nasSshPort + nasCommand);
	    	
	    	try { 
	    		result += getResources().getString(R.string.message_setup_connection);
		    	JSch jsch = new JSch();
				Session session = jsch.getSession(nasSshUsername, nasIpAddress, Integer.parseInt(nasSshPort));
				session.setPassword(nasSshPassword);
				
				// Avoid asking for key confirmation
				Properties prop = new Properties();
				prop.put("StrictHostKeyChecking", "no");
				session.setConfig(prop);
				
				result += getResources().getString(R.string.message_connect);
				session.connect();
				
				// SSH Channel
				ChannelExec channelssh = (ChannelExec) 
				                session.openChannel("exec");      
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				channelssh.setOutputStream(baos);
				
				// Execute command
				result += getResources().getString(R.string.message_sending_command);
				channelssh.setCommand(nasCommand);
				channelssh.connect();        
				channelssh.disconnect();
				
				Log.d("NasPoweroff", baos.toString());
				result += baos.toString() + "\n";
				result += getResources().getString(R.string.message_command_sent);
				return result;
	    	} 
	    	catch (Exception e) 
	    	{
	    		Log.d("NasPoweroff", e.getMessage());
	    		return result + "\n" + e.getMessage();
	    	}
	    }

	    protected void onPostExecute(String result) {
	    	console.append(result);
	    }
	}
}
