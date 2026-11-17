package com.example.domain

import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.*

data class OfflineCity(
    val id: String,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val utcOffsetHours: Double
)

enum class PrayerCalcMethod(
    val id: String,
    val label: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaIntervalMin: Int = 0
) {
    MWL("mwl", "Muslim World League", 18.0, 17.0),
    ISNA("isna", "ISNA (North America)", 15.0, 15.0),
    EGYPT("egypt", "Egyptian General Authority", 19.5, 17.5),
    MAKKAH("makkah", "Umm al-Qura (Makkah)", 18.5, 0.0, 90),
    KARACHI("karachi", "Univ. of Islamic Sciences, Karachi", 18.0, 18.0);

    companion object {
        fun fromId(id: String): PrayerCalcMethod = entries.find { it.id == id } ?: MWL
    }
}

data class DailyPrayerTimes(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val prayerMinutes: List<Pair<String, Double>>
)

data class DuaItem(
    val id: Int,
    val category: String,
    val title: String,
    val arabic: String,
    val transliteration: String,
    val translation: String
)

object IslamicAstroEngine {

    val cities: List<OfflineCity> = listOf(
        OfflineCity("makkah", "Makkah", "Saudi Arabia", 21.4225, 39.8262, 3.0),
        OfflineCity("madinah", "Madinah", "Saudi Arabia", 24.4672, 39.6111, 3.0),
        OfflineCity("riyadh", "Riyadh", "Saudi Arabia", 24.7136, 46.6753, 3.0),
        OfflineCity("dubai", "Dubai", "UAE", 25.2048, 55.2708, 4.0),
        OfflineCity("doha", "Doha", "Qatar", 25.2854, 51.5310, 3.0),
        OfflineCity("cairo", "Cairo", "Egypt", 30.0444, 31.2357, 2.0),
        OfflineCity("istanbul", "Istanbul", "Turkey", 41.0082, 28.9784, 3.0),
        OfflineCity("karachi", "Karachi", "Pakistan", 24.8607, 67.0011, 5.0),
        OfflineCity("lahore", "Lahore", "Pakistan", 31.5204, 74.3587, 5.0),
        OfflineCity("islamabad", "Islamabad", "Pakistan", 33.6844, 73.0479, 5.0),
        OfflineCity("dhaka", "Dhaka", "Bangladesh", 23.8103, 90.4125, 6.0),
        OfflineCity("delhi", "New Delhi", "India", 28.6139, 77.2090, 5.5),
        OfflineCity("mumbai", "Mumbai", "India", 19.0760, 72.8777, 5.5),
        OfflineCity("jakarta", "Jakarta", "Indonesia", -6.2088, 106.8456, 7.0),
        OfflineCity("kuala_lumpur", "Kuala Lumpur", "Malaysia", 3.1390, 101.6869, 8.0),
        OfflineCity("singapore", "Singapore", "Singapore", 1.3521, 103.8198, 8.0),
        OfflineCity("london", "London", "UK", 51.5072, -0.1276, 0.0),
        OfflineCity("paris", "Paris", "France", 48.8566, 2.3522, 1.0),
        OfflineCity("berlin", "Berlin", "Germany", 52.5200, 13.4050, 1.0),
        OfflineCity("madrid", "Madrid", "Spain", 40.4168, -3.7038, 1.0),
        OfflineCity("new_york", "New York", "USA", 40.7128, -74.0060, -5.0),
        OfflineCity("chicago", "Chicago", "USA", 41.8781, -87.6298, -6.0),
        OfflineCity("los_angeles", "Los Angeles", "USA", 34.0522, -118.2437, -8.0),
        OfflineCity("toronto", "Toronto", "Canada", 43.6532, -79.3832, -5.0),
        OfflineCity("sydney", "Sydney", "Australia", -33.8688, 151.2093, 10.0),
        OfflineCity("tokyo", "Tokyo", "Japan", 35.6762, 139.6503, 9.0),
        OfflineCity("casablanca", "Casablanca", "Morocco", 33.5731, -7.5898, 1.0),
        OfflineCity("amman", "Amman", "Jordan", 31.9454, 35.9284, 3.0),
        OfflineCity("lagos", "Lagos", "Nigeria", 6.5244, 3.3792, 1.0),
        OfflineCity("johannesburg", "Johannesburg", "South Africa", -26.2041, 28.0473, 2.0)
    )

    fun findCity(id: String): OfflineCity = cities.find { it.id == id } ?: cities.first()

    /**
     * Pure offline astronomical calculation of daily prayer times.
     */
    fun calculatePrayerTimes(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        utcOffsetHours: Double,
        method: PrayerCalcMethod
    ): DailyPrayerTimes {
        val jd = julianDate(date.year, date.monthValue, date.dayOfMonth) - longitude / (15.0 * 24.0)
        val (decl, eqTime) = sunPosition(jd)

        val dhuhr = fixHour(12.0 + utcOffsetHours - longitude / 15.0 - eqTime)
        val sunrise = dhuhr - sunAngleTime(0.833, latitude, decl)
        val sunset = dhuhr + sunAngleTime(0.833, latitude, decl)
        val fajr = dhuhr - sunAngleTime(method.fajrAngle, latitude, decl)
        val asr = dhuhr + asrTime(1.0, latitude, decl)
        val maghrib = sunset
        val isha = if (method.ishaIntervalMin > 0) {
            maghrib + method.ishaIntervalMin / 60.0
        } else {
            dhuhr + sunAngleTime(method.ishaAngle, latitude, decl)
        }

        val list = listOf(
            "Fajr" to fajr,
            "Sunrise" to sunrise,
            "Dhuhr" to dhuhr,
            "Asr" to asr,
            "Maghrib" to maghrib,
            "Isha" to isha
        )

        return DailyPrayerTimes(
            fajr = formatHour(fajr),
            sunrise = formatHour(sunrise),
            dhuhr = formatHour(dhuhr),
            asr = formatHour(asr),
            maghrib = formatHour(maghrib),
            isha = formatHour(isha),
            prayerMinutes = list
        )
    }

    /**
     * Great-Circle bearing from (lat, lon) to the Kaaba in Makkah (degrees clockwise from North).
     */
    fun calculateQiblaBearing(latitude: Double, longitude: Double): Double {
        val kaabaLat = Math.toRadians(21.422487)
        val kaabaLon = Math.toRadians(39.826206)
        val latRad = Math.toRadians(latitude)
        val lonRad = Math.toRadians(longitude)

        val dLon = kaabaLon - lonRad
        val y = sin(dLon)
        val x = cos(latRad) * tan(kaabaLat) - sin(latRad) * cos(dLon)
        val bearing = Math.toDegrees(atan2(y, x))
        return (bearing + 360.0) % 360.0
    }

    fun calculateDistanceToMakkahKm(latitude: Double, longitude: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(21.422487 - latitude)
        val dLon = Math.toRadians(39.826206 - longitude)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(latitude)) * cos(Math.toRadians(21.422487)) * sin(dLon / 2).pow(2)
        return r * 2 * asin(sqrt(a))
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val ra = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15.0
        val eqTime = q / 15.0 - fixHour(ra)
        val decl = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        return decl to eqTime
    }

    private fun sunAngleTime(angle: Double, lat: Double, decl: Double): Double {
        val term = (-sin(Math.toRadians(angle)) - sin(Math.toRadians(lat)) * sin(Math.toRadians(decl))) /
            (cos(Math.toRadians(lat)) * cos(Math.toRadians(decl)))
        val clamped = term.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clamped)) / 15.0
    }

    private fun asrTime(factor: Double, lat: Double, decl: Double): Double {
        val angle = -Math.toDegrees(atan(1.0 / (factor + tan(Math.toRadians(abs(lat - decl))))))
        return sunAngleTime(angle, lat, decl)
    }

    private fun fixAngle(a: Double): Double = (a % 360.0 + 360.0) % 360.0
    private fun fixHour(h: Double): Double = (h % 24.0 + 24.0) % 24.0

    fun formatHour(hoursDecimal: Double): String {
        if (hoursDecimal.isNaN()) return "--:--"
        val fixed = fixHour(hoursDecimal)
        val totalMinutes = (fixed * 60.0).roundToInt()
        val h24 = (totalMinutes / 60) % 24
        val m = totalMinutes % 60
        val period = if (h24 >= 12) "PM" else "AM"
        val h12 = when {
            h24 == 0 -> 12
            h24 > 12 -> h24 - 12
            else -> h24
        }
        return String.format("%d:%02d %s", h12, m, period)
    }

    val dailyDuas: List<DuaItem> = listOf(
        DuaItem(
            1, "Morning", "Morning Gratitude",
            "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ",
            "Asbahna wa asbahal-mulku lillah, wal-hamdu lillah.",
            "We have reached the morning and at this very time all sovereignty belongs to Allah, and all praise is for Allah."
        ),
        DuaItem(
            2, "Guidance", "Ease & Clarity",
            "رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي",
            "Rabbi-shrah li sadri wa yassir li amri.",
            "My Lord, expand for me my chest and ease for me my task."
        ),
        DuaItem(
            3, "Knowledge", "Beneficial Knowledge",
            "رَبِّ زِدْنِي عِلْمًا",
            "Rabbi zidni 'ilma.",
            "My Lord, increase me in knowledge."
        ),
        DuaItem(
            4, "Evening", "Evening Protection",
            "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ",
            "Amsayna wa amsal-mulku lillah, wal-hamdu lillah.",
            "We have reached the evening and at this very time all sovereignty belongs to Allah, and all praise is for Allah."
        ),
        DuaItem(
            5, "Home", "Entering Home",
            "بِسْمِ اللَّهِ وَلَجْنَا، وَبِسْمِ اللَّهِ خَرَجْنَا، وَعَلَى اللَّهِ رَبِّنَا تَوَكَّلْنَا",
            "Bismillahi walajna, wa bismillahi kharajna, wa 'ala Allahi rabbina tawakkalna.",
            "In the name of Allah we enter, in the name of Allah we leave, and upon Allah our Lord we rely."
        ),
        DuaItem(
            6, "Travel", "Safe Journey",
            "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ",
            "Subhanal-ladhi sakhkhara lana hadha wa ma kunna lahu muqrinin.",
            "Glory to Him who has subjected this to us, and we could never have it by our own efforts."
        ),
        DuaItem(
            7, "Sleep", "Before Sleeping",
            "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
            "Bismika Allahumma amutu wa ahya.",
            "In Your name, O Allah, I die and I live."
        ),
        DuaItem(
            8, "Gratitude", "Wellbeing & Peace",
            "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالْآخِرَةِ",
            "Allahumma inni as'alukal-'afwa wal-'afiyata fid-dunya wal-akhirah.",
            "O Allah, I ask You for pardon and well-being in this life and the next."
        )
    )
}
