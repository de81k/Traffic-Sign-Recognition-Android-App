package org.pytorch.v1.TSR;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.v1.BaseModuleActivity;
import org.pytorch.v1.Constants;
import org.pytorch.v1.R;
import org.pytorch.v1.TSR.view.ResultRowView;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.ImageProxy;
import androidx.constraintlayout.widget.ConstraintLayout;

public class SignsClassificationActivity extends AbstractCameraXActivity<SignsClassificationActivity.AnalysisResult> {
  private static final int COUNT = 3;
  public static final String SCORES_FORMAT = "%.2f";
  protected static final int INPUT_TENSOR_WIDTH = 224;
  protected static final int INPUT_TENSOR_HEIGHT = 224;

  static class AnalysisResult {
    private final String[] topSignsNames;
    private final float[] topScores;

    public AnalysisResult(String[] topSignsNames, float[] topScores) {
      this.topSignsNames = topSignsNames;
      this.topScores = topScores;
    }
  }

  private boolean analysisErrorState;
  private ResultRowView[] resultRowViews = new ResultRowView[COUNT];
  private Module module;
  private FloatBuffer inputBuffer;
  private Tensor inputTensor;

  @Override
  protected int getContentViewLayoutId() {
    return R.layout.activity_recognition;
  }

  @Override
  protected TextureView getCameraTextureView() { return findViewById(R.id.camera_texture_view); }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mainLayoutFilling();
  }

  @Override
  protected void applyToUiAnalyzeImageResult(AnalysisResult result) {
    for (int i = 0; i < COUNT; i++) {
      final ResultRowView rowView = resultRowViews[i];
      rowView.nameTextView.setText(result.topSignsNames[i]);
      rowView.scoreTextView.setText(String.format(Locale.US, SCORES_FORMAT,
          result.topScores[i]));
    }
  }

  @Override
  @WorkerThread
  @Nullable
  protected AnalysisResult analyzeImage(ImageProxy image, int rotationDegrees) {
    if (analysisErrorState) {
      return null;
    }

    try {
      if (module == null) {
        final String fileName = "TSR_model.pt";
        final String modelAbsolutePath = new File(
            getFilePath(this, fileName)).getAbsolutePath();
        module = Module.load(modelAbsolutePath);

        inputBuffer =
            Tensor.allocateFloatBuffer(3 * INPUT_TENSOR_WIDTH * INPUT_TENSOR_HEIGHT);
        inputTensor = Tensor.fromBlob(inputBuffer, new long[]{1, 3, INPUT_TENSOR_HEIGHT, INPUT_TENSOR_WIDTH});
      }

      TensorImageUtils.imageYUV420CenterCropToFloatBuffer(
          image.getImage(), rotationDegrees, INPUT_TENSOR_WIDTH, INPUT_TENSOR_HEIGHT,
          TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB,
              inputBuffer, 0);

      final Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();

      final float[] scores = outputTensor.getDataAsFloatArray();
      final int[] ids = topIds(scores);

      final String[] topSignsNames = new String[COUNT];
      final float[] topScores = new float[COUNT];
      for (int i = 0; i < COUNT; i++) {
        final int id = ids[i];
        topSignsNames[i] = Constants.SIGNS_CLASSES[id];
        topScores[i] = scores[id];
      }
      return new AnalysisResult(topSignsNames, topScores);
    } catch (Exception e) {
      Log.e(Constants.TAG, "Ошибка во время выполнения анализа изображения", e);
      analysisErrorState = true;
      return null;
    }
  }

  public static String getFilePath(Context context, String assetName) {
    File file = new File(context.getFilesDir(), assetName);
    if (file.exists() && file.length() > 0) {
      return file.getAbsolutePath();
    }

    try (InputStream is = context.getAssets().open(assetName)) {
      try (OutputStream os = new FileOutputStream(file)) {
        byte[] buffer = new byte[4 * 1024];
        int read;
        while ((read = is.read(buffer)) != -1) {
          os.write(buffer, 0, read);
        }
        os.flush();
      }
      return file.getAbsolutePath();
    } catch (IOException e) {
      Log.e(Constants.TAG, "Ошибка доступа к файлу");
    }
    return null;
  }

  public static int[] topIds(float[] scores) {
    float values[] = new float[COUNT];
    Arrays.fill(values, -Float.MAX_VALUE);
    int ids[] = new int[COUNT];
    Arrays.fill(ids, -1);

    for (int i = 0; i < scores.length; i++) {
      for (int j = 0; j < COUNT; j++) {
        if (scores[i] > values[j]) {
          for (int k = COUNT - 1; k >= j + 1; k--) {
            values[k] = values[k - 1];
            ids[k] = ids[k - 1];
          }
          values[j] = scores[i];
          ids[j] = i;
          break;
        }
      }
    }
    return ids;
  }

  private int startRowViewId = 1111;

  private void mainLayoutFilling() {
    final int main_layout_id = R.id.main_layout;
    final ConstraintLayout mainConstraintLayout = findViewById(main_layout_id);

    final float scale = getResources().getDisplayMetrics().density;
    final int padding_dp = 8;
    final int margin_dp = 16;
    final int padding = (int) (padding_dp * scale + 0.5f);
    final int margin = (int) (margin_dp * scale + 0.5f);
    for (int i = COUNT - 1; i >= -1; i--) {
      ResultRowView resultRowView = new ResultRowView(this);
      resultRowView.setId(startRowViewId + i);
      mainConstraintLayout.addView(resultRowView);
      ConstraintLayout.LayoutParams resultRowViewParams =
              (ConstraintLayout.LayoutParams) resultRowView.getLayoutParams();

      if (i == COUNT - 1) {
        resultRowViewParams.bottomToBottom = main_layout_id;
      }
      else {
        resultRowViewParams.bottomToTop = startRowViewId + i + 1;
      }
      resultRowViewParams.leftToLeft = main_layout_id;
      resultRowViewParams.rightToRight = main_layout_id;
      resultRowViewParams.topMargin = margin;
      resultRowViewParams.leftMargin = 2 * margin;
      resultRowViewParams.rightMargin = 2 * margin;
      resultRowViewParams.bottomMargin = margin;
      resultRowView.setPadding(padding, padding, padding, padding);
      resultRowView.setLayoutParams(resultRowViewParams);
      if (i == - 1) {
        resultRowView.nameTextView.setText(R.string.results_header_name);
        resultRowView.scoreTextView.setText(R.string.results_header_score);
      }
      else {
        resultRowViews[i] = findViewById(startRowViewId + i);
      }

      TextureView cameraView = findViewById(R.id.camera_texture_view);
      ConstraintLayout.LayoutParams cameraViewParams =
              (ConstraintLayout.LayoutParams) cameraView.getLayoutParams();

      cameraViewParams.bottomToTop = startRowViewId - 1;
      cameraViewParams.topToTop = main_layout_id;
      cameraView.setLayoutParams(cameraViewParams);

      Toolbar toolbar = findViewById(R.id.toolbar);
      mainConstraintLayout.removeView(toolbar);
      mainConstraintLayout.addView(toolbar);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (module != null) {
      module.destroy();
    }
  }
}
