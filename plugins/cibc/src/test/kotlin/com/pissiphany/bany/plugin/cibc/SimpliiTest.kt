package com.pissiphany.bany.adapter.plugin

import com.pissiphany.bany.adapter.Constants.CONFIG_FILE
import com.pissiphany.bany.config.BanyConfig
import com.squareup.moshi.Moshi
import okhttp3.*
import java.net.CookieManager
import java.net.CookiePolicy
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal class SimpliiTest {
    private val BASE_URL = "https://online.simplii.com"
    private val STATIC_URL = "$BASE_URL/static/3a60cf428e5192970126258a37cefd2"
    private val LOGIN_URL = "$BASE_URL/ebm-resources/public/client/web/index.html"
    private val AUTH_URL = "$BASE_URL/ebm-anp/api/v1/json/sessions"
    private val ACCOUNTS_URL = "$BASE_URL/ebm-ai/api/v1/json/accounts"
    private val TRANSACTIONS_URL = "$BASE_URL/ebm-ai/api/v1/json/transactions"

    private val static_1_body = """
        {"sensor_data":"7a74G7m23Vrp0o5c9051321.4-1,2,-94,-100,Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36,uaend,12147,20030107,en-US,Gecko,3,0,0,0,381702,7190489,1920,1177,1920,1200,914,1066,1920,,cpen:0,i1:0,dm:0,cwen:0,non:1,opc:0,fc:0,sc:0,wrc:1,isc:0,vib:1,bat:1,x11:0,x12:1,8969,0.824500456412,775668595244.5,loc:-1,2,-94,-101,do_en,dm_en,t_en-1,2,-94,-105,-1,2,-94,-102,-1,0,1,0,1238,1027,0;1,0,1,0,1244,-1,0;-1,2,-94,-108,0,1,1241,0,0,10,-1,0;1,2,1241,0,0,10,-1,0;2,1,1347,0,0,10,-1,0;3,2,1348,0,0,10,-1,0;-1,2,-94,-110,0,1,266,574,70;1,1,292,825,116;2,1,950,913,1;3,1,1024,913,4;4,1,1027,913,6;5,1,1040,913,9;6,1,12122,891,256;7,1,12129,877,268;8,1,12140,824,306;9,1,12157,741,346;10,1,12175,640,374;11,1,12191,557,383;12,1,12209,487,386;13,1,12224,441,386;14,1,12240,405,380;15,1,12257,390,373;16,1,12273,387,368;17,1,12291,387,365;18,1,12509,387,365;19,1,12524,387,367;20,1,12540,385,371;21,1,12558,372,380;22,1,12573,366,382;23,1,12591,338,383;24,1,12608,314,380;25,1,12624,297,373;26,1,12642,280,362;27,1,12658,267,349;28,1,12675,260,338;29,1,12695,257,330;30,1,12707,254,320;31,1,12724,254,315;32,1,12740,253,310;33,1,12757,253,307;34,1,12774,253,306;35,1,12791,253,305;36,1,12810,253,304;37,1,12824,254,304;38,1,12840,255,304;39,1,12858,256,304;40,1,12875,259,304;41,1,12890,262,304;42,1,12909,265,305;43,1,12924,268,307;44,1,12941,271,308;45,1,12958,274,308;46,1,12974,275,307;47,1,12991,277,305;48,1,13009,278,303;49,1,13023,278,300;50,1,13040,278,297;51,1,13057,278,295;52,1,13073,278,293;53,1,13090,277,291;54,1,13107,277,290;55,1,13125,277,288;56,1,13143,277,285;57,1,13158,277,282;58,1,13174,277,278;59,1,13191,277,276;60,1,13208,277,274;61,1,13225,277,273;62,1,13241,277,272;63,1,13258,277,271;64,1,13274,277,271;65,1,13291,277,270;66,1,13307,277,270;67,1,13324,277,270;68,1,13341,277,270;69,3,13634,277,270,1027;70,4,13745,277,270,1027;71,2,13749,277,270,1027;72,1,14888,278,270;73,1,14909,280,270;74,1,14925,282,271;75,1,14941,283,272;76,1,14958,284,273;77,1,14974,285,274;78,1,14991,285,275;79,1,15007,285,277;80,1,15024,285,279;81,1,15040,285,282;82,1,15059,285,287;83,1,15075,284,289;84,1,15090,284,291;85,1,15108,283,292;86,1,15124,283,294;87,1,15142,282,296;88,1,15158,281,298;89,1,15173,280,300;90,1,15191,279,302;91,1,15209,278,305;92,1,15225,277,308;93,1,15238,276,311;94,1,15256,275,314;95,1,15273,274,316;96,1,15289,273,317;97,1,15306,272,318;98,1,15322,272,318;99,1,15339,272,318;100,1,15355,271,318;101,1,15371,271,318;102,1,15388,270,318;114,3,15654,253,323,1244;115,4,15784,253,323,1244;116,2,15787,253,323,1244;580,3,2397111,261,352,1724;-1,2,-94,-117,-1,2,-94,-111,-1,2,-94,-109,-1,2,-94,-114,-1,2,-94,-103,2,2978;3,13632;2,19288;0,121882;1,132990;0,133567;1,137005;0,139551;1,142499;0,150802;1,153803;0,164573;1,175567;0,179390;1,186550;0,195328;1,280142;0,351960;1,451179;0,508306;1,641809;0,769632;1,770079;0,1475786;1,1480400;0,1486301;1,1495057;0,1497889;1,1518370;0,1519395;1,1520169;0,1520520;1,1521068;3,2397092;-1,2,-94,-112,https://online.cibc.com/ebm-resources/public/client/web/index.html#/signon-1,2,-94,-115,5226,3839790,0,0,0,0,3845015,2397111,0,1551337190489,4,16595,4,581,2765,5,0,2397113,3770139,1,2,50,301,-931491701,30261693-1,2,-94,-106,1,3-1,2,-94,-119,15,14,15,15,24,31,16,11,9,9,8,164,207,104,-1,2,-94,-122,0,0,0,0,1,0,0-1,2,-94,-123,-1,2,-94,-70,-872636394;dis;,7,8;true;true;true;480;true;24;24;true;false;1-1,2,-94,-80,4924-1,2,-94,-116,323572465-1,2,-94,-118,177364-1,2,-94,-121,;3;6;0"}
    """.trimIndent()

    private val static_2_body = """
        {"sensor_data":"7a74G7m23Vrp0o5c9051321.4-1,2,-94,-100,Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36,uaend,12147,20030107,en-US,Gecko,3,0,0,0,381702,7190489,1920,1177,1920,1200,914,1066,1920,,cpen:0,i1:0,dm:0,cwen:0,non:1,opc:0,fc:0,sc:0,wrc:1,isc:0,vib:1,bat:1,x11:0,x12:1,8969,0.942158160471,775668595244.5,loc:-1,2,-94,-101,do_en,dm_en,t_en-1,2,-94,-105,-1,2,-94,-102,-1,0,1,0,1238,1027,0;1,0,1,0,1244,-1,0;-1,2,-94,-108,0,1,1241,0,0,10,-1,0;1,2,1241,0,0,10,-1,0;2,1,1347,0,0,10,-1,0;3,2,1348,0,0,10,-1,0;-1,2,-94,-110,0,1,266,574,70;1,1,292,825,116;2,1,950,913,1;3,1,1024,913,4;4,1,1027,913,6;5,1,1040,913,9;6,1,12122,891,256;7,1,12129,877,268;8,1,12140,824,306;9,1,12157,741,346;10,1,12175,640,374;11,1,12191,557,383;12,1,12209,487,386;13,1,12224,441,386;14,1,12240,405,380;15,1,12257,390,373;16,1,12273,387,368;17,1,12291,387,365;18,1,12509,387,365;19,1,12524,387,367;20,1,12540,385,371;21,1,12558,372,380;22,1,12573,366,382;23,1,12591,338,383;24,1,12608,314,380;25,1,12624,297,373;26,1,12642,280,362;27,1,12658,267,349;28,1,12675,260,338;29,1,12695,257,330;30,1,12707,254,320;31,1,12724,254,315;32,1,12740,253,310;33,1,12757,253,307;34,1,12774,253,306;35,1,12791,253,305;36,1,12810,253,304;37,1,12824,254,304;38,1,12840,255,304;39,1,12858,256,304;40,1,12875,259,304;41,1,12890,262,304;42,1,12909,265,305;43,1,12924,268,307;44,1,12941,271,308;45,1,12958,274,308;46,1,12974,275,307;47,1,12991,277,305;48,1,13009,278,303;49,1,13023,278,300;50,1,13040,278,297;51,1,13057,278,295;52,1,13073,278,293;53,1,13090,277,291;54,1,13107,277,290;55,1,13125,277,288;56,1,13143,277,285;57,1,13158,277,282;58,1,13174,277,278;59,1,13191,277,276;60,1,13208,277,274;61,1,13225,277,273;62,1,13241,277,272;63,1,13258,277,271;64,1,13274,277,271;65,1,13291,277,270;66,1,13307,277,270;67,1,13324,277,270;68,1,13341,277,270;69,3,13634,277,270,1027;70,4,13745,277,270,1027;71,2,13749,277,270,1027;72,1,14888,278,270;73,1,14909,280,270;74,1,14925,282,271;75,1,14941,283,272;76,1,14958,284,273;77,1,14974,285,274;78,1,14991,285,275;79,1,15007,285,277;80,1,15024,285,279;81,1,15040,285,282;82,1,15059,285,287;83,1,15075,284,289;84,1,15090,284,291;85,1,15108,283,292;86,1,15124,283,294;87,1,15142,282,296;88,1,15158,281,298;89,1,15173,280,300;90,1,15191,279,302;91,1,15209,278,305;92,1,15225,277,308;93,1,15238,276,311;94,1,15256,275,314;95,1,15273,274,316;96,1,15289,273,317;97,1,15306,272,318;98,1,15322,272,318;99,1,15339,272,318;100,1,15355,271,318;101,1,15371,271,318;102,1,15388,270,318;114,3,15654,253,323,1244;115,4,15784,253,323,1244;116,2,15787,253,323,1244;580,3,2397111,261,352,1724;581,4,2397147,261,352,1724;582,2,2397154,261,352,1724;622,3,2398392,263,272,1027;-1,2,-94,-117,-1,2,-94,-111,-1,2,-94,-109,-1,2,-94,-114,-1,2,-94,-103,2,2978;3,13632;2,19288;0,121882;1,132990;0,133567;1,137005;0,139551;1,142499;0,150802;1,153803;0,164573;1,175567;0,179390;1,186550;0,195328;1,280142;0,351960;1,451179;0,508306;1,641809;0,769632;1,770079;0,1475786;1,1480400;0,1486301;1,1495057;0,1497889;1,1518370;0,1519395;1,1520169;0,1520520;1,1521068;3,2397092;-1,2,-94,-112,https://online.cibc.com/ebm-resources/public/client/web/index.html#/signon-1,2,-94,-115,5226,11036038,0,0,0,0,11041263,2398392,0,1551337190489,4,16595,4,623,2765,7,0,2398393,10962832,1,143B8BF0B752A1D3E328EB9F2D69B1FD1724032DB86C00005490775C63C01609~-1~VHewmAjovX65jAJ+5dHya1Y+CvbPACRAinaIjcm59KM=~-1~-1,8055,301,-931491701,30261693-1,2,-94,-106,1,4-1,2,-94,-119,15,14,15,15,24,31,16,11,9,9,8,164,207,104,-1,2,-94,-122,0,0,0,0,1,0,0-1,2,-94,-123,-1,2,-94,-70,-872636394;dis;,7,8;true;true;true;480;true;24;24;true;false;1-1,2,-94,-80,4924-1,2,-94,-116,323572465-1,2,-94,-118,189735-1,2,-94,-121,;2;6;0"}
    """.trimIndent()

    private data class AuthRequest(val card: Card, val password: String) {
        data class Card(val value: String, val description: String, val encrypted: Boolean, val encrypt: Boolean)
    }

//    @Test
    fun test() {
        val moshi = Moshi.Builder().build()
        val configAdapter = moshi.adapter(BanyConfig::class.java)
        val config = configAdapter.fromJson(CONFIG_FILE.readText())
            ?: throw Exception("unable to load config!")

        val credentials = config.plugins["simplii"]?.first() ?: return // no simplii data, terminate

        val client = OkHttpClient
            .Builder()
            .cookieJar(QuotePreservingCookieJar(CookieManager(null, CookiePolicy.ACCEPT_ALL)))
            .build()

         seedCookieJar(client)

        var token = ""
        val code: Int
        val message: String

        try {
            // authenticate
            token = authenticate(client, moshi, credentials.username, credentials.password) ?: throw Exception("no token found!")

            // get accounts
            val accounts = getAccounts(client, moshi, token)
                .also {
                    for ((number, account) in it) {
                        println("$number => ${account.id}")
                    }
                }

            val account = accounts[credentials.connections.first().thirdPartyAccountId] ?: return
            getTransactions(client, moshi, token, account)
                .also {
                    println("found ${it.size} transaction(s)")
                    for (t in it) {
                        println("${t.id} => ${t.date}, ${t.date}, ${t.descriptionLine1}, ${t.transactionDescription}")
                    }
                }
        } finally {
            // terminate session
            if (token.isNotBlank()) {
                val result = terminateSession(client, token)
                code = result.first
                message = result.second

                println()
                println("===")
                when (code) {
                    204 -> println("successfully terminated session")
                    else -> throw Exception("$code: unable to terminate session. $message")
                }
            }
        }
    }

    private fun seedCookieJar(client: OkHttpClient, payload: String = static_1_body, count: Int = 2) {
        if (count <= 0) return

        val mediaType = MediaType.parse("text/raw;charset=UTF-8")
        val body1 = RequestBody.create(mediaType, payload)

        val request = Request.Builder()
            .url(STATIC_URL)
            .post(body1)
            .addHeader("accept", "*/*")
            .addHeader("accept-language", "en-US,en;q=0.9")
            .addHeader("content-type", "text/raw;charset=UTF-8")
            .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0")
            .build()

        println()
        println("seed cookie jar request")
        println(request.toString())
        printHeaders(request.headers())

        println()
        println("seed cookie jar response")

        client.newCall(request).execute().use { response ->
            if (response.code() != 201) throw Exception("error!")
        }

        // Note: must call it a second time for some reason
        seedCookieJar(client, static_2_body, count - 1)
    }

    private fun authenticate(client: OkHttpClient, moshi: Moshi, cardNumber: String, password: String): String? {
        val authAdapter = moshi.adapter(AuthRequest::class.java)
        val json = authAdapter.toJson(
            AuthRequest(
                card = AuthRequest.Card(
                    value = cardNumber,
                    description = "",
                    encrypted = false,
                    encrypt = true
                ),
                password = password
            )
        )

        val mediaType = MediaType.parse("application/vnd.api+json")
        val body = RequestBody.create(mediaType, json)

        val request = Request.Builder()
            .url(AUTH_URL)
            .post(body)
//            .addHeader("Cookie", "bm_sz=1C94067C461C54DF09CB8D55956F46B6~YAAQ15o7F61B5J9oAQAAUVIfMQJjy+no0hYzkgCB8jKuyleE8Uk2Lr+PmMUMdPRts6iKpbcto2L3DFgbCsdc7ehpzm2zItTd/6GxlMHlOekiMeCVStl6gt9qzR0QD073A9DNnh6wUYf7L3B7Bt6ZnU/wBu8DvKQMutxi40KOz138Z0pnwkTiJrtKq+OhOWeW")

            .addHeader("host", "online.simplii.com")
            .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0")
            .addHeader("accept", "application/vnd.api+json")
            .addHeader("accept-language", "en")
            .addHeader("accept-encoding", "gzip, deflate, br")
            .addHeader("referer", "https://online.simplii.com/ebm-resources/public/client/web/index.html")
            .addHeader("client-type", "default_web")
            .addHeader("brand", "pcf")
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .addHeader("connection", "keep-alive")
            .addHeader("pragma", "no-cache")
            .addHeader("cache-control", "no-cache")

            .addHeader("Content-Type", "application/vnd.api+json")
            .addHeader("www-authenticate", "CardAndPassword")
            .addHeader("x-auth-token", "")
            .build()

        println()
        println("auth request")
        println(request.toString())
        printHeaders(request.headers())
        println(json)

        println()
        println("auth response")

        return client.newCall(request)
            .execute()
            .use { response ->
                println("code: ${response.code()}")
                printHeaders(response.headers())

                // return header or null
                response.header("x-auth-token")
            }
    }

    private fun getAccounts(client: OkHttpClient, moshi: Moshi, token: String) : Map<String, SimpliiAccountsWrapper.SimpliiAccount> {
        val request = Request.Builder()
            .url(ACCOUNTS_URL)
            .get()
            .addHeader("x-auth-token", token)
            .build()

        println()
        println("accounts request")
        println(request.toString())
        printHeaders(request.headers())

        println()
        println("accounts response")

        return client.newCall(request).execute().use(fun(response): Map<String, SimpliiAccountsWrapper.SimpliiAccount> {
            val json = response.body()?.string() ?: ""

            printHeaders(response.headers())
            println("body: $json")

            val adapter = moshi.adapter(SimpliiAccountsWrapper::class.java)
            return adapter.fromJson(json)
                ?.accounts
                ?.associate { account ->
                    account.number to account
                }
                ?: emptyMap()
        })
    }

    private fun getTransactions(
        client: OkHttpClient, moshi: Moshi, token: String, account: SimpliiAccountsWrapper.SimpliiAccount
    ): List<SimpliiTransactionWrapper.SimpliiTransaction> {
        val today = LocalDate.now()

        val url = HttpUrl.get(TRANSACTIONS_URL)
            .newBuilder()
            .addQueryParameter("accountId", account.id)
            .addQueryParameter("filterBy", "range")
            .addQueryParameter("lastFilterBy", "range")
            .addQueryParameter("fromDate", today.minusMonths(1L).format(DateTimeFormatter.ISO_LOCAL_DATE))
            .addQueryParameter("toDate", today.format(DateTimeFormatter.ISO_LOCAL_DATE))
            .addQueryParameter("sortAsc", "true")
            .addQueryParameter("sortByField", "date")

            // may not be needed
            .addQueryParameter("limit", "1000")
            .addQueryParameter("lowerLimitAmount", "")
            .addQueryParameter("upperLimitAmount", "")
            .addQueryParameter("offset", "0")
            .addQueryParameter("transactionType", "")

            .build()

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("x-auth-token", token)
            .build()

        println()
        println("transactions request")
        println(request.toString())
        printHeaders(request.headers())

        println()
        println("transactions response")

        return client.newCall(request).execute().use(fun(response): List<SimpliiTransactionWrapper.SimpliiTransaction> {
            val json = response.body()?.string() ?: ""

            printHeaders(response.headers())
            println("body: $json")

            val adapter = moshi.adapter(SimpliiTransactionWrapper::class.java)
            return adapter.fromJson(json)
                ?.transactions
                ?: emptyList()
        })
    }

    private fun terminateSession(client: OkHttpClient, token: String): Pair<Int, String> {
        val request = Request.Builder()
            .url(AUTH_URL)
            .delete()
            .addHeader("x-auth-token", token)
            .build()

        println()
        println("terminate session request")
        println(request.toString())
        printHeaders(request.headers())

        println()
        println("terminate session response")

        return client.newCall(request).execute().use { response ->
            printHeaders(response.headers())
            response.code() to response.message()
        }
    }

    private fun printHeaders(headers: Headers) {
        for (name in headers.names()) {
            println("header '$name': ${headers[name]}")
        }
    }

    data class SimpliiAccountsWrapper(val accounts: List<SimpliiAccount> = emptyList()) {
        data class SimpliiAccount(val id: String, val number: String, val balance: SimpliiAccountBalance)
        data class SimpliiAccountBalance(val currency: String, val amount: String)
    }

    data class SimpliiTransactionWrapper(val transactions: List<SimpliiTransaction> = emptyList()) {
        data class SimpliiTransaction(
            val id: String,
            val accountId: String,
            val date: String,
            val descriptionLine1: String,
            val transactionDescription: String,
            val credit: String,
            val debit: String,
            val transactionType: TransactionType
        )
        enum class TransactionType {
            DEP, XFR, PAY, POS, INT, CHQ, CRE
        }
    }
}