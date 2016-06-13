package br.unb.frc;

import java.io.File;
import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.support.v4.app.NavUtils;

import edu.pdx.cs410.wifi.direct.file.frc.R;

public class FileBrowser extends Activity {

	private String root;
	private String currentPath;
	
	private ArrayList<String> targets;
	private ArrayList<String> paths;
	
	private File targetFile;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);
        getActionBar().setDisplayHomeAsUpEnabled(true);
              
        root = "/";
        currentPath = root;
        
        targets = null;
        paths = null;
        
        targetFile = null;

        showDir(currentPath);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_file_browser, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void selectDirectory(View view) {
    	
	     File file = new File(currentPath);
	     targetFile = file;

		returnTarget();
    }
    

    public void setCurrentPathText(String message) {
    	TextView fileTransferStatusText = (TextView) findViewById(R.id.current_path);
    	fileTransferStatusText.setText(message);	
    }


	private void showDir(String targetDirectory){
		
		setCurrentPathText("Diretório corrente: " + currentPath);
		
		targets = new ArrayList<String>();
		paths = new ArrayList<String>();
				
	     File f = new File(targetDirectory);
	     File[] directoryContents = f.listFiles();
	     
	     
		if (!targetDirectory.equals(root)) {
			targets.add(root);
			paths.add(root);
			targets.add("../");
			paths.add(f.getParent());
		}
		
		for(File target: directoryContents) {
			paths.add(target.getPath());
			
			if(target.isDirectory()) {
		        targets.add(target.getName() + "/");
			}
			else {
		        targets.add(target.getName());

			}
		}
		
		ListView fileBrowserListView = (ListView) findViewById(R.id.file_browser_listview);

	    ArrayAdapter<String> directoryData = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, targets);
	    fileBrowserListView.setAdapter(directoryData);
	    
	    fileBrowserListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View view, int pos,long id) {
				
				  File file = new File(paths.get(pos));
				  
				  if(file.isFile()) {
					  targetFile = file;
					  returnTarget();
				  }
				  else
				  {
					  if(file.canRead()) {
						  currentPath = paths.get(pos);
						  showDir(paths.get(pos));
					  }
				  }
			}
			});
	}
	
	public void returnTarget() {
		Intent returnIntent = new Intent();
		returnIntent.putExtra("file", targetFile);
		setResult(RESULT_OK, returnIntent);
		finish();
	}
}


