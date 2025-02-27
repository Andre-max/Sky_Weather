package com.octagontechnologies.sky_weather.repository.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.octagontechnologies.sky_weather.domain.Location
import com.octagontechnologies.sky_weather.domain.Lunar
import com.octagontechnologies.sky_weather.domain.location.LatLng
import com.octagontechnologies.sky_weather.repository.database.lunar.LunarDao
import com.octagontechnologies.sky_weather.repository.database.toLocalLunar
import com.octagontechnologies.sky_weather.repository.database.toLunar
import com.octagontechnologies.sky_weather.repository.network.lunar.LunarForecastApi
import com.octagontechnologies.sky_weather.utils.Resource
import com.octagontechnologies.sky_weather.utils.doOperation
import com.octagontechnologies.sky_weather.utils.toLunarDateFormat
import com.octagontechnologies.sky_weather.utils.toLunarTimeZone
import javax.inject.Inject


class LunarRepo @Inject constructor(
    private val lunarApi: LunarForecastApi,
    private val lunarDao: LunarDao
) {

    val currentLunar = lunarDao.getLocalLunar().map { it?.lunarForecast }

    /*
    Whenever a daily forecast is selected, the DailyForecastViewModel will push an online request for lunar info
    through this repo.getSelectedLunar(). If the request is successful, the value is pushed to selectedLunar
    which will be connected to both the SelectedDaily Fragment and ViewModel
     */
    private val _selectedLunar = MutableLiveData<Lunar>()
    val selectedLunar: LiveData<Lunar> = _selectedLunar


//    suspend fun setUpRefresh() {
//        locationRepo.location.asFlow().collectLatest { location ->
//            if (location != null)
//                refreshCurrentLunarForecast(location)
//        }
//    }

    suspend fun refreshCurrentLunarForecast(
        location: Location
    ) {
        val dateInMillis: Long = System.currentTimeMillis()
        val lunarForecastResponse =
            lunarApi.getLunarForecast(
                date = dateInMillis.toLunarDateFormat(),
                lat = location.lat.toDouble(),
                lon = location.lon.toDouble(),
                timezone = dateInMillis.toLunarTimeZone()
            )

        lunarDao.insertData(lunarForecastResponse.toLocalLunar())
    }

    suspend fun getSelectedLunarForecast(
        location: LatLng,
        dateInMillis: Long
    ): Resource<Lunar> = doOperation {
        val lunarForecastResponse =
            lunarApi.getLunarForecast(
                date = dateInMillis.toLunarDateFormat(),
                lat = location.lat.toDouble(),
                lon = location.lon.toDouble(),
                timezone = dateInMillis.toLunarTimeZone()
            )

        Resource.Success(lunarForecastResponse.toLunar())
    }
}