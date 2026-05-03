package com.iptv.tv.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CategoryDto(
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("parent_id") val parentId: Int = 0,
    @SerializedName("num") val streamCount: Int? = null
)

data class LiveStreamDto(
    @SerializedName("num") val num: Int,
    @SerializedName("name") val name: String,
    @SerializedName("stream_type") val streamType: String,
    @SerializedName("stream_id") val streamId: Int,
    @SerializedName("stream_icon") val streamIcon: String?,
    @SerializedName("epg_channel_id") val epgChannelId: String?,
    @SerializedName("added") val added: String?,
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("tv_archive") val tvArchive: Int = 0,
    @SerializedName("direct_source") val directSource: String?
)

data class VodStreamDto(
    @SerializedName("num") val num: Int,
    @SerializedName("name") val name: String,
    @SerializedName("stream_type") val streamType: String,
    @SerializedName("stream_id") val streamId: Int,
    @SerializedName("stream_icon") val streamIcon: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("added") val added: String?,
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("container_extension") val containerExtension: String?,
    @SerializedName("direct_source") val directSource: String?
)

data class SeriesStreamDto(
    @SerializedName("num") val num: Int,
    @SerializedName("name") val name: String,
    @SerializedName("series_id") val seriesId: Int,
    @SerializedName("cover") val cover: String?,
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("container_extension") val containerExtension: String?
)

data class UserInfoResponse(
    @SerializedName("user_info") val userInfo: UserInfo?,
    @SerializedName("server_info") val serverInfo: ServerInfo?
)

data class UserInfo(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("status") val status: String,       // "Active" / "Banned"
    @SerializedName("exp_date") val expDate: String?,
    @SerializedName("max_connections") val maxConnections: String,
    @SerializedName("allowed_output_formats") val allowedOutputFormats: List<String>
)

data class ServerInfo(
    @SerializedName("url") val url: String,
    @SerializedName("port") val port: String,
    @SerializedName("https_port") val httpsPort: String?,
    @SerializedName("server_protocol") val serverProtocol: String,
    @SerializedName("rtmp_port") val rtmpPort: String?,
    @SerializedName("timezone") val timezone: String
)
