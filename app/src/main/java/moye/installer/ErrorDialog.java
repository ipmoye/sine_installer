package moye.installer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ErrorDialog extends Activity {
    @Override
    protected void attachBaseContext(Context newBase) {
        //用来锁定DPI为320
        Configuration origConfig = newBase.getResources().getConfiguration();
        origConfig.densityDpi = 320;
        Context confBase = newBase.createConfigurationContext(origConfig);
        super.attachBaseContext(confBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_dialog);
        Intent intent = this.getIntent();
        TextView title = (TextView) findViewById(R.id.err_dialog_title);
        title.setText(intent.getStringExtra("title"));
        TextView content = (TextView) findViewById(R.id.err_dialog_content);
        content.setText(intent.getStringExtra("content"));
    }

    public void _on_ok_click(View view) {
        finish();
    }

    @Override
    public void onBackPressed() {

    }
}