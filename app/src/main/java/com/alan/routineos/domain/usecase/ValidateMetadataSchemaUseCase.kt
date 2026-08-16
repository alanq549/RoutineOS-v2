package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.MetadataSchema
import javax.inject.Inject

sealed class MetadataSchemaValidationError(val userMessage: String) {
    object DuplicateFieldName : MetadataSchemaValidationError("No puedes tener dos campos con el mismo nombre")
    object EmptyFieldName : MetadataSchemaValidationError("El nombre del campo no puede estar vacío")
}

class ValidateMetadataSchemaUseCase @Inject constructor() {

    operator fun invoke(schema: MetadataSchema): MetadataSchemaValidationError? {
        if (schema.fields.any { it.name.isBlank() }) {
            return MetadataSchemaValidationError.EmptyFieldName
        }

        val names = schema.fields.map { it.name.trim().lowercase() }
        if (names.size != names.distinct().size) {
            return MetadataSchemaValidationError.DuplicateFieldName
        }

        return null
    }
}
