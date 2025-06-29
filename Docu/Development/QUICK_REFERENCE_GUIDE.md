# Fairr Project - Quick Reference Guide

## 🚀 Quick Start Commands

### Build & Test
```bash
# Run all tests
./gradlew test

# Build project
./gradlew build

# Clean and rebuild
./gradlew clean build

# Run specific test
./gradlew test --tests "ClassName"
```

### Development
```bash
# Check for lint issues
./gradlew lint

# Assemble debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

## 📁 Key File Locations

### Core Files
- **Main Activity**: `app/src/main/java/com/example/fairr/MainActivity.kt`
- **Application**: `app/src/main/java/com/example/fairr/FairrApplication.kt`
- **Navigation**: `app/src/main/java/com/example/fairr/navigation/FairrNavGraph.kt`
- **Build Config**: `app/build.gradle.kts`

### Documentation
- **Master TODO**: `Docu/TODO_MASTER_LIST.md`
- **Architecture**: `Docu/Architecture/ARCHITECTURE_OVERVIEW.md`
- **Training Insights**: `Docu/Development/FAIRR_SPECIFIC_TRAINING_INSIGHTS.md`

## 🔧 Common Code Patterns

### Service Layer
```kotlin
@Singleton
class ServiceName @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun operation(): Result<Type> {
        return try {
            // Implementation
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### ViewModel
```kotlin
@HiltViewModel
class ViewModelName @Inject constructor(
    private val service: ServiceName
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    fun action() {
        viewModelScope.launch {
            // Implementation
        }
    }
}
```

### UI State
```kotlin
data class UiState(
    val data: List<Type> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

## 🎨 UI Components

### Common Icons (AutoMirrored)
```kotlin
// Navigation
Icons.AutoMirrored.Default.ArrowBack
Icons.AutoMirrored.Default.ArrowForward

// Actions
Icons.AutoMirrored.Default.CallSplit
Icons.AutoMirrored.Default.Add
Icons.AutoMirrored.Default.Edit
Icons.AutoMirrored.Default.Delete

// Status
Icons.AutoMirrored.Default.Check
Icons.AutoMirrored.Default.Close
```

### Common Composables
```kotlin
// Loading state
if (isLoading) {
    CircularProgressIndicator()
}

// Error state
error?.let { errorMessage ->
    Text(
        text = errorMessage,
        color = MaterialTheme.colorScheme.error
    )
}

// Empty state
if (data.isEmpty()) {
    Text("No data available")
}
```

## 🔥 Firebase Integration

### Firestore Collections
- `users` - User profiles
- `groups` - Expense groups
- `expenses` - Expense records
- `settlements` - Settlement records
- `activities` - Activity feed

### Common Firestore Operations
```kotlin
// Get document
val doc = firestore.collection("collection").document("id").get().await()

// Query documents
val docs = firestore.collection("collection")
    .whereEqualTo("field", value)
    .get()
    .await()

// Add document
firestore.collection("collection").add(data).await()

// Update document
firestore.collection("collection").document("id").update(data).await()
```

## 🧪 Testing

### Test Structure
```kotlin
@RunWith(MockitoJUnitRunner::class)
class ViewModelTest {
    @Mock
    private lateinit var service: ServiceName
    
    @InjectMocks
    private lateinit var viewModel: ViewModelName
    
    @Test
    fun `test function name`() {
        // Given
        // When
        // Then
    }
}
```

### Common Test Patterns
```kotlin
// Mock service response
whenever(service.operation()).thenReturn(Result.success(data))

// Verify function calls
verify(service).operation()

// Test UI state changes
assertEquals(expectedState, viewModel.uiState.value)
```

## 🐛 Common Issues & Solutions

### Icon Deprecation Warnings
**Problem**: Material 3 icons showing deprecation warnings
**Solution**: Replace with AutoMirrored versions
```kotlin
// Before
Icon(Icons.Default.ArrowBack, contentDescription = null)

// After
Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
```

### Test Compilation Errors
**Problem**: Tests failing to compile
**Solution**: 
1. Check test dependencies in `build.gradle.kts`
2. Ensure proper imports
3. Run `./gradlew test` to see specific errors

### Firebase Connection Issues
**Problem**: Firebase services not working
**Solution**:
1. Check `google-services.json` configuration
2. Verify Firebase initialization in `FairrApplication.kt`
3. Check Firestore rules and indexes

## 📝 Documentation Updates

### When Making Changes
1. Update relevant TODO items in `Docu/TODO_MASTER_LIST.md`
2. Update this quick reference if adding new patterns
3. Update training insights if discovering new patterns
4. Update README if changing setup requirements

### TODO List Format
```markdown
## [Feature Name]
- [ ] Task 1
- [ ] Task 2
- [x] Completed task

### Status: In Progress
### Priority: High/Medium/Low
### Notes: Additional context
```

## 🎯 Development Workflow

### 1. Analysis Phase
- Review current TODO list
- Understand existing implementation
- Identify what needs to be changed

### 2. Implementation Phase
- Follow established patterns
- Make small, focused changes
- Test each change

### 3. Validation Phase
- Run all tests
- Check for regressions
- Update documentation

### 4. Cleanup Phase
- Remove unused code
- Update TODO lists
- Commit changes with clear messages

## 📊 Project Status Indicators

### Test Status
- ✅ All tests passing
- ⚠️ Some tests failing
- ❌ Many tests failing

### Build Status
- ✅ Build successful
- ⚠️ Build with warnings
- ❌ Build failed

### Documentation Status
- ✅ Up to date
- ⚠️ Needs updates
- ❌ Outdated

## 🔄 Common Development Tasks

### Adding New Feature
1. Create service class in `data/` directory
2. Create ViewModel in `ui/screens/` directory
3. Create UI screen in `ui/screens/` directory
4. Add navigation in `FairrNavGraph.kt`
5. Write tests
6. Update documentation

### Fixing Bug
1. Reproduce the issue
2. Identify root cause
3. Fix following established patterns
4. Add test to prevent regression
5. Update documentation if needed

### Refactoring Code
1. Understand current implementation
2. Plan refactoring carefully
3. Make incremental changes
4. Test thoroughly after each change
5. Update documentation

## 📞 Getting Help

### When Stuck
1. Check this quick reference
2. Review training insights document
3. Look at similar implementations in the codebase
4. Run tests to understand expected behavior
5. Check TODO list for context

### Useful Files for Reference
- `Docu/Development/FAIRR_SPECIFIC_TRAINING_INSIGHTS.md` - Detailed patterns and insights
- `Docu/TODO_MASTER_LIST.md` - Current project status
- `Docu/Architecture/ARCHITECTURE_OVERVIEW.md` - System architecture
- `app/build.gradle.kts` - Dependencies and configuration

---

**Last Updated**: [Current Date]
**Version**: 1.0 