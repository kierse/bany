package com.pissiphany.bany.adapter.plugin

import com.pissiphany.bany.adapter.Constants.CONFIG_FILE
import com.pissiphany.bany.config.BanyConfig
import com.squareup.moshi.Moshi
import okhttp3.*
import java.net.CookieManager
import java.net.CookiePolicy
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal class CibcTest {
    private val BASE_URL = "https://www.cibconline.cibc.com"
    private val STATIC_URL = "$BASE_URL/public/66b6b4bfb218b5ab63ab8a0b4633c"
    private val AUTH_URL = "$BASE_URL/ebm-anp/api/v1/json/sessions"
    private val ACCOUNTS_URL = "$BASE_URL/ebm-ai/api/v2/json/accounts"
    private val TRANSACTIONS_URL = "$BASE_URL/ebm-ai/api/v1/json/transactions"

    private val userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36"
    private val contentType = "application/vnd.api+json"

    private val static_1_body = """
        {"sensor_data":"7a74G7m23Vrp0o5c9062231.4-1,2,-94,-100,Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36,uaend,12147,20030107,en-US,Gecko,3,0,0,0,381783,5020145,1920,1177,1920,1200,1071,1066,1920,,cpen:0,i1:0,dm:0,cwen:0,non:1,opc:0,fc:0,sc:0,wrc:1,isc:0,vib:1,bat:1,x11:0,x12:1,8969,0.06738261733,775832510072.5,loc:-1,2,-94,-101,do_en,dm_en,t_en-1,2,-94,-105,-1,2,-94,-102,-1,0,1,0,1237,1027,0;1,0,1,0,1243,-1,0;-1,2,-94,-108,0,1,1725,0,0,10,-1,0;1,2,1726,0,0,10,-1,0;2,1,1761,0,0,10,-1,0;3,2,1761,0,0,10,-1,0;-1,2,-94,-110,0,1,2553,675,515;1,1,2569,646,510;2,1,2588,601,490;3,1,2604,546,458;4,1,2619,442,388;5,1,2636,382,334;6,1,2654,347,296;7,1,2670,330,273;8,1,2687,323,261;9,1,2706,318,250;10,1,2719,318,249;11,1,2844,318,249;12,1,2853,318,252;13,1,2869,318,257;14,1,2887,317,259;15,1,2902,315,261;16,1,2921,313,262;17,1,2936,311,263;18,1,2953,309,263;19,1,2969,307,263;20,1,2986,306,262;21,1,3004,305,260;22,1,3019,303,259;23,1,3035,302,259;24,1,3052,301,258;25,1,3069,301,258;26,1,3086,301,258;27,1,3101,301,258;28,1,3122,301,257;29,1,3136,301,257;30,1,3153,301,255;31,1,3170,301,254;32,1,3186,301,252;33,1,3202,300,250;34,1,3219,300,248;35,1,3236,299,246;36,1,3253,299,245;37,1,3268,299,244;38,1,3285,299,243;39,1,3302,299,242;40,1,3318,299,241;41,1,3336,300,240;42,1,4095,302,240;43,1,4103,317,238;44,1,4119,338,233;45,1,4137,526,205;46,1,4154,743,196;47,1,4169,860,196;48,1,4185,1003,196;49,1,5378,1062,151;50,1,5385,1030,193;51,1,5401,990,234;52,1,5424,756,388;53,1,5437,598,449;54,1,5452,449,485;55,1,5471,361,496;56,1,5487,310,496;57,1,5502,283,490;58,1,5519,259,475;59,1,5537,257,464;60,1,5711,257,445;61,1,5727,258,421;62,1,5735,259,408;63,1,5754,265,383;64,1,5770,269,370;65,1,5786,288,310;66,1,5803,295,291;67,1,5820,309,260;68,1,5839,313,251;69,1,5852,316,247;70,1,5869,320,242;71,1,5885,322,241;72,1,5902,322,240;73,1,5919,324,239;74,1,5935,325,239;75,1,5952,326,239;76,1,5970,326,239;77,1,6019,324,239;78,1,6035,323,239;79,1,6052,319,239;80,1,6068,317,240;81,1,6087,313,242;82,1,6102,312,243;83,1,6120,310,244;84,1,6135,309,244;85,1,6151,309,245;86,1,6168,309,245;87,1,6194,308,246;88,3,6415,308,246,1027;89,4,6519,308,246,1027;90,2,6523,308,246,1027;91,1,7378,318,246;92,1,7394,350,242;93,1,7411,458,231;94,1,7419,536,228;95,1,7437,799,230;96,1,7453,980,264;97,1,7469,1069,282;98,1,19711,804,35;99,1,20749,804,35;100,1,20752,806,40;101,1,20770,807,41;102,1,20786,808,41;224,3,1225694,311,246,1027;-1,2,-94,-117,-1,2,-94,-111,-1,2,-94,-109,-1,2,-94,-114,-1,2,-94,-103,2,5050;3,6414;2,8281;3,1225673;-1,2,-94,-112,https://www.cibconline.cibc.com/ebm-resources/public/banking/cibc/client/web/index.html#/signon-1,2,-94,-115,7022,1856953,0,0,0,0,1863974,1225694,0,1551665020145,4,16599,4,225,2766,3,0,1225697,1786845,1,2,50,126,1372775405,30261693-1,2,-94,-106,1,2-1,2,-94,-119,7,9,10,9,22,25,13,8,7,7,7,6,11,100,-1,2,-94,-122,0,0,0,0,1,0,0-1,2,-94,-123,-1,2,-94,-70,-872636394;dis;,7,8;true;true;true;480;true;24;24;true;false;1-1,2,-94,-80,4924-1,2,-94,-116,25100754-1,2,-94,-118,156290-1,2,-94,-121,;4;8;0"}
    """.trimIndent()

    private val static_2_body = """
        {"sensor_data":"7a74G7m23Vrp0o5c9062231.4-1,2,-94,-100,Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36,uaend,12147,20030107,en-US,Gecko,3,0,0,0,381783,5020145,1920,1177,1920,1200,1071,1066,1920,,cpen:0,i1:0,dm:0,cwen:0,non:1,opc:0,fc:0,sc:0,wrc:1,isc:0,vib:1,bat:1,x11:0,x12:1,8969,0.0116984955,775832510072.5,loc:-1,2,-94,-101,do_en,dm_en,t_en-1,2,-94,-105,-1,2,-94,-102,-1,0,1,0,1237,1027,0;1,0,1,0,1243,-1,0;-1,2,-94,-108,0,1,1725,0,0,10,-1,0;1,2,1726,0,0,10,-1,0;2,1,1761,0,0,10,-1,0;3,2,1761,0,0,10,-1,0;-1,2,-94,-110,0,1,2553,675,515;1,1,2569,646,510;2,1,2588,601,490;3,1,2604,546,458;4,1,2619,442,388;5,1,2636,382,334;6,1,2654,347,296;7,1,2670,330,273;8,1,2687,323,261;9,1,2706,318,250;10,1,2719,318,249;11,1,2844,318,249;12,1,2853,318,252;13,1,2869,318,257;14,1,2887,317,259;15,1,2902,315,261;16,1,2921,313,262;17,1,2936,311,263;18,1,2953,309,263;19,1,2969,307,263;20,1,2986,306,262;21,1,3004,305,260;22,1,3019,303,259;23,1,3035,302,259;24,1,3052,301,258;25,1,3069,301,258;26,1,3086,301,258;27,1,3101,301,258;28,1,3122,301,257;29,1,3136,301,257;30,1,3153,301,255;31,1,3170,301,254;32,1,3186,301,252;33,1,3202,300,250;34,1,3219,300,248;35,1,3236,299,246;36,1,3253,299,245;37,1,3268,299,244;38,1,3285,299,243;39,1,3302,299,242;40,1,3318,299,241;41,1,3336,300,240;42,1,4095,302,240;43,1,4103,317,238;44,1,4119,338,233;45,1,4137,526,205;46,1,4154,743,196;47,1,4169,860,196;48,1,4185,1003,196;49,1,5378,1062,151;50,1,5385,1030,193;51,1,5401,990,234;52,1,5424,756,388;53,1,5437,598,449;54,1,5452,449,485;55,1,5471,361,496;56,1,5487,310,496;57,1,5502,283,490;58,1,5519,259,475;59,1,5537,257,464;60,1,5711,257,445;61,1,5727,258,421;62,1,5735,259,408;63,1,5754,265,383;64,1,5770,269,370;65,1,5786,288,310;66,1,5803,295,291;67,1,5820,309,260;68,1,5839,313,251;69,1,5852,316,247;70,1,5869,320,242;71,1,5885,322,241;72,1,5902,322,240;73,1,5919,324,239;74,1,5935,325,239;75,1,5952,326,239;76,1,5970,326,239;77,1,6019,324,239;78,1,6035,323,239;79,1,6052,319,239;80,1,6068,317,240;81,1,6087,313,242;82,1,6102,312,243;83,1,6120,310,244;84,1,6135,309,244;85,1,6151,309,245;86,1,6168,309,245;87,1,6194,308,246;88,3,6415,308,246,1027;89,4,6519,308,246,1027;90,2,6523,308,246,1027;91,1,7378,318,246;92,1,7394,350,242;93,1,7411,458,231;94,1,7419,536,228;95,1,7437,799,230;96,1,7453,980,264;97,1,7469,1069,282;98,1,19711,804,35;99,1,20749,804,35;100,1,20752,806,40;101,1,20770,807,41;102,1,20786,808,41;224,3,1225694,311,246,1027;225,4,1225762,311,246,1027;226,2,1225764,311,246,1027;250,3,1227094,278,356,683;-1,2,-94,-117,-1,2,-94,-111,-1,2,-94,-109,-1,2,-94,-114,-1,2,-94,-103,2,5050;3,6414;2,8281;3,1225673;-1,2,-94,-112,https://www.cibconline.cibc.com/ebm-resources/public/banking/cibc/client/web/index.html#/signon-1,2,-94,-115,7022,5538031,0,0,0,0,5545052,1227094,0,1551665020145,4,16599,4,251,2766,5,0,1227096,5465465,1,7F97F82FDAD900BD49A3E0F45A4E1BACB81C3267BF5100005C8C7C5C8C33B336~-1~VEHDESjKvMEm39oDWGMHi9KTKpUSChKy1yeV/JvUYj0=~-1~-1,8140,126,1372775405,30261693-1,2,-94,-106,1,3-1,2,-94,-119,7,9,10,9,22,25,13,8,7,7,7,6,11,100,-1,2,-94,-122,0,0,0,0,1,0,0-1,2,-94,-123,-1,2,-94,-70,-872636394;dis;,7,8;true;true;true;480;true;24;24;true;false;1-1,2,-94,-80,4924-1,2,-94,-116,25100754-1,2,-94,-118,168430-1,2,-94,-121,;3;8;0"}
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

        val credentials = config.plugins["cibc"]?.first() ?: return

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

//            // get accounts
//            val accounts = getAccounts(client, moshi, token)
//                .also {
//                    for ((number, account) in it) {
//                        println("$number => ${account.id}")
//                    }
//                }
//
//            val account = accounts[plugin.connections.first().thirdPartyAccountId] ?: return
//            getTransactions(client, moshi, token, account)
//                .also {
//                    println("found ${it.size} transaction(s)")
//                    for (t in it) {
//                        println("${t.id} => ${t.date}, ${t.date}, ${t.descriptionLine1}, ${t.transactionDescription}")
//                    }
//                }
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
//            .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0")
            .addHeader("user-agent", userAgent)
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

//    private fun seedCookieJar2() {
//        WebClient(BrowserVersion.BEST_SUPPORTED).use { client ->
//            client.options.isThrowExceptionOnScriptError = false
//            val page: HtmlPage = client.getPage("https://www.cibconline.cibc.com/ebm-resources/public/banking/cibc/client/web/index.html")
//
////            val foo = page.getByXPath<HtmlInput>("//div[@class='row card-entry']")
////            val foo = page.getByXPath<HtmlInput>("//div[contains(@class, 'card-entry')]")
////            println(foo.size)
//
//
////            val cardNumberField = page.getByXPath<HtmlInput>("//div[@class='card-entry']/descendant::input[@name='cardNumber']").first()
////            println(cardNumberField.nameAttribute)
//
////            for (form in page.forms) {
////                println("in")
////                val cardNumberField: HtmlInput = form.getInputByName("cardNumber")
////                println(cardNumberField.nameAttribute)
////            }
//        }
//    }

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
//            .addHeader("Cookie", "bm_sz=921D8FBCD959733158A30BB94E285968~YAAQxtTCF9aWjq9oAQAAsrs9RQLUs2Jsulp2gTuKQX1R3qo8NkiHmNntUBIEZCDnWhrp6s7R+bYgrqLmQjXhWbzya1wEF7+v/uphl24jCPiIzyPpvV/+FUn0ZeJC5ziCPNFm28ipmuc8wdRTOe+hTQkm/ODhsoPl2BNSEftbHIfkdbb1CV+MBffq+cnnpg==")
//            .addHeader("Cookie", "_abck=98217E04D5008702B6277B27B2B3C62317C2D4C6E0310000C3387C5C9A2F9B07~0~Je+CCELbqLUGP6RiGNJ8HKCxlmTPo4cjcpWTwQ+ASdo=~-1~-1")
//            .addHeader("Cookie", "ak_bmsc=7007316E9AB7E7491D2B59E25CE97E8A17C2D4C6E0310000C3387C5CD6578941~plRhwbXD15lHZeiVivnuEMCQsft1hb5ZAVIqCqSOTdtOK7LlYgkpgg8/roIvGxL+zJGzGX77ImCIMYHNtThUDKQYHBP/EMJmo6sx3Qce9zdZBEKkt4zCYoLphoNdn3hAyQMisq5AQuWZOVyDIut++h3WhA/sYl8NpD3qu9qtz8RFPtlVEcdlC0OM2YiudWVPFGcah58XtGCRGO6kaO4+4Vl0DZ1H0xLN5CkQ0oIgV5FgY=")
//            .addHeader("Cookie", "s_gpv_pn=cibc%3Epb%3Epersonal-banking")
//            .addHeader("Cookie", "s_ppv=cibc%253Epb%253Epersonal-banking%2C30%2C30%2C1066%2C1071%2C1066%2C1920%2C1200%2C2%2CP")
//            .addHeader("Cookie", "s_ppvl=%5B%5BB%5D%5D")

            .addHeader("host", "www.cibconline.cibc.com")
//            .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0")
//            .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36")
            .addHeader("accept", contentType)
            .addHeader("accept-encoding", "gzip, deflate, br")
            .addHeader("accept-language", "en")
            .addHeader("brand", "cibc")
//            .addHeader("cache-control", "no-cache")
            .addHeader("client-type", "default_web")
            .addHeader("content-type", contentType)
//            .addHeader("connection", "keep-alive")
            .addHeader("dnt", "1")
//            .addHeader("pragma", "no-cache")
            .addHeader("origin", BASE_URL)
            .addHeader("referer", "https://www.cibconline.cibc.com/ebm-resources/public/banking/cibc/client/web/index.html")
            .addHeader("user-agent", userAgent)
            .addHeader("www-authenticate", "CardAndPassword")
            .addHeader("x-auth-token", "")
            .addHeader("x-requested-with", "XMLHttpRequest")
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