package com.alonalbert.enphase.monitor.util

import com.alonalbert.enphase.monitor.db.EnphaseConfigDao
import com.alonalbert.enphase.monitor.enphase.Credentials
import com.alonalbert.enphase.monitor.enphase.CredentialsProvider

internal class DatabaseCredentialsProvider(private val enphaseConfigDao: EnphaseConfigDao) : CredentialsProvider {
  override suspend fun getCredentials(): Credentials {
    val config = enphaseConfigDao.get() ?: throw IllegalStateException("Missing credentials")
    return Credentials(config.email, config.password)
  }
}