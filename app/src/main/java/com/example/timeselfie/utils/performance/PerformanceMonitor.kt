package com.example.timeselfie.utils.performance

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

/**
 * Utility class for monitoring app performance.
 * Only active in debug builds to avoid performance overhead in production.
 */
object PerformanceMonitor {
    
    internal const val TAG = "PerformanceMonitor"
    internal const val DEBUG = true // Set to BuildConfig.DEBUG in production

    /**
     * Check if debug mode is enabled.
     */
    fun isDebugEnabled(): Boolean = DEBUG

    /**
     * Log a performance message.
     */
    fun logPerformance(message: String) {
        Log.d(TAG, message)
    }
    
    /**
     * Measure the execution time of a suspend function.
     */
    suspend inline fun <T> measureSuspend(
        operation: String,
        crossinline block: suspend () -> T
    ): T {
        return if (isDebugEnabled()) {
            var result: T
            val time = measureTimeMillis {
                result = block()
            }
            logPerformance("$operation took ${time}ms")
            result
        } else {
            block()
        }
    }
    
    /**
     * Measure the execution time of a regular function.
     */
    inline fun <T> measure(
        operation: String,
        crossinline block: () -> T
    ): T {
        return if (isDebugEnabled()) {
            var result: T
            val time = measureTimeMillis {
                result = block()
            }
            logPerformance("$operation took ${time}ms")
            result
        } else {
            block()
        }
    }
    
    /**
     * Log memory usage information.
     */
    fun logMemoryUsage(context: String) {
        if (!isDebugEnabled()) return
        
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val availableMemory = maxMemory - usedMemory
        
        Log.d(TAG, """
            Memory Usage - $context:
            Used: ${usedMemory / 1024 / 1024}MB
            Available: ${availableMemory / 1024 / 1024}MB
            Max: ${maxMemory / 1024 / 1024}MB
            Usage: ${(usedMemory * 100 / maxMemory)}%
        """.trimIndent())
    }
    
    /**
     * Monitor database operation performance.
     */
    suspend fun <T> monitorDatabaseOperation(
        operation: String,
        block: suspend () -> T
    ): T = withContext(Dispatchers.IO) {
        measureSuspend("Database: $operation", block)
    }
    
    /**
     * Monitor image processing performance.
     */
    suspend fun <T> monitorImageOperation(
        operation: String,
        block: suspend () -> T
    ): T = withContext(Dispatchers.IO) {
        measureSuspend("Image: $operation", block)
    }
    
    /**
     * Monitor UI operation performance.
     */
    fun <T> monitorUIOperation(
        operation: String,
        block: () -> T
    ): T {
        return measure("UI: $operation", block)
    }
    
    /**
     * Start monitoring a long-running operation.
     */
    fun startOperation(operationId: String): OperationTimer {
        return OperationTimer(operationId)
    }
    
    /**
     * Timer for long-running operations.
     */
    class OperationTimer(private val operationId: String) {
        private val startTime = System.currentTimeMillis()
        
        fun checkpoint(checkpointName: String) {
            if (isDebugEnabled()) {
                val elapsed = System.currentTimeMillis() - startTime
                Log.d(TAG, "$operationId - $checkpointName: ${elapsed}ms")
            }
        }

        fun finish() {
            if (isDebugEnabled()) {
                val totalTime = System.currentTimeMillis() - startTime
                Log.d(TAG, "$operationId completed in ${totalTime}ms")
            }
        }
    }
    
    /**
     * Monitor coroutine scope operations.
     */
    fun CoroutineScope.launchWithMonitoring(
        operation: String,
        block: suspend CoroutineScope.() -> Unit
    ) {
        if (isDebugEnabled()) {
            launch {
                measureSuspend("Coroutine: $operation") {
                    block()
                }
            }
        } else {
            launch(block = block)
        }
    }
}
