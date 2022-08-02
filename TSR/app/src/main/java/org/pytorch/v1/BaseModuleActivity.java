package org.pytorch.v1;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/// base class for flow control
public class BaseModuleActivity extends AppCompatActivity {

  protected HandlerThread backgroundThread;
  protected Handler backgroundHandler;
  protected Handler UIHandler;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    UIHandler = new Handler(getMainLooper());
  }

  @Override
  protected void onPostCreate(@Nullable Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    final Toolbar toolbar = findViewById(R.id.toolbar);
    if (toolbar != null) {
      setSupportActionBar(toolbar); // allows to work info button
    }
    startBackgroundThread();
  }

  protected void startBackgroundThread() {
    backgroundThread = new HandlerThread("ModuleActivity");
    backgroundThread.start();
    backgroundHandler = new Handler(backgroundThread.getLooper());
  }

  @Override
  protected void onDestroy() {
    stopBackgroundThread();
    super.onDestroy();
  }

  protected void stopBackgroundThread() {
    backgroundThread.quitSafely();
    try {
      backgroundThread.join();
      backgroundThread = null;
      backgroundHandler = null;
    } catch (InterruptedException e) {
      Log.e(Constants.TAG, "Ошибка при остановке фонового потока", e);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }

  // to call help through the menu
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.info) {
      LayoutInflater inflater = LayoutInflater.from(this);
      View view = inflater.inflate(R.layout.info, null, false);
      TextView infoTextView = view.findViewById(R.id.info_title);
      TextView descriptionTextView = view.findViewById(R.id.info_description);

      infoTextView.setText(R.string.info_title);
      StringBuilder sb = new StringBuilder(this.getString(R.string.info_description));
      descriptionTextView.setText(sb.toString());

      final AlertDialog.Builder builder = new AlertDialog.Builder(this)
              .setCancelable(true)
              .setView(view);
      builder.show();
    }
    return super.onOptionsItemSelected(item);
  }
}
