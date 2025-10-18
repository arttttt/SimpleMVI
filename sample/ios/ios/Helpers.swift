import Shared

// MARK: - KotlinThrowable → Error bridge for KMP
public struct KotlinFlowError: Error, LocalizedError {
    public let kotlinError: KotlinThrowable
    
    public init(_ error: KotlinThrowable) {
        self.kotlinError = error
    }
    
    public var errorDescription: String? {
        kotlinError.message
    }
}

// MARK: Flow → AsyncThrowingStream
func asAsyncThrowingStream<T: AnyObject>(
    _ flow: CFlow<T>
) -> AsyncThrowingStream<T, Error> {
    AsyncThrowingStream { continuation in
        let subscription = flow.subscribe(
            onEach: { value in
                continuation.yield(value)
            },
            onCompletion: { error in
                if let error = error {
                    continuation.finish(throwing: KotlinFlowError(error))
                } else {
                    continuation.finish()
                }
            }
        )
        
        continuation.onTermination = { _ in
            subscription.unsubscribe()
        }
    }
}

// MARK: StateFlow → AsyncThrowingStream
func asAsyncThrowingStream<T: AnyObject>(
    _ stateFlow: CStateFlow<T>
) -> AsyncThrowingStream<T, Error> {
    AsyncThrowingStream { continuation in
        let subscription = stateFlow.subscribe(
            onEach: { value in
                continuation.yield(value)
            },
            onCompletion: { error in
                if let error = error {
                    continuation.finish(throwing: KotlinFlowError(error))
                } else {
                    continuation.finish()
                }
            }
        )
        
        continuation.onTermination = { _ in
            subscription.unsubscribe()
        }
    }
}

// MARK: SideEffect Equatable wrapper
class StoreSideEffectWrapper<T : Any> : Equatable {
    
    let wrapped: T
    
    init(wrapped: T) {
        self.wrapped = wrapped
    }
    
    static func == (lhs: StoreSideEffectWrapper<T>, rhs: StoreSideEffectWrapper<T>) -> Bool {
        guard
            let l = lhs.wrapped as AnyObject?,
            let r = rhs.wrapped as AnyObject?
        else {
            return false
        }
        
        return l === r
    }
}
