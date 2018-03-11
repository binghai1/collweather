package fragmentbestpractice.example.com.coolweather.util;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fragmentbestpractice.example.com.coolweather.db.City;
import fragmentbestpractice.example.com.coolweather.db.County;
import fragmentbestpractice.example.com.coolweather.db.Province;

/**
 * @author Admin
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class utility {
    public static boolean handProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)) {
            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(object.getString("name"));
                    province.setProvinceCode(object.getString("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;

    }    public static boolean handCityeResponse(String response,int provinceId){
        if(!TextUtils.isEmpty(response)) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                City city = new City();
                city.setCityCode(object.getString("id"));
                city.setCityName(object.getString("name"));
                city.setProvinceId(provinceId);
                city.save();
            }
            return true;

        }

        catch (JSONException e) {
            e.printStackTrace();
        }}
        return false;

    }    public static boolean handCountyResponse(String response,int cityId){
        if(!TextUtils.isEmpty(response)) {
            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    County county = new County();
                    county.setCityId(cityId);
                    county.setCountyName(object.getString("name"));
                    county.setWeatherId(object.getString("weather_id"));
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;

    }
}
