package com.pissiphany.bany.dataStructure

import com.pissiphany.bany.adapter.dataStructure.YnabCredentials

class BanyConfig(val ynabApiToken: String, val plugins: Map<String, List<YnabCredentials>>)
