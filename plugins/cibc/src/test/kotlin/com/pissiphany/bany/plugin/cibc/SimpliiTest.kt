package com.pissiphany.bany.plugin.cibc

import com.squareup.moshi.Moshi
import okhttp3.*
import org.junit.jupiter.api.Test
import java.io.File
import java.net.CookieManager
import java.net.CookiePolicy
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal class SimpliiTest {
    private val BASE_URL = "https://online.simplii.com"
    private val STATIC_URL = "$BASE_URL/static/e677a8bd0c8192970126258a37cefd2" // changed
    private val LOGIN_URL = "$BASE_URL/ebm-resources/public/client/web/index.html"
    private val AUTH_URL = "$BASE_URL/ebm-anp/api/v1/json/sessions"
    private val ACCOUNTS_URL = "$BASE_URL/ebm-ai/api/v1/json/accounts"
    private val TRANSACTIONS_URL = "$BASE_URL/ebm-ai/api/v1/json/transactions"

    // changed
//    {"sensor_data":"7a74G7m23Vrp0o5c9051321.4-1,2,-94,-100,Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36,uaend,12147,20030107,en-US,Gecko,3,0,0,0,381702,7190489,1920,1177,1920,1200,914,1066,1920,,cpen:0,i1:0,dm:0,cwen:0,non:1,opc:0,fc:0,sc:0,wrc:1,isc:0,vib:1,bat:1,x11:0,x12:1,8969,0.824500456412,775668595244.5,loc:-1,2,-94,-101,do_en,dm_en,t_en-1,2,-94,-105,-1,2,-94,-102,-1,0,1,0,1238,1027,0;1,0,1,0,1244,-1,0;-1,2,-94,-108,0,1,1241,0,0,10,-1,0;1,2,1241,0,0,10,-1,0;2,1,1347,0,0,10,-1,0;3,2,1348,0,0,10,-1,0;-1,2,-94,-110,0,1,266,574,70;1,1,292,825,116;2,1,950,913,1;3,1,1024,913,4;4,1,1027,913,6;5,1,1040,913,9;6,1,12122,891,256;7,1,12129,877,268;8,1,12140,824,306;9,1,12157,741,346;10,1,12175,640,374;11,1,12191,557,383;12,1,12209,487,386;13,1,12224,441,386;14,1,12240,405,380;15,1,12257,390,373;16,1,12273,387,368;17,1,12291,387,365;18,1,12509,387,365;19,1,12524,387,367;20,1,12540,385,371;21,1,12558,372,380;22,1,12573,366,382;23,1,12591,338,383;24,1,12608,314,380;25,1,12624,297,373;26,1,12642,280,362;27,1,12658,267,349;28,1,12675,260,338;29,1,12695,257,330;30,1,12707,254,320;31,1,12724,254,315;32,1,12740,253,310;33,1,12757,253,307;34,1,12774,253,306;35,1,12791,253,305;36,1,12810,253,304;37,1,12824,254,304;38,1,12840,255,304;39,1,12858,256,304;40,1,12875,259,304;41,1,12890,262,304;42,1,12909,265,305;43,1,12924,268,307;44,1,12941,271,308;45,1,12958,274,308;46,1,12974,275,307;47,1,12991,277,305;48,1,13009,278,303;49,1,13023,278,300;50,1,13040,278,297;51,1,13057,278,295;52,1,13073,278,293;53,1,13090,277,291;54,1,13107,277,290;55,1,13125,277,288;56,1,13143,277,285;57,1,13158,277,282;58,1,13174,277,278;59,1,13191,277,276;60,1,13208,277,274;61,1,13225,277,273;62,1,13241,277,272;63,1,13258,277,271;64,1,13274,277,271;65,1,13291,277,270;66,1,13307,277,270;67,1,13324,277,270;68,1,13341,277,270;69,3,13634,277,270,1027;70,4,13745,277,270,1027;71,2,13749,277,270,1027;72,1,14888,278,270;73,1,14909,280,270;74,1,14925,282,271;75,1,14941,283,272;76,1,14958,284,273;77,1,14974,285,274;78,1,14991,285,275;79,1,15007,285,277;80,1,15024,285,279;81,1,15040,285,282;82,1,15059,285,287;83,1,15075,284,289;84,1,15090,284,291;85,1,15108,283,292;86,1,15124,283,294;87,1,15142,282,296;88,1,15158,281,298;89,1,15173,280,300;90,1,15191,279,302;91,1,15209,278,305;92,1,15225,277,308;93,1,15238,276,311;94,1,15256,275,314;95,1,15273,274,316;96,1,15289,273,317;97,1,15306,272,318;98,1,15322,272,318;99,1,15339,272,318;100,1,15355,271,318;101,1,15371,271,318;102,1,15388,270,318;114,3,15654,253,323,1244;115,4,15784,253,323,1244;116,2,15787,253,323,1244;580,3,2397111,261,352,1724;-1,2,-94,-117,-1,2,-94,-111,-1,2,-94,-109,-1,2,-94,-114,-1,2,-94,-103,2,2978;3,13632;2,19288;0,121882;1,132990;0,133567;1,137005;0,139551;1,142499;0,150802;1,153803;0,164573;1,175567;0,179390;1,186550;0,195328;1,280142;0,351960;1,451179;0,508306;1,641809;0,769632;1,770079;0,1475786;1,1480400;0,1486301;1,1495057;0,1497889;1,1518370;0,1519395;1,1520169;0,1520520;1,1521068;3,2397092;-1,2,-94,-112,https://online.cibc.com/ebm-resources/public/client/web/index.html#/signon-1,2,-94,-115,5226,3839790,0,0,0,0,3845015,2397111,0,1551337190489,4,16595,4,581,2765,5,0,2397113,3770139,1,2,50,301,-931491701,30261693-1,2,-94,-106,1,3-1,2,-94,-119,15,14,15,15,24,31,16,11,9,9,8,164,207,104,-1,2,-94,-122,0,0,0,0,1,0,0-1,2,-94,-123,-1,2,-94,-70,-872636394;dis;,7,8;true;true;true;480;true;24;24;true;false;1-1,2,-94,-80,4924-1,2,-94,-116,323572465-1,2,-94,-118,177364-1,2,-94,-121,;3;6;0"}
    private val static_1_body = """
        {"sensor_data":"7a74G7m23Vrp0o5c9064901.4-1,2,-94,-100,Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36,uaend,12147,20030107,en-US,Gecko,3,0,0,0,382020,8533047,1920,1177,1920,1200,1071,1066,1920,,cpen:0,i1:0,dm:0,cwen:0,non:1,opc:0,fc:0,sc:0,wrc:1,isc:0,vib:1,bat:1,x11:0,x12:1,8969,0.538699813269,776314266523,loc:-1,2,-94,-101,do_en,dm_en,t_en-1,2,-94,-105,-1,2,-94,-102,-1,0,0,0,1238,1027,0;1,0,0,0,1244,-1,0;-1,2,-94,-108,0,1,3514,18,0,1,-1;1,1,3561,91,0,3,-1;2,1,3778,73,0,3,-1;3,2,3953,18,0,2,-1;4,2,3965,-2,0,0,-1;-1,2,-94,-110,0,1,2913,1561,18;1,1,2918,1561,18;2,1,2936,1561,54;3,1,2951,1566,84;4,1,2968,1577,115;5,1,2985,1582,121;6,1,3003,1588,125;7,1,3019,1590,125;8,1,3173,1589,127;9,1,3185,1586,135;10,1,3201,1580,150;11,1,3218,1575,164;12,1,3235,1571,176;13,1,3252,1565,196;14,1,3268,1564,198;15,1,15657,1064,84;16,1,15668,1056,90;17,1,15685,1026,108;18,1,15703,950,138;19,1,15718,867,156;20,1,15737,709,176;21,1,15753,628,185;22,1,15769,536,199;23,1,15785,490,206;24,1,15802,463,211;25,1,15820,449,214;26,1,15836,439,216;27,1,15852,436,216;28,1,16012,436,216;29,1,16018,435,219;30,1,16037,430,223;31,1,16052,424,226;32,1,16069,419,228;33,1,16086,411,230;34,1,16102,400,233;35,1,16119,389,235;36,1,16135,382,237;37,1,16153,375,239;38,1,16169,368,242;39,1,16185,365,243;40,1,16202,362,244;41,1,16220,359,245;42,1,16236,357,247;43,1,16252,355,249;44,1,16268,353,251;45,1,16285,352,252;46,1,16301,350,254;47,1,16318,346,256;48,1,16336,344,258;49,1,16355,342,260;50,1,16368,341,262;51,1,16385,340,263;52,1,16402,340,263;53,1,16418,339,264;54,1,16435,339,265;55,1,16452,338,265;56,1,16469,338,266;57,1,16485,338,266;58,1,16503,338,266;59,1,16519,337,266;60,1,16535,337,266;61,1,16552,337,267;62,1,16569,336,267;63,1,16586,335,268;64,1,16601,334,270;65,1,16620,334,271;66,1,16635,333,271;67,1,16651,333,272;68,3,17258,333,272,1027;-1,2,-94,-117,-1,2,-94,-111,-1,2,-94,-109,-1,2,-94,-114,-1,2,-94,-103,2,4042;3,17255;-1,2,-94,-112,https://online.simplii.com/ebm-resources/public/client/web/index.html#/signon-1,2,-94,-115,18991,985625,0,0,0,0,1004615,17258,0,1552628533046,5,16609,5,69,2768,1,0,17260,940134,1,2,50,818,883658489,30261693-1,2,-94,-106,1,1-1,2,-94,-119,23,26,27,16,25,32,16,11,9,8,7,207,217,149,-1,2,-94,-122,0,0,0,0,1,0,0-1,2,-94,-123,-1,2,-94,-70,-872636394;dis;,7,8;true;true;true;420;true;24;24;true;false;1-1,2,-94,-80,4918-1,2,-94,-116,42665187-1,2,-94,-118,124582-1,2,-94,-121,;3;7;0"}
    """.trimIndent()

    // changed
//    {"sensor_data":"7a74G7m23Vrp0o5c9051321.4-1,2,-94,-100,Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36,uaend,12147,20030107,en-US,Gecko,3,0,0,0,381702,7190489,1920,1177,1920,1200,914,1066,1920,,cpen:0,i1:0,dm:0,cwen:0,non:1,opc:0,fc:0,sc:0,wrc:1,isc:0,vib:1,bat:1,x11:0,x12:1,8969,0.942158160471,775668595244.5,loc:-1,2,-94,-101,do_en,dm_en,t_en-1,2,-94,-105,-1,2,-94,-102,-1,0,1,0,1238,1027,0;1,0,1,0,1244,-1,0;-1,2,-94,-108,0,1,1241,0,0,10,-1,0;1,2,1241,0,0,10,-1,0;2,1,1347,0,0,10,-1,0;3,2,1348,0,0,10,-1,0;-1,2,-94,-110,0,1,266,574,70;1,1,292,825,116;2,1,950,913,1;3,1,1024,913,4;4,1,1027,913,6;5,1,1040,913,9;6,1,12122,891,256;7,1,12129,877,268;8,1,12140,824,306;9,1,12157,741,346;10,1,12175,640,374;11,1,12191,557,383;12,1,12209,487,386;13,1,12224,441,386;14,1,12240,405,380;15,1,12257,390,373;16,1,12273,387,368;17,1,12291,387,365;18,1,12509,387,365;19,1,12524,387,367;20,1,12540,385,371;21,1,12558,372,380;22,1,12573,366,382;23,1,12591,338,383;24,1,12608,314,380;25,1,12624,297,373;26,1,12642,280,362;27,1,12658,267,349;28,1,12675,260,338;29,1,12695,257,330;30,1,12707,254,320;31,1,12724,254,315;32,1,12740,253,310;33,1,12757,253,307;34,1,12774,253,306;35,1,12791,253,305;36,1,12810,253,304;37,1,12824,254,304;38,1,12840,255,304;39,1,12858,256,304;40,1,12875,259,304;41,1,12890,262,304;42,1,12909,265,305;43,1,12924,268,307;44,1,12941,271,308;45,1,12958,274,308;46,1,12974,275,307;47,1,12991,277,305;48,1,13009,278,303;49,1,13023,278,300;50,1,13040,278,297;51,1,13057,278,295;52,1,13073,278,293;53,1,13090,277,291;54,1,13107,277,290;55,1,13125,277,288;56,1,13143,277,285;57,1,13158,277,282;58,1,13174,277,278;59,1,13191,277,276;60,1,13208,277,274;61,1,13225,277,273;62,1,13241,277,272;63,1,13258,277,271;64,1,13274,277,271;65,1,13291,277,270;66,1,13307,277,270;67,1,13324,277,270;68,1,13341,277,270;69,3,13634,277,270,1027;70,4,13745,277,270,1027;71,2,13749,277,270,1027;72,1,14888,278,270;73,1,14909,280,270;74,1,14925,282,271;75,1,14941,283,272;76,1,14958,284,273;77,1,14974,285,274;78,1,14991,285,275;79,1,15007,285,277;80,1,15024,285,279;81,1,15040,285,282;82,1,15059,285,287;83,1,15075,284,289;84,1,15090,284,291;85,1,15108,283,292;86,1,15124,283,294;87,1,15142,282,296;88,1,15158,281,298;89,1,15173,280,300;90,1,15191,279,302;91,1,15209,278,305;92,1,15225,277,308;93,1,15238,276,311;94,1,15256,275,314;95,1,15273,274,316;96,1,15289,273,317;97,1,15306,272,318;98,1,15322,272,318;99,1,15339,272,318;100,1,15355,271,318;101,1,15371,271,318;102,1,15388,270,318;114,3,15654,253,323,1244;115,4,15784,253,323,1244;116,2,15787,253,323,1244;580,3,2397111,261,352,1724;581,4,2397147,261,352,1724;582,2,2397154,261,352,1724;622,3,2398392,263,272,1027;-1,2,-94,-117,-1,2,-94,-111,-1,2,-94,-109,-1,2,-94,-114,-1,2,-94,-103,2,2978;3,13632;2,19288;0,121882;1,132990;0,133567;1,137005;0,139551;1,142499;0,150802;1,153803;0,164573;1,175567;0,179390;1,186550;0,195328;1,280142;0,351960;1,451179;0,508306;1,641809;0,769632;1,770079;0,1475786;1,1480400;0,1486301;1,1495057;0,1497889;1,1518370;0,1519395;1,1520169;0,1520520;1,1521068;3,2397092;-1,2,-94,-112,https://online.cibc.com/ebm-resources/public/client/web/index.html#/signon-1,2,-94,-115,5226,11036038,0,0,0,0,11041263,2398392,0,1551337190489,4,16595,4,623,2765,7,0,2398393,10962832,1,143B8BF0B752A1D3E328EB9F2D69B1FD1724032DB86C00005490775C63C01609~-1~VHewmAjovX65jAJ+5dHya1Y+CvbPACRAinaIjcm59KM=~-1~-1,8055,301,-931491701,30261693-1,2,-94,-106,1,4-1,2,-94,-119,15,14,15,15,24,31,16,11,9,9,8,164,207,104,-1,2,-94,-122,0,0,0,0,1,0,0-1,2,-94,-123,-1,2,-94,-70,-872636394;dis;,7,8;true;true;true;480;true;24;24;true;false;1-1,2,-94,-80,4924-1,2,-94,-116,323572465-1,2,-94,-118,189735-1,2,-94,-121,;2;6;0"}
    private val static_2_body = """
        {"sensor_data":"7a74G7m23Vrp0o5c9064901.4-1,2,-94,-100,Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36,uaend,12147,20030107,en-US,Gecko,3,0,0,0,382020,8533047,1920,1177,1920,1200,1071,1066,1920,,cpen:0,i1:0,dm:0,cwen:0,non:1,opc:0,fc:0,sc:0,wrc:1,isc:0,vib:1,bat:1,x11:0,x12:1,8969,0.432596189216,776314266523,loc:-1,2,-94,-101,do_en,dm_en,t_en-1,2,-94,-105,-1,2,-94,-102,-1,0,0,0,1238,1027,0;1,0,0,0,1244,-1,0;-1,2,-94,-108,0,1,3514,18,0,1,-1;1,1,3561,91,0,3,-1;2,1,3778,73,0,3,-1;3,2,3953,18,0,2,-1;4,2,3965,-2,0,0,-1;-1,2,-94,-110,0,1,2913,1561,18;1,1,2918,1561,18;2,1,2936,1561,54;3,1,2951,1566,84;4,1,2968,1577,115;5,1,2985,1582,121;6,1,3003,1588,125;7,1,3019,1590,125;8,1,3173,1589,127;9,1,3185,1586,135;10,1,3201,1580,150;11,1,3218,1575,164;12,1,3235,1571,176;13,1,3252,1565,196;14,1,3268,1564,198;15,1,15657,1064,84;16,1,15668,1056,90;17,1,15685,1026,108;18,1,15703,950,138;19,1,15718,867,156;20,1,15737,709,176;21,1,15753,628,185;22,1,15769,536,199;23,1,15785,490,206;24,1,15802,463,211;25,1,15820,449,214;26,1,15836,439,216;27,1,15852,436,216;28,1,16012,436,216;29,1,16018,435,219;30,1,16037,430,223;31,1,16052,424,226;32,1,16069,419,228;33,1,16086,411,230;34,1,16102,400,233;35,1,16119,389,235;36,1,16135,382,237;37,1,16153,375,239;38,1,16169,368,242;39,1,16185,365,243;40,1,16202,362,244;41,1,16220,359,245;42,1,16236,357,247;43,1,16252,355,249;44,1,16268,353,251;45,1,16285,352,252;46,1,16301,350,254;47,1,16318,346,256;48,1,16336,344,258;49,1,16355,342,260;50,1,16368,341,262;51,1,16385,340,263;52,1,16402,340,263;53,1,16418,339,264;54,1,16435,339,265;55,1,16452,338,265;56,1,16469,338,266;57,1,16485,338,266;58,1,16503,338,266;59,1,16519,337,266;60,1,16535,337,266;61,1,16552,337,267;62,1,16569,336,267;63,1,16586,335,268;64,1,16601,334,270;65,1,16620,334,271;66,1,16635,333,271;67,1,16651,333,272;68,3,17258,333,272,1027;69,4,17356,333,272,1027;70,2,17360,333,272,1027;71,1,17821,333,272;72,1,17835,333,273;73,1,17852,332,276;74,1,17868,331,279;75,1,17885,330,284;76,1,17905,328,288;77,1,17919,327,291;78,1,17935,326,293;79,1,17952,326,294;80,1,17969,325,295;81,1,17985,325,296;82,1,18003,324,298;83,1,18019,324,299;84,1,18035,324,300;85,1,18053,324,301;86,1,18068,324,302;87,1,18085,324,302;88,1,18103,324,303;89,1,18118,324,303;90,1,18135,324,303;91,1,18152,324,304;92,1,18171,324,305;93,1,18185,323,305;94,1,18202,323,306;95,1,18219,323,306;96,1,18235,323,307;97,1,18251,323,307;98,1,18269,323,308;99,1,18287,323,309;100,1,18303,323,310;101,1,18318,322,310;102,1,18335,322,311;110,3,18672,321,320,1244;-1,2,-94,-117,-1,2,-94,-111,-1,2,-94,-109,-1,2,-94,-114,-1,2,-94,-103,2,4042;3,17255;-1,2,-94,-112,https://online.simplii.com/ebm-resources/public/client/web/index.html#/signon-1,2,-94,-115,18991,1642342,0,0,0,0,1661332,18672,0,1552628533046,5,16609,5,111,2768,3,0,18673,1571994,1,3633BDB3EE3C9697D2A8B76494FF1702~-1~YAAQfjIcuOqEj0tpAQAAPy7gfwGeD8iDSFd1Jh5bmlXLAiCkwEjR9PCufVXkjrpCrYviu23zkFm4DdIciGtqpueyA0llrVYQ2hYRMkU5HuNF9vLX/JxtVaH8WdZtI4UuGrCI54WdovzKtI8GzJWC4Buz5PG5CtwLJizoCqO+C99stxRxbMzp7BgRIGmJciBy9ZVKUWaMExgPwJOcfvZTKitJYpkQMAfIUn9+CkGt+NgcUJPk4voPzDxORXwIdUOg9XArwHxgJ60CwvA6xthc++3JnfsPQ9/8xq/4FoY=~-1~-1~-1,28721,818,883658489,30261693-1,2,-94,-106,1,2-1,2,-94,-119,23,26,27,16,25,32,16,11,9,8,7,207,217,149,-1,2,-94,-122,0,0,0,0,1,0,0-1,2,-94,-123,-1,2,-94,-70,-872636394;dis;,7,8;true;true;true;420;true;24;24;true;false;1-1,2,-94,-80,4918-1,2,-94,-116,42665187-1,2,-94,-118,188077-1,2,-94,-121,;5;7;0"}
    """.trimIndent()

    private data class AuthRequest(val card: Card, val password: String) {
        data class Card(val value: String, val description: String, val encrypted: Boolean, val encrypt: Boolean)
    }

    @Test
    fun test() {
        val dir = File(System.getProperty("user.home"), ".bany")
        val configFile = File(dir, "bany.config")
        if (!configFile.exists()) {
            println("unable to find config file, skipping!")
            return
        }
        val moshi = Moshi.Builder().build()
        val configAdapter = moshi.adapter(CibcTransactionServiceTest.TestConfig::class.java)
        val config = configAdapter.fromJson(configFile.readText())
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