package org.pytorch.v1.TSR;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import org.pytorch.v1.BaseModuleActivity;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public abstract class AbstractCameraXActivity<R> extends BaseModuleActivity {
  private static final int REQUEST_CODE_CAMERA_PERMISSION = 1;
  private static final String[] PERMISSIONS = {Manifest.permission.CAMERA};

  private long analysisTime;


  protected abstract int getContentViewLayoutId();

  // a method that returns a view in which the camera will be displayed
  protected abstract TextureView getCameraTextureView();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStatusBarVisible(getWindow(), true);
    setContentView(getContentViewLayoutId());

    startBackgroundThread();

    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(
          this,
          PERMISSIONS,
          REQUEST_CODE_CAMERA_PERMISSION);
    } else {
      setupCameraX();
    }
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, String[] permissions, int[] grantResults) {
    if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
      if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
        Toast.makeText(
            this,
            "Приложению необходим доступ к камере.",
            Toast.LENGTH_LONG)
            .show();
        finish();
      } else {
        setupCameraX();
      }
    }
  }

  private void setupCameraX() {
    final TextureView textureView = getCameraTextureView();
    final PreviewConfig previewConfig = new PreviewConfig.Builder().build();
    final Preview preview = new Preview(previewConfig);
    preview.setOnPreviewOutputUpdateListener(output ->
            textureView.setSurfaceTexture(output.getSurfaceTexture()));

    final ImageAnalysisConfig imageAnalysisConfig =
        new ImageAnalysisConfig.Builder()
            .setTargetResolution(new Size(224, 224))
            .setCallbackHandler(backgroundHandler)
            .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            .build();
    final ImageAnalysis imageAnalysis = new ImageAnalysis(imageAnalysisConfig);
    imageAnalysis.setAnalyzer(
        (image, rotationDegrees) -> {
          if (SystemClock.elapsedRealtime() - analysisTime < 1000) {
            return;
          }

          final R result = analyzeImage(image, rotationDegrees);
          if (result != null) {
            analysisTime = SystemClock.elapsedRealtime();
            runOnUiThread(() -> applyToUiAnalyzeImageResult(result));
          }
        });

    CameraX.bindToLifecycle(this, preview, imageAnalysis);
  }

  @WorkerThread
  @Nullable
  protected abstract R analyzeImage(ImageProxy image, int rotationDegrees);

  @UiThread
  protected abstract void applyToUiAnalyzeImageResult(R result);

  // allows to show status bar of the device
  public static void setStatusBarVisible(Window window, final boolean showStatusBar) {
    View decorView = window.getDecorView();
    ViewCompat.setOnApplyWindowInsetsListener(
            decorView,
            (v, insets) -> {
              WindowInsetsCompat defaultInsets = ViewCompat.onApplyWindowInsets(v, insets);
              return defaultInsets.replaceSystemWindowInsets(
                      defaultInsets.getSystemWindowInsetLeft(),
                      showStatusBar ? 0 : defaultInsets.getSystemWindowInsetTop(),
                      defaultInsets.getSystemWindowInsetRight(),
                      defaultInsets.getSystemWindowInsetBottom());
            });
    ViewCompat.requestApplyInsets(decorView);
  }
}
