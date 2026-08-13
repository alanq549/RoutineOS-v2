package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.repository.ActivityRepository
import javax.inject.Inject

/**
 * Restores a specific list of nodes (Undo operation for DeleteBranch).
 */
class RestoreBranchUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    suspend operator fun invoke(nodeIds: List<String>) {
        nodeIds.forEach { id ->
            repository.getNodeById(id)?.let { node ->
                repository.upsertNode(node.copy(isDeleted = false))
            }
        }
    }
}
