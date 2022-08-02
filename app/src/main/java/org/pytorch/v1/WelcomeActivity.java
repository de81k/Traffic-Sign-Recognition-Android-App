package org.pytorch.v1;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.pytorch.v1.TSR.SignsClassificationActivity;

public class WelcomeActivity extends AppCompatActivity {

  private ViewPager viewPager; // to organize viewing of introductory information about the application
  private PagerAdapter pagerAdapter; // to provide data for ViewPager
  private TabLayout tabLayout;

  /// page class for ViewPager class
  private static class PageData {
    private int titleId;
    private int imageId;
    private int descriptionId;

    public PageData(int titleId, int imageId, int descriptionId) {
      this.titleId = titleId;
      this.imageId = imageId;
      this.descriptionId = descriptionId;
    }
  }

  private static final PageData[] PAGES = new PageData[] {
      new PageData(
          R.string.title,
          R.drawable.welcome,
          R.string.welcome_page_1_description),
      new PageData(
          R.string.welcome_page_2_title,
          R.drawable.pytorch,
          R.string.welcome_page_2_description),
      new PageData(
          R.string.welcome_page_3_title,
          R.drawable.camera,
          R.string.welcome_page_3_description)
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_welcome);

    findViewById(R.id.skip_button).setOnClickListener(v -> {
      startActivity(new Intent(WelcomeActivity.this, SignsClassificationActivity.class));
    });

    viewPager = findViewById(R.id.welcome_view_pager);
    pagerAdapter = new WelcomePagerAdapter();
    viewPager.setAdapter(pagerAdapter);

    tabLayout = findViewById(R.id.welcome_tab_layout);
    tabLayout.setupWithViewPager(viewPager);
  }

  private class WelcomePagerAdapter extends PagerAdapter {
    @Override
    public int getCount() {
      return PAGES.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
      return object == view;
    }

    // adding a page
    @Override
    @NonNull
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
      final LayoutInflater inflater = LayoutInflater.from(WelcomeActivity.this);
      final View pageView = inflater.inflate(R.layout.pager_page, container, false);
      final TextView titleTextView = pageView.findViewById(R.id.pager_page_title);
      final TextView descriptionTextView = pageView.findViewById(R.id.pager_page_description);
      final ImageView imageView = pageView.findViewById(R.id.pager_page_image);

      final PageData pageData = PAGES[position];
      titleTextView.setText(pageData.titleId);
      descriptionTextView.setText(pageData.descriptionId);
      imageView.setImageResource(pageData.imageId);
      container.addView(pageView);
      return pageView;
    }

    // deleting a page
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
      container.removeView((View) object);
    }
  }
}
