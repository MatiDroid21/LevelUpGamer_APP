package model.mappers

import data.local.RecordatorioEntity
import model.Recordatorio

fun RecordatorioEntity.toDto() = Recordatorio(
    id = id,
    uid = uid,
    createdAt = createdAt,
    message = message
)

fun Recordatorio.toEntity() = RecordatorioEntity(
    id = id,
    uid = uid,
    createdAt = createdAt,
    message = message
)
