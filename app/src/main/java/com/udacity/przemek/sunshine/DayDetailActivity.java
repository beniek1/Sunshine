package com.udacity.przemek.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import static com.udacity.przemek.sunshine.R.id.textView;

public class DayDetailActivity extends AppCompatActivity {

    /**
     * Constant for intent with weather information.
     */
    public static final String WEATHER_INFO = "weatherInfo";
    /**
     * Share action provider.
     */
    private ShareActionProvider menuShareActionProvider;
    /**
     * TextView that displays weather information.
     */
    private TextView weatherDisplayText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_detail);
        Intent callingIntent = getIntent();

        weatherDisplayText = (TextView) findViewById(textView);
        weatherDisplayText.setText(callingIntent.getStringExtra(WEATHER_INFO));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);

        MenuItem shareMenuItem = menu.findItem(R.id.share_menu);
        menuShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);

        if (menuShareActionProvider != null) {
            menuShareActionProvider.setShareIntent(createShareForecastIntent());
        }

        return super.onCreateOptionsMenu(menu);

    }


    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getWeatherString());
        return shareIntent;
    }

    /**
     * Gets currently displayed weather string.
     */
    private String getWeatherString() {
        return weatherDisplayText.getText().toString();
    }
}
