package com.pissiphany.bany.shared

import mu.KLogger
import mu.KotlinLogging

// https://stackoverflow.com/a/34462577
fun <T : Any> T.logger(): Lazy<KLogger> {
    return lazy { KotlinLogging.logger(this.javaClass.name) }
}