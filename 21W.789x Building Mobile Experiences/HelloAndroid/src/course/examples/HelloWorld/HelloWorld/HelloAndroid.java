package course.examples.HelloWorld.HelloWorld;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class HelloAndroid extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}
	
	public void updateText(View view) {
        TextView textView = (TextView) findViewById(R.id.textview);
		String msg = "Goodbye 21w.789x";
        textView.setText(msg);
    }
}