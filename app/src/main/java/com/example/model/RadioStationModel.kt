package com.example.model

data class RadioStation(
    val stationUuid: String,
    val name: String,
    val streamUrl: String,
    val genre: String,
    val country: String = "Global",
    val bitrateKbps: Int = 128,
    val codec: String = "MP3",
    val faviconUrl: String = "",
    val votes: Int = 1200,
    val isFavorite: Boolean = false,
    val tags: List<String> = emptyList()
)

data class SpotifyTrackItem(
    val id: String,
    val name: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val previewUrl: String?,
    val albumArtUrl: String?,
    val spotifyUri: String,
    val popularity: Int = 80
)

data class AppleMusicTrackItem(
    val id: String,
    val name: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val previewUrl: String?,
    val albumArtUrl: String?,
    val appleMusicUrl: String,
    val isSpatialAudio: Boolean = true,
    val isLossless: Boolean = true
)

data class SpotifyAuthToken(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresInSeconds: Int = 3600,
    val obtainedAtMs: Long = System.currentTimeMillis()
) {
    val isExpired: Boolean
        get() = (System.currentTimeMillis() - obtainedAtMs) > (expiresInSeconds - 60) * 1000L
}

object CuratedRadioStations {
    val stations = listOf(
        RadioStation(
            stationUuid = "rad_somafm_groove",
            name = "SomaFM: Groove Salad",
            streamUrl = "https://ice1.somafm.com/groovesalad-128-mp3",
            genre = "Chillout / Ambient",
            country = "United States",
            bitrateKbps = 128,
            votes = 9840,
            tags = listOf("chill", "ambient", "downtempo", "electronic")
        ),
        RadioStation(
            stationUuid = "rad_defcon",
            name = "SomaFM: DEF CON Radio",
            streamUrl = "https://ice1.somafm.com/defcon-128-mp3",
            genre = "Cyber / Synthwave",
            country = "United States",
            bitrateKbps = 128,
            votes = 8520,
            tags = listOf("synthwave", "cyberpunk", "hacker", "electronic")
        ),
        RadioStation(
            stationUuid = "rad_bbc_one",
            name = "BBC Radio 1 UK",
            streamUrl = "https://stream.live.vc.bbcmedia.co.uk/bbc_radio_one",
            genre = "Top 40 / Pop",
            country = "United Kingdom",
            bitrateKbps = 128,
            votes = 15200,
            tags = listOf("pop", "hits", "dance", "top40")
        ),
        RadioStation(
            stationUuid = "rad_kexp_seattle",
            name = "KEXP 90.3 FM Seattle",
            streamUrl = "https://kexp.streamguys1.com/kexp128.mp3",
            genre = "Indie / Alternative",
            country = "United States",
            bitrateKbps = 128,
            votes = 11450,
            tags = listOf("indie", "rock", "alternative", "live")
        ),
        RadioStation(
            stationUuid = "rad_ibiza_global",
            name = "Ibiza Global Radio",
            streamUrl = "https://listenssl.ibizaglobalradio.com:8024/ibizaglobalradio.mp3",
            genre = "Electronic / House",
            country = "Spain",
            bitrateKbps = 128,
            votes = 7800,
            tags = listOf("house", "deep house", "techno", "club")
        ),
        RadioStation(
            stationUuid = "rad_jazz24",
            name = "Jazz24 Live NWPR",
            streamUrl = "https://live.wostreaming.net/manifest/opb-jazz24aac-ibc1.m3u8",
            genre = "Jazz & Blues",
            country = "United States",
            bitrateKbps = 128,
            votes = 6450,
            tags = listOf("jazz", "blues", "classic", "swing")
        ),
        RadioStation(
            stationUuid = "rad_radio_paradise",
            name = "Radio Paradise (Main Mix)",
            streamUrl = "https://stream.radioparadise.com/mp3-128",
            genre = "Eclectic Rock / Mellow",
            country = "United States",
            bitrateKbps = 128,
            votes = 12300,
            tags = listOf("rock", "acoustic", "eclectic", "audiophile")
        ),
        RadioStation(
            stationUuid = "rad_capital_fm",
            name = "Capital FM London",
            streamUrl = "https://media-ice.musicradio.com/CapitalMP3",
            genre = "Top 40 / Dance",
            country = "United Kingdom",
            bitrateKbps = 128,
            votes = 9100,
            tags = listOf("pop", "chart", "hits", "london")
        )
    )

    val sampleSpotifyTracks = listOf(
        SpotifyTrackItem(
            id = "sp_midnight_city",
            name = "Midnight City",
            artist = "M83",
            album = "Hurry Up, We're Dreaming",
            durationMs = 243000L,
            previewUrl = "https://p.scdn.co/mp3-preview/midnight_city_sample",
            albumArtUrl = "https://i.scdn.co/image/ab67616d0000b2734a1a5b82877a5e62bf0788eb",
            spotifyUri = "spotify:track:6GyFP1nfCDB87D2bGGFQAf",
            popularity = 85
        ),
        SpotifyTrackItem(
            id = "sp_starboy",
            name = "Starboy",
            artist = "The Weeknd ft. Daft Punk",
            album = "Starboy",
            durationMs = 230000L,
            previewUrl = "https://p.scdn.co/mp3-preview/starboy_sample",
            albumArtUrl = "https://i.scdn.co/image/ab67616d0000b2734718e2b124f79258be7bc452",
            spotifyUri = "spotify:track:7MXVkk9YM5FZOsWEn09wWI",
            popularity = 92
        ),
        SpotifyTrackItem(
            id = "sp_resonance_home",
            name = "Resonance",
            artist = "HOME",
            album = "Odyssey",
            durationMs = 212000L,
            previewUrl = "https://p.scdn.co/mp3-preview/resonance_home_sample",
            albumArtUrl = "https://i.scdn.co/image/ab67616d0000b273b5443216858e3cbef302d9dc",
            spotifyUri = "spotify:track:1TuopWDI4VsTCv9smje0gu",
            popularity = 88
        ),
        SpotifyTrackItem(
            id = "sp_blinding_lights",
            name = "Blinding Lights",
            artist = "The Weeknd",
            album = "After Hours",
            durationMs = 200000L,
            previewUrl = "https://p.scdn.co/mp3-preview/blinding_lights_sample",
            albumArtUrl = "https://i.scdn.co/image/ab67616d0000b2738863bc11d2aa12b54f5aeb36",
            spotifyUri = "spotify:track:0VjIjW4GlULA75W2aF3e37",
            popularity = 95
        )
    )

    val sampleAppleMusicTracks = listOf(
        AppleMusicTrackItem(
            id = "am_get_lucky",
            name = "Get Lucky (Spatial Audio)",
            artist = "Daft Punk ft. Pharrell Williams",
            album = "Random Access Memories",
            durationMs = 248000L,
            previewUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview116/v4/a4/09/b3/a409b37a-428a-7848-18e5-f55a159942a7/mzaf_10330456108152393275.plus.aac.p.m4a",
            albumArtUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/a3/1f/f7/a31ff7d0-1a0e-bcab-5079-994df5020165/886443927087.jpg/600x600bb.jpg",
            appleMusicUrl = "https://music.apple.com/us/album/get-lucky-feat-pharrell-williams-and-nile-rodgers/617154241?i=617154366",
            isSpatialAudio = true,
            isLossless = true
        ),
        AppleMusicTrackItem(
            id = "am_solar_power",
            name = "Solar Power (Lossless)",
            artist = "Lorde",
            album = "Solar Power",
            durationMs = 192000L,
            previewUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview125/v4/ec/3b/68/ec3b6807-742a-912f-f446-28562d94ee94/mzaf_15781329824630511874.plus.aac.p.m4a",
            albumArtUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/ce/20/df/ce20dfa5-cb07-96a1-f761-4cfa6c41b89d/21UMGIM56456.rgb.jpg/600x600bb.jpg",
            appleMusicUrl = "https://music.apple.com/us/album/solar-power/1572949298?i=1572949301",
            isSpatialAudio = true,
            isLossless = true
        ),
        AppleMusicTrackItem(
            id = "am_cyber_city",
            name = "Days of Thunder (Cyber Mix)",
            artist = "The Midnight",
            album = "Days of Thunder",
            durationMs = 236000L,
            previewUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview115/v4/db/48/44/db484439-0186-b4d7-ea88-46777aa0d65b/mzaf_7240366624510447385.plus.aac.p.m4a",
            albumArtUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music124/v4/95/ec/4c/95ec4ca4-3998-ea2f-1a5c-59b8eb9fe1e5/194491745479.jpg/600x600bb.jpg",
            appleMusicUrl = "https://music.apple.com/us/album/days-of-thunder/1474931980?i=1474931984",
            isSpatialAudio = true,
            isLossless = true
        )
    )
}
