package com.pissiphany.bany.plugin.equitable.client

import org.jsoup.Connection
import java.net.URL

private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36"

internal fun URL.addToPath(vararg parts: String) = URL(this, parts.joinToString("/")).toString()

internal fun Connection.execute(expected: Int = 200, err: (code: Int, msg: String) -> String): Connection.Response {
    return this
        .userAgent(USER_AGENT)
        .execute()
        .also { res ->
            check(res.statusCode() == expected) { err(res.statusCode(), res.statusMessage()) }
        }
}
