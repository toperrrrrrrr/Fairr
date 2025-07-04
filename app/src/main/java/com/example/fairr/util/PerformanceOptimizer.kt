package com.example.fairr.util

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext
import com.google.firebase.firestore.ListenerRegistration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.system.measureTimeMillis

/**
 * Performance optimization utility for Fairr app
 * Handles memory management, coroutine cleanup, animation optimization, and performance monitoring
 */
object PerformanceOptimizer {
    
    private const val TAG = "PerformanceOptimizer"
    
    // Memory tracking
    private val activeCoroutines = AtomicLong(0)
    private val activeListeners = ConcurrentHashMap<String, ListenerRegistration>()
    private val performanceMetrics = ConcurrentHashMap<String, Long>()
    private val animationRegistry = ConcurrentHashMap<String, Boolean>()
    
    // Animation optimization settings
    private var reducedMotionEnabled = false
    private var animationScaleFactor = 1.0f
    
    /**
     * Enable reduced motion for performance optimization
     */
    fun enableReducedMotion(enabled: Boolean) {
        reducedMotionEnabled = enabled
        animationScaleFactor = if (enabled) 0.5f else 1.0f
        Log.i(TAG, "Reduced motion: $enabled, Animation scale: $animationScaleFactor")
    }
    
    /**
     * Get optimized animation duration based on performance settings
     */
    fun getOptimizedAnimationDuration(originalDuration: Int): Int {
        return (originalDuration * animationScaleFactor).toInt()
    }
    
    /**
     * Register an animation for performance tracking
     */
    fun registerAnimation(animationId: String) {
        animationRegistry[animationId] = true
        Log.d(TAG, "Registered animation: $animationId (Total: ${animationRegistry.size})")
        
        // Warn if too many animations are active
        if (animationRegistry.size > 10) {
            Log.w(TAG, "High number of active animations: ${animationRegistry.size}")
        }
    }
    
    /**
     * Unregister an animation
     */
    fun unregisterAnimation(animationId: String) {
        animationRegistry.remove(animationId)
        Log.d(TAG, "Unregistered animation: $animationId (Remaining: ${animationRegistry.size})")
    }
    
    /**
     * Creates a memory-safe coroutine scope for ViewModels
     */
    fun ViewModel.createOptimizedScope(): CoroutineScope = 
        viewModelScope + SupervisorJob() + Dispatchers.Main.immediate
    
    /**
     * Launches a coroutine with automatic cleanup and error handling
     */
    fun ViewModel.launchOptimized(
        context: CoroutineContext = Dispatchers.Main,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        activeCoroutines.incrementAndGet()
        
        return viewModelScope.launch(context, start) {
            try {
                block()
            } catch (e: CancellationException) {
                Log.d(TAG, "Coroutine cancelled gracefully")
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error in optimized coroutine", e)
            } finally {
                activeCoroutines.decrementAndGet()
            }
        }
    }
    
    /**
     * Collects a Flow with lifecycle awareness and automatic cleanup
     */
    fun <T> ViewModel.collectSafely(
        flow: Flow<T>,
        onError: (Throwable) -> Unit = { Log.e(TAG, "Flow collection error", it) },
        onEach: (T) -> Unit
    ): Job {
        return launchOptimized {
            flow
                .catch { throwable ->
                    onError(throwable)
                }
                .collect { value ->
                    onEach(value)
                }
        }
    }
    
    /**
     * Registers a Firestore listener with automatic cleanup
     */
    fun ViewModel.registerListener(
        listenerId: String,
        listener: ListenerRegistration,
        onCleanup: () -> Unit = {}
    ) {
        // Remove existing listener if any
        activeListeners[listenerId]?.remove()
        
        // Register new listener
        activeListeners[listenerId] = listener
        
        Log.d(TAG, "Registered listener: $listenerId (Total: ${activeListeners.size})")
    }
    
    /**
     * Removes a specific Firestore listener
     */
    fun removeListener(listenerId: String) {
        activeListeners.remove(listenerId)?.let { listener ->
            listener.remove()
            Log.d(TAG, "Removed listener: $listenerId (Remaining: ${activeListeners.size})")
        }
    }
    
    /**
     * Cleans up all resources for a ViewModel
     */
    fun ViewModel.cleanup() {
        // Remove all listeners for this ViewModel
        val viewModelListeners = activeListeners.keys.filter { 
            it.startsWith(this::class.simpleName ?: "ViewModel") 
        }
        
        viewModelListeners.forEach { listenerId ->
            removeListener(listenerId)
        }
        
        Log.d(TAG, "Cleaned up resources for ${this::class.simpleName}")
    }
    
    /**
     * Measures execution time of a block and logs performance metrics
     */
    suspend fun <T> measurePerformance(
        operationName: String,
        logResult: Boolean = true,
        block: suspend () -> T
    ): T {
        val result: T
        val executionTime = measureTimeMillis {
            result = block()
        }
        
        performanceMetrics[operationName] = executionTime
        
        if (logResult) {
            Log.d(TAG, "Performance: $operationName took ${executionTime}ms")
        }
        
        return result
    }
    
    /**
     * Creates an optimized Flow with memory management
     */
    fun <T> createOptimizedFlow(
        block: suspend FlowCollector<T>.() -> Unit
    ): Flow<T> = flow(block)
        .flowOn(Dispatchers.IO)
        .conflate() // Drop intermediate values if collector is slow
        .catch { e ->
            Log.e(TAG, "Error in optimized flow", e)
            // Emit default value or handle error gracefully
        }
    
    /**
     * Debounces a Flow to prevent excessive operations
     */
    fun <T> Flow<T>.debounceOptimized(timeoutMillis: Long = 300): Flow<T> =
        debounce(timeoutMillis)
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    
    /**
     * Combines multiple flows with memory optimization
     */
    fun <T1, T2, R> combineOptimized(
        flow1: Flow<T1>,
        flow2: Flow<T2>,
        transform: suspend (T1, T2) -> R
    ): Flow<R> = combine(flow1, flow2, transform)
        .flowOn(Dispatchers.Default)
        .conflate()
    
    /**
     * Creates a state flow with initial value and memory optimization
     */
    fun <T> createOptimizedStateFlow(
        initialValue: T,
        scope: CoroutineScope = GlobalScope
    ): MutableStateFlow<T> = MutableStateFlow(initialValue)
    
    /**
     * Optimized image loading with memory management
     */
    suspend fun optimizeImageLoading(
        imageLoadBlock: suspend () -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            // Ensure we're on IO dispatcher for image operations
            imageLoadBlock()
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "OutOfMemoryError during image loading", e)
            // Trigger garbage collection
            System.gc()
            // Could also implement image compression fallback here
        } catch (e: Exception) {
            Log.e(TAG, "Error during optimized image loading", e)
        }
    }
    
    /**
     * Batch operation optimization for Firestore
     */
    suspend fun <T> optimizeBatchOperation(
        items: List<T>,
        batchSize: Int = 10,
        operation: suspend (List<T>) -> Unit
    ) = withContext(Dispatchers.IO) {
        items.chunked(batchSize).forEach { batch ->
            try {
                operation(batch)
                // Small delay between batches to prevent overwhelming Firestore
                delay(50)
            } catch (e: Exception) {
                Log.e(TAG, "Error in batch operation", e)
            }
        }
    }
    
    /**
     * Memory-safe cache implementation
     */
    class MemorySafeCache<K, V>(
        private val maxSize: Int = 100,
        private val evictionTimeMs: Long = 300_000 // 5 minutes
    ) {
        private val cache = ConcurrentHashMap<K, CacheEntry<V>>()
        private val accessTimes = ConcurrentHashMap<K, Long>()
        
        data class CacheEntry<V>(
            val value: V,
            val timestamp: Long = System.currentTimeMillis()
        )
        
        fun get(key: K): V? {
            val entry = cache[key] ?: return null
            
            // Check if entry has expired
            if (System.currentTimeMillis() - entry.timestamp > evictionTimeMs) {
                remove(key)
                return null
            }
            
            accessTimes[key] = System.currentTimeMillis()
            return entry.value
        }
        
        fun put(key: K, value: V) {
            // Evict old entries if cache is full
            if (cache.size >= maxSize) {
                evictOldest()
            }
            
            cache[key] = CacheEntry(value)
            accessTimes[key] = System.currentTimeMillis()
        }
        
        fun remove(key: K): V? {
            accessTimes.remove(key)
            return cache.remove(key)?.value
        }
        
        fun clear() {
            cache.clear()
            accessTimes.clear()
        }
        
        private fun evictOldest() {
            val oldestKey = accessTimes.minByOrNull { it.value }?.key
            oldestKey?.let { remove(it) }
        }
        
        fun size() = cache.size
    }
    
    /**
     * Get current performance statistics
     */
    fun getPerformanceStats(): Map<String, Any> = mapOf(
        "activeCoroutines" to activeCoroutines.get(),
        "activeListeners" to activeListeners.size,
        "performanceMetrics" to performanceMetrics.toMap(),
        "memoryUsage" to getMemoryUsage()
    )
    
    /**
     * Get current memory usage
     */
    private fun getMemoryUsage(): Map<String, Long> {
        val runtime = Runtime.getRuntime()
        return mapOf(
            "totalMemory" to runtime.totalMemory(),
            "freeMemory" to runtime.freeMemory(),
            "usedMemory" to (runtime.totalMemory() - runtime.freeMemory()),
            "maxMemory" to runtime.maxMemory()
        )
    }
    
    /**
     * Log performance statistics
     */
    fun logPerformanceStats() {
        val stats = getPerformanceStats()
        Log.i(TAG, "Performance Stats: $stats")
        
        // Warn if too many resources are active
        if (activeCoroutines.get() > 20) {
            Log.w(TAG, "High number of active coroutines: ${activeCoroutines.get()}")
        }
        
        if (activeListeners.size > 10) {
            Log.w(TAG, "High number of active listeners: ${activeListeners.size}")
        }
    }
    
    /**
     * Force cleanup of all resources (use sparingly)
     */
    fun forceCleanup() {
        Log.w(TAG, "Forcing cleanup of all resources")
        
        // Remove all listeners
        activeListeners.values.forEach { it.remove() }
        activeListeners.clear()
        
        // Clear performance metrics
        performanceMetrics.clear()
        
        // Suggest garbage collection
        System.gc()
        
        Log.i(TAG, "Force cleanup completed")
    }
} 