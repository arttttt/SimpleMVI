# SimpleMVI

A lightweight, flexible, and powerful MVI library for Kotlin Multiplatform applications.

## Overview

SimpleMVI is an opinionated MVI (Model-View-Intent) implementation focused on domain logic organization. Built with Kotlin Multiplatform, it provides a robust foundation for building maintainable applications across multiple platforms.

### Key Features

- **Platform Agnostic**: Core functionality works seamlessly across Android, iOS, macOS, and Wasm JS
- **Unidirectional Data Flow**: Predictable state management with clear data flow
- **Type-safe**: Leverages Kotlin's type system for compile-time safety
- **Coroutines Support**: First-class support for Kotlin Coroutines

## Architecture

SimpleMVI follows these core principles:

- **Single Source of Truth**: State is managed in a centralized Store
- **Immutable State**: All state changes are performed through pure functions
- **Clear Boundaries**: Strict separation between UI and domain logic

### Core Components

- **Store**: Central component managing state and coordinating other components
- **Actor**: Business logic processor handling intents and producing state changes
- **Middleware**: Optional interceptors for logging, analytics, etc.
- **Intent**: Represents user actions or system events
- **State**: Immutable data representing application state
- **Side Effect**: One-time events that can't be represented in state

## Documentation

For detailed documentation, examples and best practices, please visit: [Documentation](https://arttttt.github.io/SimpleMVI/)

## Threading Model

- Background processing should be handled within Actors using coroutines

## Contributing

Contributions are welcome!

## License

```
MIT License

Copyright (c) 2025 Artem Bambalov

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```