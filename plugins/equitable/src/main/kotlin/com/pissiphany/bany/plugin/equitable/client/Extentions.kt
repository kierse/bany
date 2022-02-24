package com.pissiphany.bany.plugin.equitable.client

import okhttp3.*
import org.jsoup.Connection
import org.jsoup.nodes.Element

internal fun List<Connection.KeyVal>.toRequestBody() = with(FormBody.Builder()) {
    forEach { keyVal -> add(keyVal.key(), keyVal.value()) }
    build()
}

internal fun Request.Builder.cookies(cookies: Cookies): Request.Builder = apply {
    addHeader("Cookie", cookies.joinToString(";"))
}

internal fun Response.cookies(): Cookies = headers.values("Set-Cookie")

internal var Element.value: String
    get() = `val`()
    set(new) { `val`(new) }