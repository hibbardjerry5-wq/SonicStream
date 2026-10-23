package com.example.model

data class ScannerChannel(
    val id: String,
    val name: String,
    val department: String,
    val frequencyMhz: String,
    val category: String, // Police, Fire/EMS, Highway, Weather, Aviation
    val isLive: Boolean = true,
    val listenerCount: Int = 124,
    val streamUrl: String = "",
    val location: String = "Metro Area",
    val bitrateKbps: Int = 32,
    val isFavorite: Boolean = false,
    val signalStrengthBars: Int = 5,
    val agencyType: String = "Municipal",
    val isCustom: Boolean = false
)

data class DispatchTransmission(
    val channelId: String,
    val unitCallSign: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMs: Long = 4000L,
    val code10: String = "10-4",
    val priorityLevel: String = "Routine"
)

enum class ScannerStreamState {
    IDLE,
    CONNECTING,
    BUFFERING,
    STREAMING,
    TRANSMITTING,
    PAUSED,
    ERROR
}

object PublicSafetyFeeds {
    const val DISCLAIMER = "Unofficial public safety broadcast feeds for informational and situational awareness only. In an emergency, always dial 911 directly."

    val channels = listOf(
        ScannerChannel(
            id = "ch_chicago_police",
            name = "Chicago Police - Zones 1 & 2",
            department = "CPD 16th & 17th Districts Dispatch",
            frequencyMhz = "460.025 MHz",
            category = "Police",
            listenerCount = 1845,
            streamUrl = "https://broadcastify.cdnstream1.com/9553",
            location = "Chicago, IL",
            bitrateKbps = 32,
            signalStrengthBars = 5,
            agencyType = "Police Dept"
        ),
        ScannerChannel(
            id = "ch_lapd_south",
            name = "LAPD - South Bureau Dispatch",
            department = "Los Angeles Police Department Tac 2",
            frequencyMhz = "484.437 MHz",
            category = "Police",
            listenerCount = 1420,
            streamUrl = "https://broadcastify.cdnstream1.com/20296",
            location = "Los Angeles, CA",
            bitrateKbps = 32,
            signalStrengthBars = 5,
            agencyType = "Police Dept"
        ),
        ScannerChannel(
            id = "ch_fdny_manhattan",
            name = "FDNY - Manhattan Fire & Rescue",
            department = "Fire Department City of New York",
            frequencyMhz = "154.250 MHz",
            category = "Fire/EMS",
            listenerCount = 980,
            streamUrl = "https://broadcastify.cdnstream1.com/935",
            location = "New York, NY",
            bitrateKbps = 32,
            signalStrengthBars = 5,
            agencyType = "Fire & Rescue"
        ),
        ScannerChannel(
            id = "ch_metro_police",
            name = "Metro Police Dispatch",
            department = "City Police Dept - Central & Traffic",
            frequencyMhz = "460.125 MHz",
            category = "Police",
            listenerCount = 428,
            streamUrl = "https://broadcastify.cdnstream1.com/39316",
            location = "Central Metro",
            bitrateKbps = 32,
            signalStrengthBars = 5,
            agencyType = "Police Dept"
        ),
        ScannerChannel(
            id = "ch_county_sheriff",
            name = "County Sheriff Patrol",
            department = "Sheriff Office Operations & Tactical",
            frequencyMhz = "453.250 MHz",
            category = "Police",
            listenerCount = 215,
            streamUrl = "https://broadcastify.cdnstream1.com/3257",
            location = "County Wide",
            bitrateKbps = 32,
            signalStrengthBars = 4,
            agencyType = "Sheriff Office"
        ),
        ScannerChannel(
            id = "ch_fire_ems",
            name = "Fire Rescue & Emergency Medic",
            department = "County Fire / Paramedic Dispatch",
            frequencyMhz = "154.385 MHz",
            category = "Fire/EMS",
            listenerCount = 310,
            streamUrl = "https://broadcastify.cdnstream1.com/15234",
            location = "Regional Dispatch",
            bitrateKbps = 32,
            signalStrengthBars = 5,
            agencyType = "Fire/EMS"
        ),
        ScannerChannel(
            id = "ch_houston_emergency",
            name = "Houston Fire & EMS Dispatch",
            department = "Houston Fire Department Operations",
            frequencyMhz = "853.375 MHz",
            category = "Fire/EMS",
            listenerCount = 670,
            streamUrl = "https://broadcastify.cdnstream1.com/5144",
            location = "Houston, TX",
            bitrateKbps = 32,
            signalStrengthBars = 4,
            agencyType = "Fire/EMS"
        ),
        ScannerChannel(
            id = "ch_highway_patrol",
            name = "State Highway Patrol - Interstate",
            department = "Highway Patrol Traffic Enforcement",
            frequencyMhz = "851.012 MHz",
            category = "Highway",
            listenerCount = 312,
            streamUrl = "https://broadcastify.cdnstream1.com/19904",
            location = "Interstate Corridor",
            bitrateKbps = 32,
            signalStrengthBars = 4,
            agencyType = "State Police"
        ),
        ScannerChannel(
            id = "ch_noaa_alert",
            name = "NOAA All-Hazards Emergency Weather",
            department = "National Weather Service KWO39",
            frequencyMhz = "162.550 MHz",
            category = "Weather",
            listenerCount = 520,
            streamUrl = "https://stream.weather.gov/chicago",
            location = "Great Lakes Region",
            bitrateKbps = 64,
            signalStrengthBars = 5,
            agencyType = "Federal / NWS"
        ),
        ScannerChannel(
            id = "ch_aviation_tower",
            name = "International Airport Tower & Ground",
            department = "Federal Aviation Administration ATC",
            frequencyMhz = "118.700 MHz",
            category = "Aviation",
            listenerCount = 440,
            streamUrl = "https://broadcastify.cdnstream1.com/13101",
            location = "Metro International (ORD)",
            bitrateKbps = 32,
            signalStrengthBars = 5,
            agencyType = "Aviation / ATC"
        )
    )

    val sampleTransmissions = listOf(
        DispatchTransmission("ch_chicago_police", "Squad 1621", "Dispatch, 10-4, traffic stop at Belmont & Cicero, running plate now.", code10 = "10-4", priorityLevel = "Traffic"),
        DispatchTransmission("ch_chicago_police", "Control-Zone 1", "All units be advised: 10-43 assist motorist on expressway ramp, shoulder clear.", code10 = "10-43", priorityLevel = "Routine"),
        DispatchTransmission("ch_lapd_south", "Unit 7-Adam-14", "South division, code 4 on the investigation at 108th, resuming patrol.", code10 = "Code 4", priorityLevel = "Clear"),
        DispatchTransmission("ch_fdny_manhattan", "Battalion 4", "Battalion 4 to Manhattan: Box 0842, slight smoke on 4th floor, using 1 and 1.", code10 = "10-75", priorityLevel = "Fire Priority"),
        DispatchTransmission("ch_metro_police", "Unit 12-Adam", "Dispatch, 10-4, traffic check on Grand Ave, license verified valid.", code10 = "10-4", priorityLevel = "Traffic"),
        DispatchTransmission("ch_metro_police", "Control", "All units, be advised: stalled vehicle blocking right lane on Highway 101.", code10 = "10-33", priorityLevel = "Advisory"),
        DispatchTransmission("ch_county_sheriff", "Sheriff-7", "En route to assist motorist, 10-76, ETA three minutes.", code10 = "10-76", priorityLevel = "En Route"),
        DispatchTransmission("ch_fire_ems", "Engine 4", "Engine 4 on scene, residential medical response, patient stable.", code10 = "10-23", priorityLevel = "Medical"),
        DispatchTransmission("ch_houston_emergency", "Medic 28", "Medic 28 en route to trauma center with one priority two, ETA 8 minutes.", code10 = "10-76", priorityLevel = "EMS Priority"),
        DispatchTransmission("ch_highway_patrol", "Troop B-9", "Clear on the interstate inspection, road hazard cleared, sector 4 normal.", code10 = "10-98", priorityLevel = "Clear"),
        DispatchTransmission("ch_noaa_alert", "NOAA-Chicago", "National Weather Service bulletin: barometer 30.12 rising, wind 9 knots west.", code10 = "WX-Alert", priorityLevel = "Weather Alert"),
        DispatchTransmission("ch_aviation_tower", "Tower Control", "United 421 heavy, wind 280 at 12, runway 28 center, cleared to land.", code10 = "ATC-CLR", priorityLevel = "Approach")
    )
}
