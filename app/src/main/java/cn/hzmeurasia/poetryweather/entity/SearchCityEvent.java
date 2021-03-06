package cn.hzmeurasia.poetryweather.entity;

/**
 * 类名: SearchCityEvent<br>
 * 功能:(搜索后被选中城市实体类)<br>
 * 作者:黄振敏 <br>
 * 日期:2018/9/26 20:29
 */
public class SearchCityEvent {
    private String cityCode;

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public SearchCityEvent(String cityCode) {
        this.cityCode = cityCode;
    }

}
