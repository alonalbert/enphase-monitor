package com.alonalbert.enphase.monitor.enphase

data class Credentials(val email: String, val password: String)

fun interface CredentialsProvider {
  suspend fun getCredentials(): Credentials
}
