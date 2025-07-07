# AI TRAINING DATA: UI Conventions & Jetpack Compose Patterns - Fairr Android App

## 1. FILE-TO-UI-COMPONENT MAPPING

### **Design System Foundation**
```
ui/theme/Theme.kt → Material 3 theme configuration with dark/light mode support
ui/theme/Color.kt → Comprehensive color system with semantic naming
ui/theme/Type.kt → Complete Material 3 typography system
→ Pattern: Centralized design system architecture
```

### **Component Library Architecture (`ui/components/`)**

#### **Modern Component System**
```
components/ModernComponents.kt → Advanced Material 3 components with animations
→ ModernCard: Elevated card with shadow and click handling
→ ModernItemCard: Complex content cards with icons, badges, progress
→ ModernProgressBar: Styled progress indicators
→ Pattern: Composition-first component design with prop-based customization
```

#### **Common Reusable Components**
```
components/CommonComponents.kt → Shared UI elements across app
→ FairrFilterChip: Custom filter selection components
→ FairrActionChip: Action-based chip components  
→ FairrConfirmationDialog: Standardized confirmation dialogs
→ FairrLoadingDialog: Loading state management
→ Pattern: Prefixed component naming for brand consistency
```

#### **Specialized Component Categories**
```
components/Calculator.kt → Financial calculation UI components
components/Cards.kt → Card-based layout components
components/CurrencyComponents.kt → Currency selection and display
components/EmojiComponents.kt → Emoji picker and display system
components/FriendGroupComponents.kt → Social feature UI components
components/ImageComponents.kt → Image loading and display with states
components/LoadingSpinner.kt → Loading state variations
components/ModernNavigationBar.kt → Custom bottom navigation with animations
components/ModernUXComponents.kt → Advanced UX interaction components
components/ToolbarComponents.kt → Toolbar and app bar variations
→ Pattern: Domain-specific component organization
```

### **Navigation System**
```
components/ModernNavigationBar.kt → Custom animated bottom navigation
→ NavigationItem data class: Icon and state management
→ ModernNavigationBarItem: Individual tab with scale animations
→ Pattern: Animation-rich navigation with state transitions
```

## 2. JETPACK COMPOSE PATTERN TRAINING DATA

### **Material 3 Implementation Patterns**

#### **Theme System (`ui/theme/Theme.kt`)**
```kotlin
// Pattern: Dynamic color scheme with system theme detection
val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
}

// Pattern: Status bar color coordination with theme
SideEffect {
    val window = (view.context as Activity).window
    window.statusBarColor = colorScheme.background.toArgb()
    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
}
```
**AI Learning Points:**
- Dynamic Material You color integration
- System UI coordination with app theme
- Conditional theme selection patterns

#### **Color System Architecture (`ui/theme/Color.kt`)**
```kotlin
// Pattern: Semantic color organization with WCAG compliance
val TextPrimary = PureBlack  // High contrast text
val TextSecondary = CharcoalGray  // Medium contrast text
val TextTertiary = MediumGray  // Low contrast text

// Pattern: Status-based color categories
object FairrColors {
    val ButtonPrimary = PureBlack
    val ButtonPrimaryText = PureWhite
    val ButtonSecondary = LightGray
    val ButtonSecondaryText = PureBlack
}

// Pattern: Component-specific color semantics
object ComponentColors {
    val Success = SuccessGreen
    val Error = ErrorRed
    val Warning = WarningOrange
    val Info = InfoBlue
}
```
**AI Learning Points:**
- Semantic color naming conventions
- WCAG accessibility compliance patterns
- Hierarchical color organization strategies

#### **Typography System (`ui/theme/Type.kt`)**
```kotlin
// Pattern: Complete Material 3 typography scale implementation
val Typography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 57.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
)

// Pattern: App-specific typography extensions
object FairrTypography {
    val AmountLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp)
    val CardTitle = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
}
```
**AI Learning Points:**
- Material 3 typography scale implementation
- Custom typography extension patterns
- Context-specific text style organization

### **Component Composition Patterns**

#### **Modern Card Pattern (`components/ModernComponents.kt`)**
```kotlin
@Composable
fun ModernCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = CardBackground,
    shadowElevation: Int = 2,
    cornerRadius: Int = 16,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = shadowElevation.dp, shape = RoundedCornerShape(cornerRadius.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(cornerRadius.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}
```
**AI Learning Points:**
- Optional parameter patterns with defaults
- Conditional modifier application
- Composition-based content slots
- Custom shadow and elevation handling

#### **Complex Component Pattern (`components/ModernComponents.kt`)**
```kotlin
@Composable
fun ModernItemCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconBackgroundColor: Color = LightGray,
    badge: String? = null,
    progress: Float? = null,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Multi-element composition with conditional rendering
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Icon container with styled background
        Box(modifier = Modifier.background(iconBackgroundColor, RoundedCornerShape(12.dp))) {
            Icon(imageVector = icon, modifier = Modifier.size(24.dp))
        }
        
        // Content with conditional elements
        Column {
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = title, modifier = Modifier.weight(1f))
                badge?.let { BadgeComponent(it) }
            }
            subtitle?.let { Text(text = it) }
            progress?.let { ProgressIndicator(it) }
        }
    }
}
```
**AI Learning Points:**
- Complex multi-element component composition
- Conditional rendering with nullable parameters
- Flexible layout with spacing arrangements
- State-based UI element display

#### **Navigation Animation Pattern (`components/ModernNavigationBar.kt`)**
```kotlin
@Composable
private fun ModernNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    item: NavigationItem,
    modifier: Modifier = Modifier
) {
    val animatedElevation by animateDpAsState(
        targetValue = if (selected) 8.dp else 0.dp,
        animationSpec = tween(durationMillis = 200)
    )
    
    val animatedScale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1.0f,
        animationSpec = tween(durationMillis = 200)
    )
    
    AnimatedVisibility(
        visible = selected,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        // Selected state UI
    }
}
```
**AI Learning Points:**
- State-based animation patterns
- Multiple simultaneous animations
- AnimatedVisibility for show/hide transitions
- Smooth state transition management

### **Dialog and Modal Patterns**

#### **Confirmation Dialog Pattern (`components/CommonComponents.kt`)**
```kotlin
@Composable
fun FairrConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.SemiBold) },
        text = { Text(text = message, color = TextSecondary) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) ErrorRed else DarkGreen
                )
            ) { Text(confirmText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissText) }
        }
    )
}
```
**AI Learning Points:**
- Reusable dialog patterns with semantic parameters
- Conditional styling based on action type
- Consistent button placement and styling
- Accessibility-friendly dialog structure

## 3. UI ARCHITECTURE TRAINING DATA

### **Screen-Component Relationship Patterns**
```
screens/[feature]/[Feature]Screen.kt → Feature-specific UI implementation
↓ (uses)
components/[Category]Components.kt → Reusable component library
↓ (applies)
theme/[Theme|Color|Type].kt → Design system foundation
```

### **State Management in UI**
```
screens/[feature]/[Feature]ViewModel.kt → UI state management
↓ (provides StateFlow)
screens/[feature]/[Feature]Screen.kt → UI consumption
↓ (collects state)
@Composable Screen(viewModel: ViewModel = hiltViewModel())
```

### **Component Hierarchy Patterns**
```
Scaffold → TopAppBar + Content + BottomNavigation
├── TopAppBar (ToolbarComponents.kt)
├── Content Area
│   ├── LazyColumn/LazyRow (data lists)
│   ├── ModernCard (content containers)
│   └── ModernComponents (interactive elements)
└── ModernNavigationBar (bottom navigation)
```

## 4. DESIGN SYSTEM TRAINING INSIGHTS

### **Material 3 Compliance Patterns**
**Implementation Files:**
- `ui/theme/Theme.kt` → Material 3 color scheme integration
- `ui/theme/Color.kt` → Material color roles and semantic colors
- `ui/theme/Type.kt` → Material 3 typography scale

**Design Principles:**
- Dynamic color support for Android 12+
- WCAG 2.1 AA accessibility compliance
- Consistent elevation and shadow usage
- Semantic color naming for maintainability

### **Component Naming Conventions**
```
Pattern: [Brand][ComponentType][Variant?]
Examples:
- FairrFilterChip (branded filter component)
- ModernCard (modern variant of card)
- ModernNavigationBar (modern navigation component)
```

### **Animation and Interaction Patterns**
**Animation Files:**
- `components/ModernNavigationBar.kt` → Navigation state transitions
- `components/ModernComponents.kt` → Card hover and interaction states

**Animation Characteristics:**
- 200ms duration for state transitions
- Scale and fade combinations for visibility changes
- Elevation changes for selection states
- Smooth property animations with tween specifications

### **Responsive Design Patterns**
```
Modifier.fillMaxWidth() → Full width responsive containers
Arrangement.spacedBy(16.dp) → Consistent spacing patterns  
Modifier.weight(1f) → Flexible space distribution
Modifier.padding(20.dp) → Standard content padding
```

## 5. AI PATTERN LEARNING OBJECTIVES

### **Jetpack Compose Mastery**
- **Component Composition**: How to build complex components from simple primitives
- **State Management**: Integration of ViewModels with Compose UI state
- **Animation Patterns**: Smooth transitions and state-based animations
- **Material 3 Implementation**: Proper theme system integration

### **Design System Recognition**
- **Semantic Naming**: How colors, typography, and components are systematically named
- **Accessibility Patterns**: WCAG compliance through design system choices
- **Brand Consistency**: How prefix naming maintains component consistency
- **Responsive Design**: Flexible layout patterns for different screen sizes

### **Architecture Alignment**
- **Clean Separation**: UI components separated from business logic
- **Reusability**: Component library patterns for shared UI elements
- **Testability**: Component structure that enables easy testing
- **Maintainability**: Organized file structure for long-term maintenance

## 6. IMPLEMENTATION GUIDELINES FOR AI

### **Component Recognition Patterns**
1. **Identify component type** by file name and structure
2. **Analyze composition patterns** in `@Composable` functions
3. **Map design system usage** through theme integration
4. **Understand state integration** through ViewModel patterns

### **Quality Assessment Criteria**
- **Material 3 Compliance**: Proper use of Material Design components
- **Accessibility**: WCAG-compliant color contrasts and component structure  
- **Performance**: Efficient composition and recomposition patterns
- **Maintainability**: Clear naming and organizational structure

### **Common Anti-Patterns to Avoid**
- Hard-coded colors instead of theme references
- Inconsistent spacing and typography
- Missing accessibility considerations
- Poor component naming conventions

---

*AI Training Data for UI Conventions - Generated from Fairr Android App Pass 2*
*File references verified and pattern analysis complete* 