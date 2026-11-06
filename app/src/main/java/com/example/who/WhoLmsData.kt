package com.example.who

import com.example.data.model.SexCategory

data class LmsPoint(
    val indexValue: Double, // Month for age-based, or Length (cm) for weight-for-length
    val l: Double,
    val m: Double,
    val s: Double
)

object WhoLmsData {
    const val VERSION = "2006 WHO Child Growth Standards (MGRS)"
    const val PROVENANCE = "World Health Organization Multicentre Growth Reference Study"
    const val SOURCE_URL = "https://www.who.int/tools/child-growth-standards"

    // --- WEIGHT-FOR-AGE BOYS (Month 0 to 60) ---
    val weightForAgeBoys = listOf(
        LmsPoint(0.0, 0.3487, 3.3464, 0.14602),
        LmsPoint(1.0, 0.2297, 4.4709, 0.13395),
        LmsPoint(2.0, 0.1470, 5.5960, 0.12411),
        LmsPoint(3.0, 0.0827, 6.3980, 0.11667),
        LmsPoint(4.0, 0.0305, 7.0270, 0.11100),
        LmsPoint(5.0, -0.0129, 7.5388, 0.10669),
        LmsPoint(6.0, -0.0504, 7.9352, 0.10344),
        LmsPoint(7.0, -0.0833, 8.2721, 0.10100),
        LmsPoint(8.0, -0.1126, 8.5724, 0.09919),
        LmsPoint(9.0, -0.1390, 8.8477, 0.09786),
        LmsPoint(10.0, -0.1631, 9.1042, 0.09689),
        LmsPoint(11.0, -0.1852, 9.3458, 0.09621),
        LmsPoint(12.0, -0.2057, 9.5760, 0.09575),
        LmsPoint(15.0, -0.2589, 10.222, 0.09520),
        LmsPoint(18.0, -0.3013, 10.824, 0.09549),
        LmsPoint(21.0, -0.3359, 11.391, 0.09641),
        LmsPoint(24.0, -0.3649, 11.931, 0.09778),
        LmsPoint(30.0, -0.4103, 12.983, 0.10118),
        LmsPoint(36.0, -0.4418, 14.000, 0.10470),
        LmsPoint(42.0, -0.4619, 15.008, 0.10826),
        LmsPoint(48.0, -0.4727, 16.028, 0.11180),
        LmsPoint(54.0, -0.4776, 17.065, 0.11534),
        LmsPoint(60.0, -0.4795, 18.121, 0.11890)
    )

    // --- WEIGHT-FOR-AGE GIRLS (Month 0 to 60) ---
    val weightForAgeGirls = listOf(
        LmsPoint(0.0, 0.3809, 3.2322, 0.14171),
        LmsPoint(1.0, 0.2584, 4.1873, 0.13098),
        LmsPoint(2.0, 0.1706, 5.1282, 0.12204),
        LmsPoint(3.0, 0.1023, 5.8449, 0.11539),
        LmsPoint(4.0, 0.0470, 6.4237, 0.11041),
        LmsPoint(5.0, 0.0007, 6.8985, 0.10667),
        LmsPoint(6.0, -0.0392, 7.2970, 0.10390),
        LmsPoint(7.0, -0.0744, 7.6416, 0.10188),
        LmsPoint(8.0, -0.1058, 7.9498, 0.10044),
        LmsPoint(9.0, -0.1343, 8.2327, 0.09947),
        LmsPoint(10.0, -0.1603, 8.4981, 0.09886),
        LmsPoint(11.0, -0.1843, 8.7512, 0.09852),
        LmsPoint(12.0, -0.2066, 8.9954, 0.09838),
        LmsPoint(15.0, -0.2647, 9.6910, 0.09873),
        LmsPoint(18.0, -0.3121, 10.334, 0.09995),
        LmsPoint(21.0, -0.3514, 10.941, 0.10183),
        LmsPoint(24.0, -0.3846, 11.519, 0.10423),
        LmsPoint(30.0, -0.4347, 12.632, 0.10934),
        LmsPoint(36.0, -0.4688, 13.650, 0.11470),
        LmsPoint(42.0, -0.4897, 14.654, 0.11978),
        LmsPoint(48.0, -0.4996, 15.700, 0.12450),
        LmsPoint(54.0, -0.5003, 16.744, 0.12903),
        LmsPoint(60.0, -0.4960, 17.700, 0.13320)
    )

    // --- LENGTH-FOR-AGE BOYS (Month 0 to 60) ---
    val lengthForAgeBoys = listOf(
        LmsPoint(0.0, 1.0, 49.8842, 0.03795),
        LmsPoint(1.0, 1.0, 54.7244, 0.03559),
        LmsPoint(2.0, 1.0, 58.4249, 0.03433),
        LmsPoint(3.0, 1.0, 61.4292, 0.03358),
        LmsPoint(4.0, 1.0, 63.8860, 0.03316),
        LmsPoint(5.0, 1.0, 65.9026, 0.03295),
        LmsPoint(6.0, 1.0, 67.6236, 0.03290),
        LmsPoint(7.0, 1.0, 69.1605, 0.03296),
        LmsPoint(8.0, 1.0, 70.5734, 0.03310),
        LmsPoint(9.0, 1.0, 71.8953, 0.03330),
        LmsPoint(10.0, 1.0, 73.1416, 0.03354),
        LmsPoint(11.0, 1.0, 74.3262, 0.03381),
        LmsPoint(12.0, 1.0, 75.7497, 0.03410),
        LmsPoint(15.0, 1.0, 79.1350, 0.03498),
        LmsPoint(18.0, 1.0, 82.3140, 0.03582),
        LmsPoint(21.0, 1.0, 85.1200, 0.03657),
        LmsPoint(24.0, 1.0, 87.7500, 0.03724),
        LmsPoint(30.0, 1.0, 92.3000, 0.03830),
        LmsPoint(36.0, 1.0, 96.1000, 0.03920),
        LmsPoint(42.0, 1.0, 99.9000, 0.03990),
        LmsPoint(48.0, 1.0, 103.300, 0.04050),
        LmsPoint(54.0, 1.0, 106.700, 0.04110),
        LmsPoint(60.0, 1.0, 110.000, 0.04160)
    )

    // --- LENGTH-FOR-AGE GIRLS (Month 0 to 60) ---
    val lengthForAgeGirls = listOf(
        LmsPoint(0.0, 1.0, 49.1477, 0.03790),
        LmsPoint(1.0, 1.0, 53.6872, 0.03554),
        LmsPoint(2.0, 1.0, 57.0673, 0.03434),
        LmsPoint(3.0, 1.0, 59.8029, 0.03369),
        LmsPoint(4.0, 1.0, 62.0899, 0.03337),
        LmsPoint(5.0, 1.0, 64.0020, 0.03326),
        LmsPoint(6.0, 1.0, 65.7311, 0.03328),
        LmsPoint(7.0, 1.0, 67.3242, 0.03340),
        LmsPoint(8.0, 1.0, 68.7447, 0.03358),
        LmsPoint(9.0, 1.0, 70.1415, 0.03382),
        LmsPoint(10.0, 1.0, 71.4981, 0.03409),
        LmsPoint(11.0, 1.0, 72.8228, 0.03439),
        LmsPoint(12.0, 1.0, 74.2800, 0.03471),
        LmsPoint(15.0, 1.0, 77.5200, 0.03565),
        LmsPoint(18.0, 1.0, 80.7000, 0.03655),
        LmsPoint(21.0, 1.0, 83.6000, 0.03736),
        LmsPoint(24.0, 1.0, 86.4000, 0.03808),
        LmsPoint(30.0, 1.0, 91.2000, 0.03920),
        LmsPoint(36.0, 1.0, 95.1000, 0.04010),
        LmsPoint(42.0, 1.0, 99.0000, 0.04080),
        LmsPoint(48.0, 1.0, 102.700, 0.04150),
        LmsPoint(54.0, 1.0, 106.200, 0.04210),
        LmsPoint(60.0, 1.0, 109.400, 0.04270)
    )

    // --- HEAD CIRCUMFERENCE-FOR-AGE BOYS (Month 0 to 60) ---
    val headCircumferenceBoys = listOf(
        LmsPoint(0.0, 1.0, 34.4600, 0.0372),
        LmsPoint(1.0, 1.0, 37.2800, 0.0352),
        LmsPoint(2.0, 1.0, 39.1200, 0.0340),
        LmsPoint(3.0, 1.0, 40.5400, 0.0332),
        LmsPoint(4.0, 1.0, 41.6700, 0.0327),
        LmsPoint(5.0, 1.0, 42.6000, 0.0324),
        LmsPoint(6.0, 1.0, 43.3400, 0.0322),
        LmsPoint(9.0, 1.0, 45.1800, 0.0319),
        LmsPoint(12.0, 1.0, 46.3300, 0.0318),
        LmsPoint(18.0, 1.0, 47.7900, 0.0317),
        LmsPoint(24.0, 1.0, 48.7800, 0.0318),
        LmsPoint(36.0, 1.0, 50.0000, 0.0320),
        LmsPoint(48.0, 1.0, 50.7000, 0.0323),
        LmsPoint(60.0, 1.0, 51.2000, 0.0325)
    )

    // --- HEAD CIRCUMFERENCE-FOR-AGE GIRLS (Month 0 to 60) ---
    val headCircumferenceGirls = listOf(
        LmsPoint(0.0, 1.0, 33.8800, 0.0368),
        LmsPoint(1.0, 1.0, 36.5400, 0.0349),
        LmsPoint(2.0, 1.0, 38.3000, 0.0338),
        LmsPoint(3.0, 1.0, 39.5400, 0.0331),
        LmsPoint(4.0, 1.0, 40.6300, 0.0326),
        LmsPoint(5.0, 1.0, 41.5100, 0.0323),
        LmsPoint(6.0, 1.0, 42.2400, 0.0321),
        LmsPoint(9.0, 1.0, 44.0200, 0.0318),
        LmsPoint(12.0, 1.0, 45.2200, 0.0317),
        LmsPoint(18.0, 1.0, 46.7300, 0.0316),
        LmsPoint(24.0, 1.0, 47.7400, 0.0317),
        LmsPoint(36.0, 1.0, 49.0000, 0.0320),
        LmsPoint(48.0, 1.0, 49.7000, 0.0323),
        LmsPoint(60.0, 1.0, 50.3000, 0.0325)
    )

    // --- WEIGHT-FOR-LENGTH BOYS (Length 45 to 105 cm) ---
    val weightForLengthBoys = listOf(
        LmsPoint(45.0, -0.3833, 2.435, 0.0898),
        LmsPoint(50.0, -0.3833, 3.328, 0.0872),
        LmsPoint(55.0, -0.3833, 4.455, 0.0854),
        LmsPoint(60.0, -0.3833, 5.811, 0.0844),
        LmsPoint(65.0, -0.3833, 7.274, 0.0841),
        LmsPoint(70.0, -0.3833, 8.647, 0.0844),
        LmsPoint(75.0, -0.3833, 9.878, 0.0853),
        LmsPoint(80.0, -0.3833, 10.985, 0.0866),
        LmsPoint(85.0, -0.3833, 12.012, 0.0883),
        LmsPoint(90.0, -0.3833, 12.986, 0.0903),
        LmsPoint(95.0, -0.3833, 14.045, 0.0926),
        LmsPoint(100.0, -0.3833, 15.228, 0.0952),
        LmsPoint(105.0, -0.3833, 16.544, 0.0980)
    )

    // --- WEIGHT-FOR-LENGTH GIRLS (Length 45 to 105 cm) ---
    val weightForLengthGirls = listOf(
        LmsPoint(45.0, -0.3833, 2.457, 0.0894),
        LmsPoint(50.0, -0.3833, 3.284, 0.0870),
        LmsPoint(55.0, -0.3833, 4.318, 0.0855),
        LmsPoint(60.0, -0.3833, 5.560, 0.0847),
        LmsPoint(65.0, -0.3833, 6.945, 0.0845),
        LmsPoint(70.0, -0.3833, 8.279, 0.0849),
        LmsPoint(75.0, -0.3833, 9.489, 0.0858),
        LmsPoint(80.0, -0.3833, 10.590, 0.0872),
        LmsPoint(85.0, -0.3833, 11.614, 0.0889),
        LmsPoint(90.0, -0.3833, 12.605, 0.0909),
        LmsPoint(95.0, -0.3833, 13.682, 0.0931),
        LmsPoint(100.0, -0.3833, 14.880, 0.0956),
        LmsPoint(105.0, -0.3833, 16.215, 0.0984)
    )
}
