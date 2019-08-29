package com.cronus.zdone.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TaskStatusUpdate(
        val id: String,
        @field:JsonProperty("subtask_id") var subtaskId: String? = null,
        val update: String,
        val service: String)