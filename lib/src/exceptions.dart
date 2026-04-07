/// Base exception for all account manager plugin errors.
sealed class AccountManagerException implements Exception {
  const AccountManagerException({
    required this.message,
    this.errorCode,
    this.details,
  });

  final String message;
  final int? errorCode;
  final String? details;

  @override
  String toString() => '$runtimeType(message: $message, errorCode: $errorCode)';
}

/// Account with the same username+type already exists. Code range: 1000–1099.
class AccountAlreadyExistsException extends AccountManagerException {
  const AccountAlreadyExistsException({
    required super.message,
    super.errorCode = 1001,
  });
}

/// No account found matching the given criteria. Code range: 1000–1099.
class AccountNotFoundException extends AccountManagerException {
  const AccountNotFoundException({
    required super.message,
    super.errorCode = 1002,
  });
}

/// Re-authentication required (user interaction needed). Code range: 1100–1199.
class AuthenticationRequiredException extends AccountManagerException {
  const AuthenticationRequiredException({
    required super.message,
    super.errorCode = 1100,
  });
}

/// Network failure during sync. Code range: 1200–1299.
class SyncNetworkException extends AccountManagerException {
  const SyncNetworkException({
    required super.message,
    super.errorCode = 1200,
  });
}

/// Sync conflict that could not be auto-resolved. Code range: 1200–1299.
class SyncConflictException extends AccountManagerException {
  const SyncConflictException({
    required super.message,
    required this.conflictId,
    required this.localData,
    required this.remoteData,
    super.errorCode = 1201,
  });

  final String conflictId;
  final String localData;
  final String remoteData;
}

/// Plugin is not properly configured (missing manifest/plist entries). Code range: 1500–1599.
class PluginNotConfiguredException extends AccountManagerException {
  const PluginNotConfiguredException({
    required super.message,
    super.errorCode = 1500,
  });
}

/// Credential storage or retrieval failure. Code range: 1300–1399.
class CredentialException extends AccountManagerException {
  const CredentialException({
    required super.message,
    super.errorCode = 1300,
  });
}

/// Requested operation is not supported on this platform. Code range: 1500–1599.
class UnsupportedOperationException extends AccountManagerException {
  const UnsupportedOperationException({
    required super.message,
    super.errorCode = 1501,
  });
}
