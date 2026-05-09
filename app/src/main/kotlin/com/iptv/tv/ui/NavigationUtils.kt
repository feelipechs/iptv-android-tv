package com.iptv.tv.ui

import java.net.URLDecoder
import java.net.URLEncoder

fun String.encodeUrl(): String = URLEncoder.encode(this, "UTF-8").replace("+", "%20")
fun String.decodeUrl(): String = URLDecoder.decode(this, "UTF-8")
