# Fairr - UI/UX Improvement Suggestions (Cascade)

## Immediate Fixes
- [x] change the blue header on the welcome screen; use a monotone background and the Fairr brand icon.
- [x] Replace the generic "F" logo on the welcome screen with the Fairr brand icon in a squircle light gray background.
- [x] Improve button styling on the welcome screen for a more modern look.
- [x] Reduce vertical whitespace and better balance the welcome page layout.
- [x] Unify the splash screen background color with the rest of the app (use monotone light or dark, as per theme).
- [x] Fix navigation bar issues:
    - Remove extra white space/padding around navigation bar
    - Implement animated or visually distinct indicator for selected tab (colored bump or circle)
    - Vertically center navigation bar icons
    - Use consistent icon set (outlined for unselected, filled for selected)
    - Remove text labels and center icons
    - Add proper spacing between icons
- [x] Remove floating action button (FAB) completely
- [x] Restore monotone color scheme:
    - Replace all blue accents (buttons, FAB, nav selection) with the chosen monotone accent color
    - Use consistent color palette across all screens
    - Ensure proper contrast ratios
- [x] Add subtle shadows or dividers to separate cards and sections on the home screen.
- [x] Compact empty space on non-home tabs for a more balanced look.

## Welcome Screen Improvements
- [x] Redesign welcome screen:
    - Replace solid blue background with monotone gradient or subtle pattern
    - Enhance logo container with modern "squircle" shape and subtle shadow
    - Add subtle animations for logo and text elements
    - Improve button styling with proper spacing and modern design
    - Make layout fully responsive with proper edge-to-edge design
- [x] Improve typography:
    - Use consistent font weights
    - Better spacing between text elements
    - Ensure proper text hierarchy

## Home Screen Refinements
- [x] Update card designs:
    - Use consistent corner radius across all cards
    - Add subtle shadows for depth
    - Improve spacing between elements
    - Better visual hierarchy for numbers and text
- [x] Overview section:
    - Enhance stats cards with better iconography
    - Add subtle animations for number changes
    - Improve balance between text and numbers
- [x] Recent Groups:
    - Update list item design with modern styling
    - Better handling of group avatars/icons
    - Clearer display of amounts and status

## Theme
- [ ] Splash screen:
    - Center logo properly
    - make the background light gray or something that will make the logo more visible 
    - Add smooth fade-in animation
    - Use consistent background color
    - Implement proper icon masking

## Navigation & Interaction
- [x] Bottom navigation:
    - Implement smooth tab switching animations
    - Add haptic feedback for tab changes
    - Ensure proper touch targets
    - Use consistent icon weights
- [ ] Screen transitions:
    - Add smooth transitions between screens
    - Implement proper gesture navigation
    - Handle edge cases for navigation

## Accessibility & Polish
- [ ] Improve accessibility:
    - Ensure proper content scaling
    - Add content descriptions for all interactive elements
    - Test with screen readers
    - Verify color contrast ratios
- [ ] General polish:
    - Consistent spacing throughout app
    - Proper error states and feedback
    - Loading states and animations
    - Handle edge cases (long text, different screen sizes)

## Testing & Verification
- [ ] Verify animations:
    - Smooth performance
    - No janky transitions
    - Proper timing

---

**After implementing any of these changes, always build and run the app on your device to verify improvements and catch regressions.**

**Remaining Priority Order:**
1. Splash screen improvements
2. Screen transitions
3. Accessibility improvements
4. General polish and testing

**Completed:**
1. ✅ Navigation bar fixes and FAB removal
2. ✅ Color scheme restoration
3. ✅ Welcome screen improvements
4. ✅ Home screen refinements
