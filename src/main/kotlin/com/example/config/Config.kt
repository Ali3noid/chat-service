package com.example.config

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*

val config: ApplicationConfig = HoconApplicationConfig(ConfigFactory.load())