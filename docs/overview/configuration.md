# SimpleMVI Configuration

SimpleMVI provides a flexible configuration system that allows you to customize library behavior including error handling and logging.

## Configuration Options

### Error Handling

In SimpleMVI, all main thread checks are mandatory and cannot be disabled, as this is a fundamental architectural constraint.

However, you can configure how the library behaves when it encounters errors such as:

- Using an uninitialized Store
- Using a destroyed Store

```kotlin
// Configuration for production environment
configureSimpleMVI {
    // Lenient mode: only log errors without throwing exceptions
    strictMode = false
}

// Configuration for development environment
configureSimpleMVI {
    // Strict mode: throw exceptions when errors occur (default)
    strictMode = true
}
```

When `strictMode = true`, the library operates in strict mode and throws exceptions when errors are detected.
When `strictMode = false` (default), the library operates in lenient mode and only logs errors without throwing exceptions.

### Logging

You can configure the logger used by the library:

```kotlin
// Setting a custom logger
configureSimpleMVI {
    logger = MyCustomLogger()
}

// Disabling logging completely
configureSimpleMVI {
    logger = null
}
```

By default, `DefaultLogger` is used. If you set `logger` to `null`, logging will be disabled.

#### Legacy Logging Configuration

For backward compatibility, the library still supports the legacy `setDefaultLogger()` method, but it's deprecated and will be removed in future versions:

```kotlin
// Legacy way to set logger (deprecated)
setDefaultLogger(MyCustomLogger())

// Recommended way
configureSimpleMVI {
    logger = MyCustomLogger()
}
```

> **Note:** When using `setDefaultLogger()`, other configuration values like `strictMode` are preserved.

## Complete Configuration Example

```kotlin
// Complete configuration
configureSimpleMVI {
    // Use lenient mode for production
    strictMode = false
    
    // Set custom logger
    logger = object : Logger {
        override fun log(message: String) {
            // Forward to your logging system
            MyLoggingSystem.log(message)
        }
    }
}
```

## Usage in Different Environments

A common approach is to use different configurations for different environments:

```kotlin
// In Application.onCreate() or another initialization point
if (BuildConfig.DEBUG) {
    // Configuration for development
    configureSimpleMVI {
        strictMode = true
        logger = DebugLogger()
    }
} else {
    // Configuration for production
    configureSimpleMVI {
        strictMode = false
        logger = ReleaseLogger()
    }
}
```

## Notes

1. Configuration is set globally for all Store instances.
2. Main thread checks are always mandatory and cannot be disabled.
3. In lenient mode (strictMode = false), errors will be logged if a logger is configured.
4. The name `strictMode` follows common practice in Android and other platforms, where this term describes a mode with stricter checks.
5. If you need to create your own logger, implement the `Logger` interface with a single `log(message: String)` method.
6. All configuration settings are applied immediately and affect all future operations.