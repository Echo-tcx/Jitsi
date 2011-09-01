/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.neomedia.codec.audio.silk;

import java.util.*;

/**
 * This file has been automatically generated
 * ROM usage:  0.29 + 2.66 kB
 *
 * @author Jing Dai
 * @author Dingxin Xu
 */
public class Silk_tables_NLSF_CB0_10
{
    static final int NLSF_MSVQ_CB0_10_STAGES =      6;
    static final int NLSF_MSVQ_CB0_10_VECTORS =     120;

    static final int[] SKP_Silk_NLSF_MSVQ_CB0_10_CDF =
    {
                0,
             2658,
             4420,
             6107,
             7757,
             9408,
            10955,
            12502,
            13983,
            15432,
            16882,
            18331,
            19750,
            21108,
            22409,
            23709,
            25010,
            26256,
            27501,
            28747,
            29965,
            31158,
            32351,
            33544,
            34736,
            35904,
            36997,
            38091,
            39185,
            40232,
            41280,
            42327,
            43308,
            44290,
            45271,
            46232,
            47192,
            48132,
            49032,
            49913,
            50775,
            51618,
            52462,
            53287,
            54095,
            54885,
            55675,
            56449,
            57222,
            57979,
            58688,
            59382,
            60076,
            60726,
            61363,
            61946,
            62505,
            63052,
            63543,
            63983,
            64396,
            64766,
            65023,
            65279,
            65535,
                0,
             4977,
             9542,
            14106,
            18671,
            23041,
            27319,
            31596,
            35873,
            39969,
            43891,
            47813,
            51652,
            55490,
            59009,
            62307,
            65535,
                0,
             8571,
            17142,
            25529,
            33917,
            42124,
            49984,
            57844,
            65535,
                0,
             8732,
            17463,
            25825,
            34007,
            42189,
            50196,
            58032,
            65535,
                0,
             8948,
            17704,
            25733,
            33762,
            41791,
            49821,
            57678,
            65535,
                0,
             4374,
             8655,
            12936,
            17125,
            21313,
            25413,
            29512,
            33611,
            37710,
            41809,
            45820,
            49832,
            53843,
            57768,
            61694,
            65535
    };
    static private int [] SKP_Silk_NLSF_MSVQ_CB0_10_CDF_0 
            = Arrays.copyOfRange(SKP_Silk_NLSF_MSVQ_CB0_10_CDF, 0, SKP_Silk_NLSF_MSVQ_CB0_10_CDF.length);
    static private int [] SKP_Silk_NLSF_MSVQ_CB0_10_CDF_65 
            = Arrays.copyOfRange(SKP_Silk_NLSF_MSVQ_CB0_10_CDF, 65, SKP_Silk_NLSF_MSVQ_CB0_10_CDF.length);
    static private int [] SKP_Silk_NLSF_MSVQ_CB0_10_CDF_82 
            = Arrays.copyOfRange(SKP_Silk_NLSF_MSVQ_CB0_10_CDF, 82, SKP_Silk_NLSF_MSVQ_CB0_10_CDF.length);
    static private int [] SKP_Silk_NLSF_MSVQ_CB0_10_CDF_91 
            = Arrays.copyOfRange(SKP_Silk_NLSF_MSVQ_CB0_10_CDF, 91, SKP_Silk_NLSF_MSVQ_CB0_10_CDF.length);
    static private int [] SKP_Silk_NLSF_MSVQ_CB0_10_CDF_100 
            = Arrays.copyOfRange(SKP_Silk_NLSF_MSVQ_CB0_10_CDF, 100, SKP_Silk_NLSF_MSVQ_CB0_10_CDF.length);
    static private int [] SKP_Silk_NLSF_MSVQ_CB0_10_CDF_109 
            = Arrays.copyOfRange(SKP_Silk_NLSF_MSVQ_CB0_10_CDF, 109, SKP_Silk_NLSF_MSVQ_CB0_10_CDF.length);
    
    static final int[][]  SKP_Silk_NLSF_MSVQ_CB0_10_CDF_start_ptr =
    {
         SKP_Silk_NLSF_MSVQ_CB0_10_CDF_0,
         SKP_Silk_NLSF_MSVQ_CB0_10_CDF_65,
         SKP_Silk_NLSF_MSVQ_CB0_10_CDF_82,
         SKP_Silk_NLSF_MSVQ_CB0_10_CDF_91,
         SKP_Silk_NLSF_MSVQ_CB0_10_CDF_100,
         SKP_Silk_NLSF_MSVQ_CB0_10_CDF_109
    };
    
    static final  int[] SKP_Silk_NLSF_MSVQ_CB0_10_CDF_middle_idx  =
    {
          23,
           8,
           5,
           5,
           5,
           9
    };

    static final short[] SKP_Silk_NLSF_MSVQ_CB0_10_rates_Q5 =
    {
                  148,              167,
                  169,              170,
                  170,              173,
                  173,              175,
                  176,              176,
                  176,              177,
                  179,              181,
                  181,              181,
                  183,              183,
                  183,              184,
                  185,              185,
                  185,              185,
                  186,              189,
                  189,              189,
                  191,              191,
                  191,              194,
                  194,              194,
                  195,              195,
                  196,              198,
                  199,              200,
                  201,              201,
                  202,              203,
                  204,              204,
                  205,              205,
                  206,              209,
                  210,              210,
                  213,              214,
                  218,              220,
                  221,              226,
                  231,              234,
                  239,              256,
                  256,              256,
                  119,              123,
                  123,              123,
                  125,              126,
                  126,              126,
                  128,              130,
                  130,              131,
                  131,              135,
                  138,              139,
                   94,               94,
                   95,               95,
                   96,               98,
                   98,               99,
                   93,               93,
                   95,               96,
                   96,               97,
                   98,              100,
                   92,               93,
                   97,               97,
                   97,               97,
                   98,               98,
                  125,              126,
                  126,              127,
                  127,              128,
                  128,              128,
                  128,              128,
                  129,              129,
                  129,              130,
                  130,              131
    };

    static final int[] SKP_Silk_NLSF_MSVQ_CB0_10_ndelta_min_Q15 =
    {
                  563,
                    3,
                   22,
                   20,
                    3,
                    3,
                  132,
                  119,
                  358,
                   86,
                  964
    };

    static final short[] SKP_Silk_NLSF_MSVQ_CB0_10_Q15 = 
    {
                 2210,             4023,
                 6981,             9260,
                12573,            15687,
                19207,            22383,
                25981,            29142,
                 3285,             4172,
                 6116,            10856,
                15289,            16826,
                19701,            22010,
                24721,            29313,
                 1554,             2511,
                 6577,            10337,
                13837,            16511,
                20086,            23214,
                26480,            29464,
                 3062,             4017,
                 5771,            10037,
                13365,            14952,
                20140,            22891,
                25229,            29603,
                 2085,             3457,
                 5934,             8718,
                11501,            13670,
                17997,            21817,
                24935,            28745,
                 2776,             4093,
                 6421,            10413,
                15111,            16806,
                20825,            23826,
                26308,            29411,
                 2717,             4034,
                 5697,             8463,
                14301,            16354,
                19007,            23413,
                25812,            28506,
                 2872,             3702,
                 5881,            11034,
                17141,            18879,
                21146,            23451,
                25817,            29600,
                 2999,             4015,
                 7357,            11219,
                12866,            17307,
                20081,            22644,
                26774,            29107,
                 2942,             3866,
                 5918,            11915,
                13909,            16072,
                20453,            22279,
                27310,            29826,
                 2271,             3527,
                 6606,             9729,
                12943,            17382,
                20224,            22345,
                24602,            28290,
                 2207,             3310,
                 5844,             9339,
                11141,            15651,
                18576,            21177,
                25551,            28228,
                 3963,             4975,
                 6901,            11588,
                13466,            15577,
                19231,            21368,
                25510,            27759,
                 2749,             3549,
                 6966,            13808,
                15653,            17645,
                20090,            22599,
                26467,            28537,
                 2126,             3504,
                 5109,             9954,
                12550,            14620,
                19703,            21687,
                26457,            29106,
                 3966,             5745,
                 7442,             9757,
                14468,            16404,
                19135,            23048,
                25375,            28391,
                 3197,             4751,
                 6451,             9298,
                13038,            14874,
                17962,            20627,
                23835,            28464,
                 3195,             4081,
                 6499,            12252,
                14289,            16040,
                18357,            20730,
                26980,            29309,
                 1533,             2471,
                 4486,             7796,
                12332,            15758,
                19567,            22298,
                25673,            29051,
                 2002,             2971,
                 4985,             8083,
                13181,            15435,
                18237,            21517,
                24595,            28351,
                 3808,             4925,
                 6710,            10201,
                12011,            14300,
                18457,            20391,
                26525,            28956,
                 2281,             3418,
                 4979,             8726,
                15964,            18104,
                20250,            22771,
                25286,            28954,
                 3051,             5479,
                 7290,             9848,
                12744,            14503,
                18665,            23684,
                26065,            28947,
                 2364,             3565,
                 5502,             9621,
                14922,            16621,
                19005,            20996,
                26310,            29302,
                 4093,             5212,
                 6833,             9880,
                16303,            18286,
                20571,            23614,
                26067,            29128,
                 2941,             3996,
                 6038,            10638,
                12668,            14451,
                16798,            19392,
                26051,            28517,
                 3863,             5212,
                 7019,             9468,
                11039,            13214,
                19942,            22344,
                25126,            29539,
                 4615,             6172,
                 7853,            10252,
                12611,            14445,
                19719,            22441,
                24922,            29341,
                 3566,             4512,
                 6985,             8684,
                10544,            16097,
                18058,            22475,
                26066,            28167,
                 4481,             5489,
                 7432,            11414,
                13191,            15225,
                20161,            22258,
                26484,            29716,
                 3320,             4320,
                 6621,             9867,
                11581,            14034,
                21168,            23210,
                26588,            29903,
                 3794,             4689,
                 6916,             8655,
                10143,            16144,
                19568,            21588,
                27557,            29593,
                 2446,             3276,
                 5918,            12643,
                16601,            18013,
                21126,            23175,
                27300,            29634,
                 2450,             3522,
                 5437,             8560,
                15285,            19911,
                21826,            24097,
                26567,            29078,
                 2580,             3796,
                 5580,             8338,
                 9969,            12675,
                18907,            22753,
                25450,            29292,
                 3325,             4312,
                 6241,             7709,
                 9164,            14452,
                21665,            23797,
                27096,            29857,
                 3338,             4163,
                 7738,            11114,
                12668,            14753,
                16931,            22736,
                25671,            28093,
                 3840,             4755,
                 7755,            13471,
                15338,            17180,
                20077,            22353,
                27181,            29743,
                 2504,             4079,
                 8351,            12118,
                15046,            18595,
                21684,            24704,
                27519,            29937,
                 5234,             6342,
                 8267,            11821,
                15155,            16760,
                20667,            23488,
                25949,            29307,
                 2681,             3562,
                 6028,            10827,
                18458,            20458,
                22303,            24701,
                26912,            29956,
                 3374,             4528,
                 6230,             8256,
                 9513,            12730,
                18666,            20720,
                26007,            28425,
                 2731,             3629,
                 8320,            12450,
                14112,            16431,
                18548,            22098,
                25329,            27718,
                 3481,             4401,
                 7321,             9319,
                11062,            13093,
                15121,            22315,
                26331,            28740,
                 3577,             4945,
                 6669,             8792,
                10299,            12645,
                19505,            24766,
                26996,            29634,
                 4058,             5060,
                 7288,            10190,
                11724,            13936,
                15849,            18539,
                26701,            29845,
                 4262,             5390,
                 7057,             8982,
                10187,            15264,
                20480,            22340,
                25958,            28072,
                 3404,             4329,
                 6629,             7946,
                10121,            17165,
                19640,            22244,
                25062,            27472,
                 3157,             4168,
                 6195,             9319,
                10771,            13325,
                15416,            19816,
                24672,            27634,
                 2503,             3473,
                 5130,             6767,
                 8571,            14902,
                19033,            21926,
                26065,            28728,
                 4133,             5102,
                 7553,            10054,
                11757,            14924,
                17435,            20186,
                23987,            26272,
                 4972,             6139,
                 7894,             9633,
                11320,            14295,
                21737,            24306,
                26919,            29907,
                 2958,             3816,
                 6851,             9204,
                10895,            18052,
                20791,            23338,
                27556,            29609,
                 5234,             6028,
                 8034,            10154,
                11242,            14789,
                18948,            20966,
                26585,            29127,
                 5241,             6838,
                10526,            12819,
                14681,            17328,
                19928,            22336,
                26193,            28697,
                 3412,             4251,
                 5988,             7094,
                 9907,            18243,
                21669,            23777,
                26969,            29087,
                 2470,             3217,
                 7797,            15296,
                17365,            19135,
                21979,            24256,
                27322,            29442,
                 4939,             5804,
                 8145,            11809,
                13873,            15598,
                17234,            19423,
                26476,            29645,
                 5051,             6167,
                 8223,             9655,
                12159,            17995,
                20464,            22832,
                26616,            28462,
                 4987,             5907,
                 9319,            11245,
                13132,            15024,
                17485,            22687,
                26011,            28273,
                 5137,             6884,
                11025,            14950,
                17191,            19425,
                21807,            24393,
                26938,            29288,
                 7057,             7884,
                 9528,            10483,
                10960,            14811,
                19070,            21675,
                25645,            28019,
                 6759,             7160,
                 8546,            11779,
                12295,            13023,
                16627,            21099,
                24697,            28287,
                 3863,             9762,
                11068,            11445,
                12049,            13960,
                18085,            21507,
                25224,            28997,
                  397,              335,
                  651,             1168,
                  640,              765,
                  465,              331,
                  214,             -194,
                 -578,             -647,
                 -657,              750,
                  564,              613,
                  549,              630,
                  304,              -52,
                  828,              922,
                  443,              111,
                  138,              124,
                  169,               14,
                  144,               83,
                  132,               58,
                 -413,             -752,
                  869,              336,
                  385,               69,
                   56,              830,
                 -227,             -266,
                 -368,             -440,
                -1195,              163,
                  126,             -228,
                  802,              156,
                  188,              120,
                  376,               59,
                 -358,             -558,
                -1326,             -254,
                 -202,             -789,
                  296,               92,
                  -70,             -129,
                 -718,            -1135,
                  292,              -29,
                 -631,              487,
                 -157,             -153,
                 -279,                2,
                 -419,             -342,
                  -34,             -514,
                 -799,            -1571,
                 -687,             -609,
                 -546,             -130,
                 -215,             -252,
                 -446,             -574,
                -1337,              207,
                  -72,               32,
                  103,             -642,
                  942,              733,
                  187,               29,
                 -211,             -814,
                  143,              225,
                   20,               24,
                 -268,             -377,
                 1623,             1133,
                  667,              164,
                  307,              366,
                  187,               34,
                   62,             -313,
                 -832,            -1482,
                -1181,              483,
                  -42,              -39,
                 -450,            -1406,
                 -587,              -52,
                 -760,              334,
                   98,              -60,
                 -500,             -488,
                -1058,              299,
                  131,             -250,
                 -251,             -703,
                 1037,              568,
                 -413,             -265,
                 1687,              573,
                  345,              323,
                   98,               61,
                 -102,               31,
                  135,              149,
                  617,              365,
                  -39,               34,
                 -611,             1201,
                 1421,              736,
                 -414,             -393,
                 -492,             -343,
                 -316,             -532,
                  528,              172,
                   90,              322,
                 -294,             -319,
                 -541,              503,
                  639,              401,
                    1,             -149,
                  -73,             -167,
                  150,              118,
                  308,              218,
                  121,              195,
                 -143,             -261,
                -1013,             -802,
                  387,              436,
                  130,             -427,
                 -448,             -681,
                  123,              -87,
                 -251,             -113,
                  274,              310,
                  445,              501,
                  354,              272,
                  141,             -285,
                  569,              656,
                   37,              -49,
                  251,             -386,
                 -263,             1122,
                  604,              606,
                  336,               95,
                   34,                0,
                   85,              180,
                  207,             -367,
                 -622,             1070,
                   -6,              -79,
                 -160,              -92,
                 -137,             -276,
                 -323,             -371,
                 -696,            -1036,
                  407,              102,
                  -86,             -214,
                 -482,             -647,
                  -28,             -291,
                  -97,             -180,
                 -250,             -435,
                  -18,              -76,
                 -332,              410,
                  407,              168,
                  539,              411,
                  254,              111,
                   58,             -145,
                  200,               30,
                  187,              116,
                  131,             -367,
                 -475,              781,
                 -559,              561,
                  195,             -115,
                    8,             -168,
                   30,               55,
                 -122,              131,
                   82,               -5,
                 -273,              -50,
                 -632,              668,
                    4,               32,
                  -26,             -279,
                  315,              165,
                  197,              377,
                  155,              -41,
                 -138,             -324,
                 -109,             -617,
                  360,               98,
                  -53,             -319,
                 -114,             -245,
                  -82,              507,
                  468,              263,
                 -137,             -389,
                  652,              354,
                  -18,             -227,
                 -462,             -135,
                  317,               53,
                  -16,               66,
                  -72,             -126,
                 -356,             -347,
                 -328,              -72,
                 -337,              324,
                  152,              349,
                  169,             -196,
                  179,              254,
                  260,              325,
                  -74,              -80,
                   75,              -31,
                  270,              275,
                   87,              278,
                 -446,             -301,
                  309,               71,
                  -25,             -242,
                  516,              161,
                 -162,              -83,
                  329,              230,
                 -311,             -259,
                  177,              -26,
                 -462,               89,
                  257,                6,
                 -130,              -93,
                 -456,             -317,
                 -221,             -206,
                 -417,             -182,
                  -74,              234,
                   48,              261,
                  359,              231,
                  258,               85,
                 -282,              252,
                 -147,             -222,
                  251,             -207,
                  443,              123,
                 -417,              -36,
                  273,             -241,
                  240,             -112,
                   44,             -167,
                  126,             -124,
                  -77,               58,
                 -401,              333,
                 -118,               82,
                  126,              151,
                 -433,              359,
                 -130,             -102,
                  131,             -244,
                   86,               85,
                 -462,              414,
                 -240,               16,
                  145,               28,
                 -205,             -481,
                  373,              293,
                  -72,             -174,
                   62,              259,
                   -8,              -18,
                  362,              233,
                  185,               43,
                  278,               27,
                  193,              570,
                 -248,              189,
                   92,               31,
                 -275,               -3,
                  243,              176,
                  438,              209,
                  206,              -51,
                   79,              109,
                  168,             -185,
                 -308,              -68,
                 -618,              385,
                 -310,             -108,
                 -164,              165,
                   61,             -152,
                 -101,             -412,
                 -268,             -257,
                  -40,              -20,
                  -28,             -158,
                 -301,              271,
                  380,             -338,
                 -367,             -132,
                   64,              114,
                 -131,             -225,
                 -156,             -260,
                  -63,             -116,
                  155,             -586,
                 -202,              254,
                 -287,              178,
                  227,             -106,
                 -294,              164,
                  298,             -100,
                  185,              317,
                  193,              -45,
                   28,               80,
                  -87,             -433,
                   22,              -48,
                   48,             -237,
                 -229,             -139,
                  120,             -364,
                  268,             -136,
                  396,              125,
                  130,              -89,
                 -272,              118,
                 -256,              -68,
                 -451,              488,
                  143,             -165,
                  -48,             -190,
                  106,              219,
                   47,              435,
                  245,               97,
                   75,             -418,
                  121,             -187,
                  570,             -200,
                 -351,              225,
                  -21,             -217,
                  234,             -111,
                  194,               14,
                  242,              118,
                  140,             -397,
                  355,              361,
                  -45,             -195
    };
    
    static final SKP_Silk_NLSF_CBS[] SKP_Silk_NLSF_CB0_10_Stage_info= 
    {
        new SKP_Silk_NLSF_CBS(64, SKP_Silk_NLSF_MSVQ_CB0_10_Q15, 10*0,   SKP_Silk_NLSF_MSVQ_CB0_10_rates_Q5, 0),
        new SKP_Silk_NLSF_CBS(16, SKP_Silk_NLSF_MSVQ_CB0_10_Q15, 10*64,  SKP_Silk_NLSF_MSVQ_CB0_10_rates_Q5, 64),
        new SKP_Silk_NLSF_CBS(8,  SKP_Silk_NLSF_MSVQ_CB0_10_Q15, 10*80,  SKP_Silk_NLSF_MSVQ_CB0_10_rates_Q5, 80),
        new SKP_Silk_NLSF_CBS(8,  SKP_Silk_NLSF_MSVQ_CB0_10_Q15, 10*88,  SKP_Silk_NLSF_MSVQ_CB0_10_rates_Q5, 88),
        new SKP_Silk_NLSF_CBS(8,  SKP_Silk_NLSF_MSVQ_CB0_10_Q15, 10*96,  SKP_Silk_NLSF_MSVQ_CB0_10_rates_Q5, 96),
        new SKP_Silk_NLSF_CBS(16, SKP_Silk_NLSF_MSVQ_CB0_10_Q15, 10*104, SKP_Silk_NLSF_MSVQ_CB0_10_rates_Q5, 104)
    };
    
    static final SKP_Silk_NLSF_CB_struct SKP_Silk_NLSF_CB0_10 = 
        new SKP_Silk_NLSF_CB_struct(
        NLSF_MSVQ_CB0_10_STAGES,
        SKP_Silk_NLSF_CB0_10_Stage_info,
        SKP_Silk_NLSF_MSVQ_CB0_10_ndelta_min_Q15,
        SKP_Silk_NLSF_MSVQ_CB0_10_CDF,
        SKP_Silk_NLSF_MSVQ_CB0_10_CDF_start_ptr,
        SKP_Silk_NLSF_MSVQ_CB0_10_CDF_middle_idx    
    );
}
