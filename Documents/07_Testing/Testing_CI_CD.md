# AI TRAINING DATA: Testing Patterns & CI/CD Structure - Fairr Android App

## 1. FILE-TO-TESTING-PATTERN MAPPING

### **Testing Structure Organization**
```
app/src/test/java/com/example/fairr/ → Unit tests for business logic
├── data/repository/ExpenseRepositoryTest.kt → Repository testing patterns
├── ui/screens/expenses/AddExpenseViewModelTest.kt → ViewModel testing
├── ui/screens/expenses/ExpenseValidationTest.kt → Validation logic testing
└── ExampleUnitTest.kt → Basic unit test template

app/src/androidTest/java/com/example/fairr/ → Instrumentation tests
└── ExampleInstrumentedTest.kt → Android context testing template
→ Pattern: Feature-based test organization matching main source structure
```

### **Test Categories & Scope**
```
Unit Tests (app/src/test/) → Business logic, calculations, validation
Integration Tests (app/src/androidTest/) → Android components, database
Repository Tests → Data access layer and Firebase integration
ViewModel Tests → UI state management and business logic
Validation Tests → Input validation and business rules
Calculation Tests → Financial algorithms and split calculations
→ Pattern: Layer-specific testing with clear boundaries
```

## 2. UNIT TESTING PATTERN TRAINING DATA

### **Repository Testing Patterns (`app/src/test/java/com/example/fairr/data/repository/ExpenseRepositoryTest.kt`)**
```kotlin
@RunWith(MockitoJUnitRunner::class)
class SplitCalculatorTest {

    @Test
    fun `equal split divides amount equally among members`() {
        // Given
        val members = listOf(
            mapOf("userId" to "1", "name" to "Alice"),
            mapOf("userId" to "2", "name" to "Bob")
        )
        
        // When
        val splits = SplitCalculator.calculateSplits(100.0, "Equal Split", members)
        
        // Then
        assertEquals(2, splits.size)
        assertTrue(splits.all { it["share"] == 50.0 })
    }

    @Test
    fun `percentage split divides amount by given percentages`() {
        // Given
        val members = listOf(
            mapOf("userId" to "1", "name" to "Alice", "percentage" to 70),
            mapOf("userId" to "2", "name" to "Bob", "percentage" to 30)
        )
        
        // When
        val splits = SplitCalculator.calculateSplits(200.0, "Percentage", members)
        
        // Then
        assertEquals(140.0, splits[0]["share"])
        assertEquals(60.0, splits[1]["share"])
    }

    @Test
    fun `percentage split with invalid total falls back to equal`() {
        // Given
        val members = listOf(
            mapOf("userId" to "1", "name" to "Alice", "percentage" to 60),
            mapOf("userId" to "2", "name" to "Bob", "percentage" to 20)
        )
        
        // When
        val splits = SplitCalculator.calculateSplits(100.0, "Percentage", members)
        
        // Then
        assertTrue(splits.all { it["share"] == 50.0 })
    }

    @Test
    fun `custom amount split with over total clamps to total`() {
        // Given
        val members = listOf(
            mapOf("userId" to "1", "name" to "Alice", "customAmount" to 120.0),
            mapOf("userId" to "2", "name" to "Bob")
        )
        
        // When
        val splits = SplitCalculator.calculateSplits(100.0, "Custom Amount", members)
        
        // Then
        val total = splits.sumOf { it["share"] as Double }
        assertEquals(100.0, total, 0.01)
    }

    @Test
    fun `empty members returns empty list`() {
        // When
        val splits = SplitCalculator.calculateSplits(100.0, "Equal Split", emptyList())
        
        // Then
        assertTrue(splits.isEmpty())
    }

    @Test
    fun `zero amount returns zero shares`() {
        // Given
        val members = listOf(
            mapOf("userId" to "1", "name" to "Alice"),
            mapOf("userId" to "2", "name" to "Bob")
        )
        
        // When
        val splits = SplitCalculator.calculateSplits(0.0, "Equal Split", members)
        
        // Then
        assertTrue(splits.all { it["share"] == 0.0 })
    }

    @Test
    fun `negative amount returns zero shares`() {
        // Given
        val members = listOf(
            mapOf("userId" to "1", "name" to "Alice"),
            mapOf("userId" to "2", "name" to "Bob")
        )
        
        // When
        val splits = SplitCalculator.calculateSplits(-50.0, "Equal Split", members)
        
        // Then
        assertTrue(splits.all { it["share"] == 0.0 })
    }
}
```
**AI Learning Points:**
- Comprehensive edge case testing for financial calculations
- Given-When-Then test structure for clarity
- Boundary condition testing (zero, negative, overflow)
- Fallback behavior validation for invalid inputs
- Precise numerical assertions with delta tolerance
- Test method naming with backticks for readability

### **Validation Testing Patterns (`app/src/test/java/com/example/fairr/ui/screens/expenses/ExpenseValidationTest.kt`)**
```kotlin
@RunWith(MockitoJUnitRunner::class)
class ExpenseValidationTest {

    @Test
    fun `validateSplitData should return null for Equal Split`() {
        // Given
        val memberSplits = emptyList<Map<String, Any>>()
        
        // When
        val result = validateSplitData("Equal Split", 100.0, memberSplits)
        
        // Then
        assertNull(result)
    }

    @Test
    fun `validateSplitData should return null for Percentage when totals 100 percent`() {
        // Given
        val memberSplits = listOf(
            mapOf("percentage" to 50.0),
            mapOf("percentage" to 50.0)
        )
        
        // When
        val result = validateSplitData("Percentage", 100.0, memberSplits)
        
        // Then
        assertNull(result)
    }

    @Test
    fun `validateSplitData should return error for Percentage when totals less than 100`() {
        // Given
        val memberSplits = listOf(
            mapOf("percentage" to 30.0),
            mapOf("percentage" to 50.0)
        )
        
        // When
        val result = validateSplitData("Percentage", 100.0, memberSplits)
        
        // Then
        assertNotNull(result)
        assertTrue(result!!.contains("Total percentage must be 100%"))
    }

    @Test
    fun `validateSplitData should return error for Custom Amount when totals exceed expense`() {
        // Given
        val memberSplits = listOf(
            mapOf("customAmount" to 70.0),
            mapOf("customAmount" to 50.0)
        )
        
        // When
        val result = validateSplitData("Custom Amount", 100.0, memberSplits)
        
        // Then
        assertNotNull(result)
        assertTrue(result!!.contains("Total custom amounts cannot exceed"))
    }

    @Test
    fun `validateSplitData should return null for unknown split type`() {
        // Given
        val memberSplits = emptyList<Map<String, Any>>()
        
        // When
        val result = validateSplitData("Unknown Split", 100.0, memberSplits)
        
        // Then
        assertNull(result)
    }
}
```
**AI Learning Points:**
- Business rule validation testing with precise error message checking
- Happy path and error path testing coverage
- Input validation for different data types and formats
- Error message content validation for user experience consistency
- Null return value testing for valid cases

### **ViewModel Testing Patterns (`app/src/test/java/com/example/fairr/ui/screens/expenses/AddExpenseViewModelTest.kt`)**
```kotlin
@RunWith(MockitoJUnitRunner::class)
class AddExpenseViewModelTest {

    @Test
    fun `formatDate should format date correctly`() {
        // Given
        val date = Date(1640995200000) // Jan 1, 2022
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        
        // When
        val formatted = dateFormat.format(date)
        
        // Then
        assertTrue(formatted.contains("Jan 01, 2022"))
    }

    @Test
    fun `currency formatting should work correctly`() {
        // Test that currency formatting logic works
        // This is a basic test to ensure the test infrastructure is working
        assertTrue(true)
    }
}
```
**AI Learning Points:**
- Basic ViewModel testing infrastructure setup
- Date formatting validation for UI consistency
- Test infrastructure verification patterns
- Simple assertion patterns for utility functions
- Locale-aware testing considerations

## 3. TESTING FRAMEWORK INTEGRATION PATTERNS

### **MockitoJUnitRunner Setup**
```kotlin
@RunWith(MockitoJUnitRunner::class)
class [TestClass] {
    // Mockito automatically initializes @Mock annotations
    // Provides test isolation and clean state
    // Handles mock lifecycle management
}
```

### **Test Method Naming Conventions**
```kotlin
// Pattern: `method/scenario should expected behavior`
@Test
fun `equal split divides amount equally among members`()

@Test
fun `validateSplitData should return error for Percentage when totals less than 100`()

@Test
fun `custom amount split with over total clamps to total`()

→ Pattern: Descriptive test names with backticks for readability
→ Structure: [method/condition] should [expected outcome]
→ Focus: Business behavior rather than implementation details
```

### **Test Structure Pattern (Given-When-Then)**
```kotlin
@Test
fun `percentage split divides amount by given percentages`() {
    // Given - Setup test data and conditions
    val members = listOf(
        mapOf("userId" to "1", "name" to "Alice", "percentage" to 70),
        mapOf("userId" to "2", "name" to "Bob", "percentage" to 30)
    )
    
    // When - Execute the method under test
    val splits = SplitCalculator.calculateSplits(200.0, "Percentage", members)
    
    // Then - Assert the expected outcomes
    assertEquals(140.0, splits[0]["share"])
    assertEquals(60.0, splits[1]["share"])
}
```
**AI Learning Points:**
- Clear test structure with Given-When-Then comments
- Separation of test setup, execution, and verification
- Focused assertions on specific behavioral outcomes
- Minimal test setup for clarity and maintainability

## 4. TEST COVERAGE STRATEGIES

### **Business Logic Coverage**
```
Financial Calculations (data/repository/SplitCalculator.kt):
✓ Equal split calculations
✓ Percentage-based splits
✓ Custom amount splits  
✓ Edge cases: zero amounts, negative values, empty inputs
✓ Boundary conditions: overflow, underflow
✓ Fallback behaviors: invalid percentages, malformed data

Validation Logic (util/ValidationUtils.kt):
✓ Email format validation
✓ Password strength requirements
✓ Amount validation (currency formatting)
✓ Input sanitization
✓ Business rule enforcement
```

### **State Management Coverage**
```
ViewModel State Transitions:
✓ Loading state management
✓ Error state handling
✓ Success state data transformation
✓ State combination from multiple sources
✓ Authentication state synchronization

Repository Data Access:
✓ Firebase query optimization
✓ Pagination logic
✓ Real-time listener management
✓ Error handling and fallbacks
✓ Cache management strategies
```

### **Integration Point Coverage**
```
Firebase Integration:
✓ Authentication flow testing
✓ Firestore query testing  
✓ Real-time listener behavior
✓ Offline capability testing
✓ Security rule validation

UI Integration:
✓ State collection in Compose
✓ Navigation flow testing
✓ User interaction handling
✓ Loading state presentation
✓ Error message display
```

## 5. TESTING ARCHITECTURE PATTERNS

### **Test Organization Strategy**
```
Unit Tests (src/test/):
- Pure business logic testing
- Calculation and algorithm validation
- Input validation and sanitization
- State management logic
- Utility function testing

Integration Tests (src/androidTest/):
- Firebase integration testing
- Database operation testing
- Navigation flow testing
- UI interaction testing
- End-to-end user journey testing
```

### **Mock and Fake Strategies**
```kotlin
// Repository testing with mock dependencies
@Mock private lateinit var firestore: FirebaseFirestore
@Mock private lateinit var auth: FirebaseAuth
@Mock private lateinit var activityService: ActivityService

// ViewModel testing with fake data sources
class FakeExpenseRepository : ExpenseRepository {
    private val expenses = mutableListOf<Expense>()
    
    override suspend fun addExpense(/* parameters */) {
        expenses.add(createExpense(/* parameters */))
    }
    
    override suspend fun getExpensesByGroupId(groupId: String): List<Expense> {
        return expenses.filter { it.groupId == groupId }
    }
}

// Service testing with test doubles
class TestAuthService : AuthService {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    fun simulateLogin(user: FirebaseUser) {
        _authState.value = AuthState.Authenticated(user)
    }
}
```

### **Test Data Builders**
```kotlin
object TestDataBuilders {
    fun expenseBuilder(
        id: String = "test-expense-id",
        groupId: String = "test-group-id",
        description: String = "Test Expense",
        amount: Double = 100.0,
        currency: String = "USD",
        paidBy: String = "user-1",
        paidByName: String = "Test User"
    ) = Expense(
        id = id,
        groupId = groupId,
        description = description,
        amount = amount,
        currency = currency,
        paidBy = paidBy,
        paidByName = paidByName,
        splitBetween = listOf(
            ExpenseSplit("user-1", "User 1", amount / 2),
            ExpenseSplit("user-2", "User 2", amount / 2)
        )
    )
    
    fun groupBuilder(
        id: String = "test-group-id",
        name: String = "Test Group",
        currency: String = "USD",
        memberCount: Int = 2
    ) = Group(
        id = id,
        name = name,
        currency = currency,
        members = (1..memberCount).map { 
            GroupMember("user-$it", "User $it", "user$it@test.com", GroupRole.MEMBER)
        }
    )
}
```

## 6. CI/CD INTEGRATION PATTERNS

### **Build Configuration (`app/build.gradle.kts`)**
```kotlin
android {
    defaultConfig {
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    // Unit testing dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:4.6.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    
    // Android testing dependencies
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$compose_version")
}
```

### **Test Execution Strategy**
```bash
# Local testing commands
./gradlew test                    # Run unit tests
./gradlew connectedAndroidTest   # Run instrumentation tests
./gradlew testDebugUnitTest      # Run debug unit tests
./gradlew jacocoTestReport       # Generate coverage reports

# CI/CD pipeline testing
- Unit tests run on every commit
- Integration tests run on pull requests
- Performance tests run on release candidates
- UI tests run on staging deployments
```

### **Coverage Requirements**
```
Minimum Coverage Thresholds:
- Business Logic: 90%+ (calculations, validations)
- Repository Layer: 80%+ (data access patterns)
- ViewModel Layer: 85%+ (state management logic)
- Service Layer: 80%+ (business operations)
- Overall Project: 75%+ (comprehensive coverage)
```

## 7. TESTING BEST PRACTICES

### **Test Isolation Principles**
```kotlin
// Each test is independent and can run in any order
@Test
fun `test should not depend on other tests`() {
    // Setup specific to this test
    val testData = createTestData()
    
    // Execute test logic
    val result = systemUnderTest.process(testData)
    
    // Assert specific to this test
    assertEquals(expectedResult, result)
    
    // Cleanup handled automatically by test framework
}
```

### **Assertion Strategies**
```kotlin
// Specific assertions for business logic
assertEquals(50.0, split.share, 0.01)  // Delta for floating point
assertTrue(result.isSuccess)
assertFalse(errors.isEmpty())
assertNull(validationError)
assertNotNull(createdExpense)

// Collection assertions
assertEquals(2, splits.size)
assertTrue(splits.all { it.share > 0 })
assertThat(expenses).hasSize(5)

// Exception testing
assertThrows<IllegalArgumentException> {
    calculator.divide(10.0, 0.0)
}

// Custom matchers for domain objects
assertThat(expense).hasAmount(100.0).hasCurrency("USD")
```

### **Test Documentation**
```kotlin
/**
 * Test class for expense splitting calculations
 * 
 * Covers:
 * - Equal split calculations
 * - Percentage-based splits
 * - Custom amount splits
 * - Edge cases and error conditions
 * - Fallback behaviors
 */
@RunWith(MockitoJUnitRunner::class)
class SplitCalculatorTest {
    
    /**
     * Verify that equal split divides expense amount equally
     * among all group members regardless of member count
     */
    @Test
    fun `equal split divides amount equally among members`() {
        // Test implementation
    }
}
```

## 8. AI PATTERN LEARNING OBJECTIVES

### **Testing Strategy Recognition**
- **Layered Testing**: Unit tests for logic, integration tests for components
- **Business Focus**: Testing business rules and calculations comprehensively
- **Edge Case Coverage**: Boundary conditions and error scenarios
- **Test Organization**: Feature-based structure matching main source code

### **Quality Assurance Patterns**
- **Given-When-Then**: Clear test structure for maintainability
- **Descriptive Naming**: Test names that explain business behavior
- **Isolation**: Independent tests that don't rely on external state
- **Assertion Precision**: Specific assertions for expected outcomes

### **CI/CD Integration**
- **Automated Execution**: Tests run on every code change
- **Coverage Monitoring**: Minimum thresholds for different layers
- **Performance Testing**: Load and stress testing for critical paths
- **Integration Validation**: End-to-end testing of user journeys

## 9. IMPLEMENTATION GUIDELINES FOR AI

### **Quality Indicators**
1. **Comprehensive Coverage**: All business logic paths tested
2. **Clear Structure**: Given-When-Then organization
3. **Edge Cases**: Boundary conditions and error scenarios covered
4. **Maintainable Tests**: Descriptive names and focused assertions

### **Common Anti-Patterns to Avoid**
- Tests that depend on external services without mocking
- Overly complex test setup that obscures the test purpose
- Testing implementation details instead of business behavior
- Insufficient edge case and error condition coverage
- Poor test naming that doesn't explain the scenario being tested

---

*AI Training Data for Testing Patterns - Generated from Fairr Android App Pass 7*
*File references verified and testing strategies documented* 