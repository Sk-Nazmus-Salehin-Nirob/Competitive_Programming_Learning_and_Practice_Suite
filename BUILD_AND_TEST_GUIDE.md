# Building and Running the Bookmarks Feature

## Prerequisites
- Android Studio installed
- Android SDK configured
- Emulator or physical Android device

## Build Instructions

### Option 1: Using Android Studio (Recommended)

1. **Open Project in Android Studio**
   - Open Android Studio
   - Click "Open" and navigate to the CPLPS project folder
   - Wait for Gradle sync to complete

2. **Sync Gradle**
   - Click "File" → "Sync Project with Gradle Files"
   - OR click the sync button in the toolbar
   - Wait for sync to complete

3. **Build the Project**
   - Click "Build" → "Make Project" (Ctrl+F9)
   - OR click "Build" → "Rebuild Project" for a clean build
   - Check the Build output for any errors

4. **Run the App**
   - Click the "Run" button (green play icon)
   - OR press Shift+F10
   - Select your emulator or connected device
   - Wait for the app to install and launch

### Option 2: Using Command Line (if gradlew is available)

If you have gradlew files, you can build from command line:

```bash
# Windows
gradlew.bat assembleDebug

# Linux/Mac
./gradlew assembleDebug
```

## Testing the Bookmarks Feature

### 1. Initial Setup
- Launch the app
- Login or create an account
- Go to Profile and add your Codeforces handle
- Let the app sync your solved problems

### 2. Testing Add Bookmark
1. Click on the "Bookmarks" icon in bottom navigation
2. Click the FAB (+) button
3. Paste a Codeforces problem URL, for example:
   - `https://codeforces.com/contest/1234/problem/A`
   - `https://codeforces.com/problemset/problem/1/A`
4. Select category ("Problems to solve" or "Interesting Problems")
5. Click "Add"
6. Verify the bookmark appears with problem details

### 3. Testing Problem Opening
1. Click on any bookmark card
2. Verify it opens the Codeforces problem in browser

### 4. Testing Delete
1. Click the delete (trash) icon on a bookmark
2. Verify the bookmark is removed from the list

### 5. Testing Auto-Detection
1. Add a problem you haven't solved yet to "Problems to solve"
2. Go to Codeforces and solve that problem
3. Return to app → Go to Profile
4. Sync your profile (this fetches latest solved problems)
5. Go back to Bookmarks → "Problems to solve" tab
6. Refresh (pull down) or navigate away and back
7. Verify the solved problem is automatically removed
8. Check for toast notification

### 6. Testing Categories
1. Add bookmarks to both categories
2. Switch between tabs
3. Verify bookmarks appear only in their respective categories

## Common Issues and Solutions

### Build Errors

**Issue**: "Cannot resolve symbol" errors
- **Solution**: File → Invalidate Caches / Restart → Invalidate and Restart

**Issue**: Gradle sync failed
- **Solution**: 
  - Check internet connection
  - File → Sync Project with Gradle Files
  - Update Gradle version if needed

**Issue**: Missing dependencies
- **Solution**: Check `build.gradle` files and ensure all dependencies are correct

### Runtime Errors

**Issue**: App crashes on opening Bookmarks
- **Solution**: 
  - Check if you're logged in
  - Check logcat for error messages
  - Verify database migration completed successfully

**Issue**: Problem not auto-detected as solved
- **Solution**:
  - Ensure you've added your Codeforces handle in Profile
  - Ensure you've synced your profile
  - Check if the problem code matches exactly
  - Pull to refresh in the Bookmarks screen

**Issue**: "Invalid Codeforces URL format" error
- **Solution**: Ensure URL is in one of these formats:
  - `https://codeforces.com/contest/[contestId]/problem/[index]`
  - `https://codeforces.com/problemset/problem/[contestId]/[index]`

## Debugging Tips

### View Database Contents
You can use Android Studio's App Inspection tool:
1. Run the app in debug mode
2. View → Tool Windows → App Inspection
3. Select "Database Inspector"
4. Browse the `bookmarked_problems` table

### Check Logs
1. View → Tool Windows → Logcat
2. Filter by "BookmarksActivity" or "BookmarkListFragment"
3. Look for error messages or API responses

### Network Inspection
1. Ensure device/emulator has internet connection
2. Check if Codeforces API is accessible
3. Look for retrofit/network errors in logcat

## Known Limitations

1. **API Limitations**: 
   - Codeforces API may be slow or unavailable at times
   - If API fails, bookmark is still added with default values

2. **Problem Detection**:
   - Auto-detection only works after profile sync
   - User must have Codeforces handle added
   - Only works for problems in "Problems to solve" category

3. **URL Parsing**:
   - Only supports Codeforces URLs
   - Must be in specific format (contest or problemset URLs)

## Next Steps After Building

1. Test all features thoroughly
2. Add more Codeforces problems to test with
3. Customize the UI colors/styling if needed
4. Consider adding additional features:
   - Problem notes/hints
   - Problem tags
   - Sorting and filtering
   - Statistics

## Support

If you encounter issues:
1. Check the logcat output
2. Verify all files were created correctly
3. Ensure database version updated to 4
4. Check that all imports are resolving correctly
5. Review the BOOKMARKS_FEATURE.md for implementation details

---

**Happy Coding! 🚀**
