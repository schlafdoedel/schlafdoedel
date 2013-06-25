package com.kg6.schlafdoedel.custom;

import com.kg6.schlafdoedel.Overview;
import com.kg6.schlafdoedel.R;

import android.app.Activity;
import android.app.Dialog;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.LinearLayout.LayoutParams;

public class StatisticDialog extends Dialog {

	private final Activity CONTEXT;
	
	private LinearLayout contentLayout;
	private ImageView statisticImageView;
	
	public StatisticDialog(Activity context) {
		super(context);
		
		CONTEXT = context;
		
		initializeControls();
	}
	
	private void initializeControls() {
		
		setTitle("Dummy sleep statistic");
		
		contentLayout = new LinearLayout(getContext());
		contentLayout.setOrientation(LinearLayout.VERTICAL);
		
		addContentView(contentLayout, new LayoutParams(Util.GetDeviceWidth(getContext()), LayoutParams.MATCH_PARENT));
		
		statisticImageView = new ImageView(getContext());
	}
	
	public void showStatistic() {
		
		statisticImageView.setImageDrawable(CONTEXT.getResources().getDrawable(R.drawable.dummy_statistic));
		
		LayoutParams statisticImageParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		statisticImageParams.gravity = Gravity.CENTER;
		
		contentLayout.removeAllViews();
		contentLayout.addView(statisticImageView, statisticImageParams);
		
		show();
	}
}
