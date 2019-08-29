package com.cronus.zdone.api.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Task(
        @JsonProperty("id")
        val id: String,
        @JsonProperty("name")
        val name: String,
        @JsonProperty("sub_tasks")
        val subtasks: List<SubTask>?,
        @JsonProperty("service")
        val service: String,
        @JsonProperty("length_minutes")
        val lengthMins: Int
)