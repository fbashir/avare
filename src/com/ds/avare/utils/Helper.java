/*
Copyright (c) 2012, Zubair Khan (governer@gmail.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.ds.avare.utils;


import java.util.Locale;

import com.ds.avare.storage.Preferences;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.view.WindowManager;

/**
 * 
 * @author zkhan
 *
 */
public class Helper {

    /**
     * 
     * @param lonlat
     */
    public static double truncGeo(double lonlat) {
        lonlat *= 10000;
        lonlat = Math.round(lonlat);
        lonlat /= 10000;
        return lonlat;
    }
    
    /**
     * 
     * @param paint
     */
    public static void invertCanvasColors(Paint paint) {
       float mx [] = {
                -1.0f,  0.0f,  0.0f,  1.0f,  0.0f,
                0.0f,  -1.0f,  0.0f,  1.0f,  0.0f,
                0.0f,  0.0f,  -1.0f,  1.0f,  0.0f,
                1.0f,  1.0f,  1.0f,  1.0f,  0.0f 
       };
       ColorMatrix cm = new ColorMatrix(mx);
       paint.setColorFilter(new ColorMatrixColorFilter(cm));
    }

    /**
     * See the explanation in the function setThreshold. 
     * @param altitude in FL for printing
     * @return
     */
    public static String calculateAltitudeFromThreshold(int threshold) {
        double altitude = (threshold) * Preferences.heightConversion * 50.0 / 100.0;
        return(String.format(Locale.getDefault(), "FL%03d", (int)altitude));
    }

    /**
     * See the explanation in the function setThreshold. 
     * @param altitude
     * @return
     */
    public static int calculateThreshold(double altitude) {
        double threshold = altitude / Preferences.heightConversion / 50.0; 
        return((int)Math.round(threshold));
    }

    /**
     * 
     * @param paint
     */
    public static void setThreshold(Paint paint, float threshold) {
        /*
         * Elevation matrix. This will threshold the elevation with GPS altitude.
         * The factor is used to increase the brightness for a given elevation map.
         * Elevation map is prepared so that altitudes from 0-5000 meter are encoded with 0-200 pixel values.
         * Each pixel level is 25 meter. 
         * 
         * Negative sign for black threshold instead of white.
         * Threshold of to 0 to 100 translated to 0 - 200 for all pixels thresholded at 5000 meters.
         * 
         * Calibrated (visually) at 
         * KRNO - 1346 meters, threshold = 28
         * KLXV - 3027 meters, threshold = 61
         * L70  - 811  meters, threshold = 16
         * KHIE - 326  meters, threshold = 7
         *--------------------------------------------
         *       5510                    = 112  ~ 50 meters per px
         * Formula to calculate threshold is:
         * threshold = altitude / 3 (meters per foot) / 50
         * Give 2 levels margin of safety
         */
        float factor = 8.f;
        float mx [] = {
                factor, 0,             0,             0,  -(factor) * (threshold - 5) * 2.0f,
                0,      factor / 1.5f, 0,             0,  -(factor) * (threshold - 5) * 2.0f,
                0,      0,             factor / 2.0f, 0,  -(factor) * (threshold - 5) * 2.0f,
                0     , 0,             0,             1,  0
       };
       ColorMatrix cm = new ColorMatrix(mx);
       paint.setColorFilter(new ColorMatrixColorFilter(cm));
    }

    /**
     * 
     * @param paint
     */
    public static void restoreCanvasColors(Paint paint) {
       paint.setColorFilter(null);
    }
    
    /**
     * 
     * @param lon
     * @return
     */
    public static boolean isLongitudeSane(double lon) {
        return (lon < 0) && (lon > -180); 
    }
    
    /**
     * 
     * @param lat
     * @return
     */
    public static boolean isLatitudeSane(double lat) {
        return (lat >= 0) && (lat < 90); 
    }
    
    
    /**
     * 
     * @param distance
     * @param eta
     * @param heading
     * @return
     */
    public static String makeLine(double value, String unit, String eta, double heading) {
        String valTrunc = String.format(Locale.getDefault(), "%4d", (Math.round(value)));
        return 
                valTrunc + unit + " " +  eta + " " + 
                Helper.correctConvertHeading(Math.round(heading)) + '\u00B0';
    }
    
    /**
     * 
     * @param heading
     * @return
     */
    public static String correctConvertHeading(long heading) {
        String ret = String.format(Locale.getDefault(), "%03d", heading);
        if(ret.equals("000")) {
            ret = "360";
        }
        return ret;
    }
    
    /**
     * 
     * @param val
     * @return
     */
    public static String removeLeadingZeros(String val) {
        return val.replaceFirst("^0+(?!$)", ""); 
    }
    
    /**
     * 
     * @param variation
     * @return
     */
    public static double parseVariation(String variation) {
        double var = 0;
        if((null == variation) || (variation.length() < 3)) {
            return 0;
        }
        else {
            var = Double.parseDouble(variation.substring(0, 2));            
            if(variation.contains("E")) {
                var = -var;                 
            }
        }
        return var;
    }

    /**
     * 
     * @param variation
     * @return
     */
    public static String makeVariation(double variation) {
        int var = (int)Math.round(variation);
        String ret = String.format(Locale.getDefault(), "%02d", var);
        if(var < 0) {
            ret = "E" + var + "\u00B0";
        }
        else {
            ret = "W" + var + "\u00B0";
        }
        return ret;
    }

    /**
     * Set theme
     * @param act
     */
    public static void setTheme(Activity act) {
        Preferences p = new Preferences(act.getApplicationContext()); 
        if(p.isNightMode()) {
            act.setTheme(android.R.style.Theme_Black);
        }
        else {
            act.setTheme(android.R.style.Theme_Light);            
        }
    }
    
    /**
     * Set common features of all activities in the framework
     * @param act
     */
    public static void setOrientationAndOn(Activity act) {
        
        Preferences pref = new Preferences(act.getApplicationContext());
        if(pref.shouldScreenStayOn()) {
            act.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);            
        }

        if(pref.isPortrait()) {
            act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);            
        }
        else {
            act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

    }    
}
