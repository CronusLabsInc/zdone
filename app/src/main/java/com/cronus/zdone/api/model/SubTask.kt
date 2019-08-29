package com.cronus.zdone.api.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubTask(
        @JsonProperty("id")
        val id: String,
        @JsonProperty("name")
        val name: String,
        @JsonProperty("service")
        val service: String)
