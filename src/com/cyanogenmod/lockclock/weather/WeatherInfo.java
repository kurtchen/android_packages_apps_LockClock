/*
 * Copyright (C) 2012 The AOKP Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyanogenmod.lockclock.weather;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

import com.cyanogenmod.lockclock.R;
import com.cyanogenmod.lockclock.misc.Preferences;
import com.cyanogenmod.lockclock.misc.WidgetUtils;

import java.text.DecimalFormat;
import java.util.Date;

public class WeatherInfo {
    private static final DecimalFormat sNoDigitsFormat = new DecimalFormat("0");

    private Context mContext;

    private String id;
    private String city;
    private String forecastDate;
    private String condition;
    private int conditionCode;
    private float temperature;
    private float lowTemperature;
    private float highTemperature;
    private String tempUnit;
    private float humidity;
    private float wind;
    private int windDirection;
    private String speedUnit;
    private long timestamp;

    private String aqiInfo;

    public WeatherInfo(Context context, String id,
            String city, String fdate, String condition, int conditionCode,
            float temp, float low, float high, String tempUnit, float humidity,
            float wind, int windDir, String speedUnit, long timestamp) {
        this.mContext = context.getApplicationContext();
        this.id = id;
        this.city = city;
        this.forecastDate = fdate;
        this.condition = condition;
        this.conditionCode = conditionCode;
        this.humidity = humidity;
        this.wind = wind;
        this.windDirection = windDir;
        this.speedUnit = speedUnit;
        this.timestamp = timestamp;
        this.temperature = temp;
        this.lowTemperature = low;
        this.highTemperature = high;
        this.tempUnit = tempUnit;
    }

    public int getConditionResource() {
        final Resources res = mContext.getResources();
        final int resId = res.getIdentifier("weather2_" + conditionCode, "drawable", mContext.getPackageName());
        if (resId != 0) {
            return resId;
        }
        return R.drawable.weather2_na;
    }

    public Bitmap getConditionBitmap(int color) {
        final Resources res = mContext.getResources();
        int resId = res.getIdentifier("weather_" + conditionCode, "drawable", mContext.getPackageName());
        if (resId == 0) {
            resId = R.drawable.weather_na;
        }
        return WidgetUtils.getOverlaidBitmap(mContext, resId, color);
    }

    public String getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

    public String getCondition() {
        final Resources res = mContext.getResources();
        final int resId = res.getIdentifier("weather_" + conditionCode, "string", mContext.getPackageName());
        if (resId != 0) {
            return res.getString(resId);
        }
        return condition;
    }

    public Date getTimestamp() {
        return new Date(timestamp);
    }

    private String getFormattedValue(float value, String unit) {
        if (Float.isNaN(highTemperature)) {
            return "-";
        }
        return sNoDigitsFormat.format(value) + unit;
    }

    public String getFormattedTemperature() {
        return getFormattedValue(temperature, "°" + tempUnit);
    }

    public String getFormattedLow() {
        return getFormattedValue(lowTemperature, "°");
    }

    public String getFormattedHigh() {
        return getFormattedValue(highTemperature, "°");
    }

    public String getFormattedHumidity() {
        return getFormattedValue(humidity, "%");
    }

    public String getFormattedWindSpeed() {
        if (wind < 0) {
            return mContext.getString(R.string.unknown);
        }
        return getFormattedValue(wind, speedUnit);
    }

    public String getWindDirection() {
        int resId;

        if (windDirection < 0) resId = R.string.unknown;
        else if (windDirection < 23) resId = R.string.weather_N;
        else if (windDirection < 68) resId = R.string.weather_NE;
        else if (windDirection < 113) resId = R.string.weather_E;
        else if (windDirection < 158) resId = R.string.weather_SE;
        else if (windDirection < 203) resId = R.string.weather_S;
        else if (windDirection < 248) resId = R.string.weather_SW;
        else if (windDirection < 293) resId = R.string.weather_W;
        else if (windDirection < 338) resId = R.string.weather_NW;
        else resId = R.string.weather_N;

        return mContext.getString(resId);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WeatherInfo for ");
        builder.append(city);
        builder.append(" (");
        builder.append(id);
        builder.append(") @ ");
        builder.append(getTimestamp());
        builder.append(": ");
        builder.append(getCondition());
        builder.append("(");
        builder.append(conditionCode);
        builder.append("), temperature ");
        builder.append(getFormattedTemperature());
        builder.append(", low ");
        builder.append(getFormattedLow());
        builder.append(", high ");
        builder.append(getFormattedHigh());
        builder.append(", humidity ");
        builder.append(getFormattedHumidity());
        builder.append(", wind ");
        builder.append(getFormattedWindSpeed());
        builder.append(" at ");
        builder.append(getWindDirection());

        builder.append("; AQI info:");
        builder.append(getAqiInfo());

        return builder.toString();
    }

    public String toSerializedString() {
        StringBuilder builder = new StringBuilder();
        builder.append(id).append('|');
        builder.append(city).append('|');
        builder.append(forecastDate).append('|');
        builder.append(condition).append('|');
        builder.append(conditionCode).append('|');
        builder.append(temperature).append('|');
        builder.append(lowTemperature).append('|');
        builder.append(highTemperature).append('|');
        builder.append(tempUnit).append('|');
        builder.append(humidity).append('|');
        builder.append(wind).append('|');
        builder.append(windDirection).append('|');
        builder.append(speedUnit).append('|');
        builder.append(timestamp);

        if (aqiInfo != null) {
            builder.append('|').append(aqiInfo);
        }

        return builder.toString();
    }

    public static WeatherInfo fromSerializedString(Context context, String input) {
        if (input == null) {
            return null;
        }

        String[] parts = input.split("\\|");
        if (parts == null || (parts.length != 14 && parts.length != 15)) {
            return null;
        }

        int conditionCode, windDirection;
        long timestamp;
        float temperature, low, high, humidity, wind;

        try {
            conditionCode = Integer.parseInt(parts[4]);
            temperature = Float.parseFloat(parts[5]);
            low = Float.parseFloat(parts[6]);
            high = Float.parseFloat(parts[7]);
            humidity = Float.parseFloat(parts[9]);
            wind = Float.parseFloat(parts[10]);
            windDirection = Integer.parseInt(parts[11]);
            timestamp = Long.parseLong(parts[13]);
        } catch (NumberFormatException e) {
            return null;
        }

        WeatherInfo info = new WeatherInfo(context,
                /* id */ parts[0], /* city */ parts[1], /* date */ parts[2],
                /* condition */ parts[3], conditionCode, temperature, low, high,
                /* tempUnit */ parts[8], humidity, wind, windDirection,
                /* speedUnit */ parts[12], timestamp);

        if (parts.length == 15) {
            info.setAqiInfo(parts[14]);
        }

        return info;
    }

    // text format example:
    // 11-27-2013 09:00; PM2.5; 99.0; 173; Unhealthy (at 24-hour exposure at this level)
    public static String parseAqiInfo(String text) {
        if (text == null) {
            return null;
        }

        String[] segments = text.split(";");
        if (segments.length != 5) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        // I know the date, only need time
        String[] timeSegments = segments[0].split("\\s");
        if (timeSegments.length == 2) {
            builder.append(timeSegments[1]);
        } else {
            // builder.append(segments[0]);
            return null; // I don't care about 24hr avg
        }

        builder.append(";").append(segments[1]);
        builder.append(":").append(segments[2].trim());
        builder.append("; AQI:").append(segments[3].trim());

        int index = segments[4].lastIndexOf('(');
        if (index > 0) {
            builder.append(";").append(segments[4].substring(0, index - 1));
        } else {
            builder.append(";").append(segments[4]);
        }

        return builder.toString();
    }

    public int[] getAqiLevelColors() {
        int[] colors = new int[2];
        colors[0] = Preferences.weatherFontColor(mContext);
        colors[1] = 0;

        if (aqiInfo == null) {
            return colors;
        }

        String[] segments = aqiInfo.split(";");
        if (segments.length != 4) {
            return colors;
        }

        String[] aqiSegments = segments[2].split(":");
        if (aqiSegments.length != 2) {
            return colors;
        }

        int aqi = -1;
        try {
            aqi = Integer.parseInt(aqiSegments[1]);
        } catch(NumberFormatException e) {
            aqi = -1;
        }

        if (-1 == aqi) {
            return colors;
        }

        // refer to http://www.airnow.gov/index.cfm?action=aqibasics.aqi
        if (aqi >= 0 && aqi <= 50) {
            colors[0] = mContext.getResources().getColor(R.color.aqi_info_text_color_black);
            colors[1] = mContext.getResources().getColor(R.color.aqi_level_good);
        } else if (aqi >= 51 && aqi <= 100) {
            colors[0] = mContext.getResources().getColor(R.color.aqi_info_text_color_black);
            colors[1] = mContext.getResources().getColor(R.color.aqi_level_moderate);
        } else if (aqi >= 101 && aqi <= 150) {
            colors[0] = mContext.getResources().getColor(R.color.aqi_info_text_color_white);
            colors[1] = mContext.getResources().getColor(R.color.aqi_level_unhealthy_for_sensitive);
        } else if (aqi >= 151 && aqi <= 200) {
            colors[0] = mContext.getResources().getColor(R.color.aqi_info_text_color_white);
            colors[1] = mContext.getResources().getColor(R.color.aqi_level_unhealthy);
        } else if (aqi >= 201 && aqi <= 300) {
            colors[0] = mContext.getResources().getColor(R.color.aqi_info_text_color_white);
            colors[1] = mContext.getResources().getColor(R.color.aqi_level_very_unhealthy);
        } else if (aqi >= 301 && aqi <= 500) {
            colors[0] = mContext.getResources().getColor(R.color.aqi_info_text_color_white);
            colors[1] = mContext.getResources().getColor(R.color.aqi_level_hazardous);
        }

        return colors;
    }

    public String getAqiInfo() {
        return aqiInfo;
    }

    public void setAqiInfo(String aqiInfo) {
        this.aqiInfo = aqiInfo;
    }
}
