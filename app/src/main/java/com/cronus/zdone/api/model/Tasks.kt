package com.cronus.zdone.api.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty


@JsonIgnoreProperties(ignoreUnknown = true)
data class Tasks(
        @JsonProperty("tasks_to_do")
        val tasksToDo: List<Task>,
        @JsonProperty("times")
        val timeProgress: TimeProgress
)