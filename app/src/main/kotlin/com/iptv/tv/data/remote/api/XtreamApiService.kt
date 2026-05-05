package com.iptv.tv.data.remote.api

import com.iptv.tv.data.remote.dto.CategoryDto
import com.iptv.tv.data.remote.dto.LiveStreamDto
import com.iptv.tv.data.remote.dto.SeriesStreamDto
import com.iptv.tv.data.remote.dto.SeriesInfoResponse
import com.iptv.tv.data.remote.dto.UserInfoResponse
import com.iptv.tv.data.remote.dto.VodInfoDto
import com.iptv.tv.data.remote.dto.VodStreamDto
import retrofit2.http.GET
import retrofit2.http.Query

interface XtreamApiService {

    @GET("player_api.php")
    suspend fun getUserInfo(
        @Query("username") username: String,
        @Query("password") password: String
    ): UserInfoResponse

    @GET("player_api.php")
    suspend fun getLiveCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_categories"
    ): List<CategoryDto>

    @GET("player_api.php")
    suspend fun getLiveStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_streams",
        @Query("category_id") categoryId: String
    ): List<LiveStreamDto>

    @GET("player_api.php")
    suspend fun getVodCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_categories"
    ): List<CategoryDto>

    @GET("player_api.php")
    suspend fun getVodStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_streams",
        @Query("category_id") categoryId: String
    ): List<VodStreamDto>

    @GET("player_api.php")
    suspend fun getSeriesCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series_categories"
    ): List<CategoryDto>

    @GET("player_api.php")
    suspend fun getSeriesStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series",
        @Query("category_id") categoryId: String
    ): List<SeriesStreamDto>

    @GET("player_api.php")
    suspend fun getVodInfo(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_info",
        @Query("vod_id") vodId: String
    ): VodInfoDto

    @GET("player_api.php")
    suspend fun getSeriesInfo(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series_info",
        @Query("series_id") seriesId: Int
    ): SeriesInfoResponse
}
