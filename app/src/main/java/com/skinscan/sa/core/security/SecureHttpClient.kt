package com.skinscan.sa.core.security

import com.skinscan.sa.BuildConfig
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure HTTP Client Configuration (Story 6.2)
 *
 * Provides OkHttp client with:
 * - Certificate pinning infrastructure (pins added when real APIs integrated)
 * - Connection timeouts
 * - Logging for debug builds only
 *
 * NFR-SEC04: Certificate pinning for MITM protection
 */
@Singleton
class SecureHttpClient @Inject constructor() {

    companion object {
        private const val CONNECT_TIMEOUT_SECONDS = 30L
        private const val READ_TIMEOUT_SECONDS = 30L
        private const val WRITE_TIMEOUT_SECONDS = 30L

        // Placeholder pins for future Clicks API integration
        // To obtain pins when API is available:
        // echo | openssl s_client -servername api.clicks.co.za -connect api.clicks.co.za:443 2>/dev/null \
        //     | openssl x509 -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | base64
        private val CLICKS_API_PINS = emptyList<String>()
        // When real API is available:
        // private val CLICKS_API_PINS = listOf(
        //     "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        //     "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="
        // )
    }

    /**
     * Build secure OkHttp client
     */
    fun buildClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        // Add certificate pinner if pins are configured
        if (CLICKS_API_PINS.isNotEmpty()) {
            builder.certificatePinner(buildCertificatePinner())
        }

        // Add logging interceptor for debug builds only
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    /**
     * Build certificate pinner with configured pins
     */
    private fun buildCertificatePinner(): CertificatePinner {
        val pinnerBuilder = CertificatePinner.Builder()

        // Add Clicks API pins when available
        CLICKS_API_PINS.forEach { pin ->
            pinnerBuilder.add("api.clicks.co.za", pin)
        }

        return pinnerBuilder.build()
    }

    /**
     * Validate that certificate pinning is working
     * Call this in security tests to verify MITM protection
     */
    fun isPinningEnabled(): Boolean {
        return CLICKS_API_PINS.isNotEmpty()
    }
}
