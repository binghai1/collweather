package fragmentbestpractice.example.com.coolweather;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fragmentbestpractice.example.com.coolweather.db.City;
import fragmentbestpractice.example.com.coolweather.db.County;
import fragmentbestpractice.example.com.coolweather.db.Province;
import fragmentbestpractice.example.com.coolweather.util.HttpUtil;
import fragmentbestpractice.example.com.coolweather.util.utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author Admin
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class ChooseAreaFrament extends Fragment {
    public final static int LEVEL_PROVINCE=0;
    public final static int LEVEL_CITY=1;
    public final static int LEVEL_COUNTY=2;
    private ProgressDialog mProgressDialog;
    private Button mButton;
    private TextView mTextView;
    List<String>dataList=new ArrayList<>();
    private ArrayAdapter<String> mAdapter;
    private ListView ls;
    private int currentLevel;
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province SelectProvice;
    private City SelectCity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.choose_area, container, false);
        mButton = (Button) view.findViewById(R.id.bcbt);
        mTextView = (TextView) view.findViewById(R.id.tx);
        mAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        ls = (ListView) view.findViewById(R.id.ls);
        ls.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==LEVEL_PROVINCE){
                    SelectProvice = provinceList.get(position);
                    quertCitys();
                }else if(currentLevel==LEVEL_CITY){
                    SelectCity = cityList.get(position);
                    queryCounty();
                }
            }
        });
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel==LEVEL_COUNTY){
                    quertCitys();
                }else if (currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();


    }

    private void queryCounty() {
        mTextView.setText(SelectCity.getCityName());
        mButton.setVisibility(View.VISIBLE);
        countyList=DataSupport.where("cityId=?",String.valueOf(SelectCity.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for (County county :
                    countyList) {
                dataList.add(county.getCountyName());
            }
            mAdapter.notifyDataSetChanged();
            ls.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else{
            String cityCode = SelectCity.getCityCode();
            String provinceCode = SelectProvice.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryfromServer(address,"county");
        }


    }

    private void quertCitys() {
        mTextView.setText(SelectProvice.getProvinceName());
        mButton.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceId=?",String.valueOf(SelectProvice.getId())).find(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for (City city :
                    cityList) {
                dataList.add(city.getCityName());
            }
            mAdapter.notifyDataSetChanged();
            ls.setSelection(0);
            currentLevel=LEVEL_CITY;

        }else {
            String provinceCode = SelectProvice.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryfromServer(address,"city");
        }

    }

    private void queryProvinces() {
        mTextView.setText("中国");
        mButton.setVisibility(View.GONE);
        provinceList=DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            dataList.clear();
            for (Province province:provinceList) {
                dataList.add(province.getProvinceName());
            }
            mAdapter.notifyDataSetChanged();
            ls.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else {
            String address="http://guolin.tech/api/china";
            queryfromServer(address,"province");
        }



    }

    private void queryfromServer(String address, final String type) {

        showProgressDialog();
        HttpUtil.sendOkhttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();

                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseData = response.body().string();
                boolean result=false;
                if("province".equals(type)){
                    result=utility.handProvinceResponse(responseData);
                }else if("city".equals(type)){
                    result=utility.handCityeResponse(responseData,SelectProvice.getId());
                }else if("county".equals(type)){
                    result=utility.handCountyResponse(responseData,SelectCity.getId());
                }
                if(result){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                quertCitys();
                            }else if("county".equals(type)){
                                queryCounty();
                            }

                        }


                }); }

            }
        });


    }
    private void closeProgressDialog(){
        if(mProgressDialog!=null){
            mProgressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if(mProgressDialog!=null){
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("正在加载");
            mProgressDialog.setCanceledOnTouchOutside(true);
            mProgressDialog.show();
        }

    }
}
