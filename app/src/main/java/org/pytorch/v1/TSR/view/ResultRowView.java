package org.pytorch.v1.TSR.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.pytorch.v1.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ResultRowView extends RelativeLayout {
  public final TextView nameTextView;
  public final TextView scoreTextView;

  public ResultRowView(@NonNull Context context) {
    this(context, null);
  }

  public ResultRowView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ResultRowView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
  }

  public ResultRowView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
                       int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    inflate(context, R.layout.result_row, this);
    nameTextView = findViewById(R.id.result_row_name_text_view);
    scoreTextView = findViewById(R.id.result_row_score_text_view);
  }
}
