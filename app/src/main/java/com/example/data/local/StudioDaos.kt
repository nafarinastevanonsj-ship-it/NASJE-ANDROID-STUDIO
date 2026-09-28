package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BuildHistoryEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY lastModified DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)
}

@Dao
interface ProjectFileDao {
    @Query("SELECT * FROM project_files WHERE projectId = :projectId ORDER BY filePath ASC")
    fun getFilesForProject(projectId: Long): Flow<List<ProjectFileEntity>>

    @Query("SELECT * FROM project_files WHERE projectId = :projectId AND filePath = :path LIMIT 1")
    suspend fun getFileByPath(projectId: Long, path: String): ProjectFileEntity?

    @Query("SELECT * FROM project_files WHERE id = :fileId LIMIT 1")
    suspend fun getFileById(fileId: Long): ProjectFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<ProjectFileEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: ProjectFileEntity): Long

    @Update
    suspend fun updateFile(file: ProjectFileEntity)

    @Delete
    suspend fun deleteFile(file: ProjectFileEntity)

    @Query("DELETE FROM project_files WHERE projectId = :projectId")
    suspend fun deleteAllFilesForProject(projectId: Long)
}

@Dao
interface BuildHistoryDao {
    @Query("SELECT * FROM build_history WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getHistoryForProject(projectId: Long): Flow<List<BuildHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildHistory(history: BuildHistoryEntity): Long
}
