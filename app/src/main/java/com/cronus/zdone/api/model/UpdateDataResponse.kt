package com.cronus.zdone.api.model

import com.fasterxml.jackson.annotation.JsonProperty

data class UpdateDataResponse(
        @JsonProperty("result")
        val result: String,
        @JsonProperty("reason", required = false)
        val message: String? = null)
