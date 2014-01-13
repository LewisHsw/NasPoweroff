package com.figura4.naspoweroff;

import java.io.ByteArrayOutputStream;
import java.util.Properties;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class TurnoffFragment extends Fragment implements OnClickListener {
	protected EditText console;
	
    public TurnoffFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_turn_off, container, false);
        getActivity().setTitle("Turn Off NAS");
        
        Button btn = (Button) rootView.findViewById(R.id.poweroff_button);     
        btn.setOnClickListener(this);
        
        // @TODO fix fucking null pointer exception!!!
        console = (EditText) rootView.findViewById(R.id.editConsole);
        
        return rootView;
    }
    
    @Override
    public void onClick(View v) {
        PowerOff(v);
    }
    
	public void PowerOff(View v){
		Log.d("NasPoweroff", "method PowerOff called");
    	console.setText(v.getResources().getString(R.string.message_connecting));
    	new SshRequestThread().execute();
	}
	
	public class SshRequestThread extends AsyncTask<Void, Void, String> {
	    
		protected String doInBackground(Void...voids) {
	    	
	    	Log.d("NasPoweroff", "method doInBackground called");
	    	
	    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
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
	    	Log.d("NasPoweroff", result);
	    	console.append(result);
	    }  
	}
}
