package com.pissiphany.bany.plugin.cibc.dataStructure

data class AuthRequest(val card: Card, val password: String) {
    data class Card(val value: String, val description: String, val encrypted: Boolean, val encrypt: Boolean)
}
