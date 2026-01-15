# Project Configuration

## Development Environment

### WearOS SDK CLI Tools

This project has the WearOS SDK CLI tools installed, enabling development and deployment of Wear OS applications from the command line.

### Version Control

This project uses Git for version control. Commit changes regularly with descriptive messages.

## Testing

### Running Unit Tests

Run all unit tests:
```bash
./gradlew test
```

Run debug unit tests only:
```bash
./gradlew testDebugUnitTest
```

### Test Coverage

Unit tests cover pure business logic:
- `TimeUtilsTest` - Time formatting functions
- `DailyStatsTest` - Stats computation and defaults
- `TimerStateTest` - Timer progress calculations

Test files are in `app/src/test/java/com/pomowear/`.
