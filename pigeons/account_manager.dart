// ignore_for_file: one_member_abstracts

import 'package:pigeon/pigeon.dart';

@ConfigurePigeon(PigeonOptions(
  dartOut: 'lib/src/generated/account_manager_api.g.dart',
  dartOptions: DartOptions(),
  kotlinOut:
      'android/src/main/kotlin/com/lkrjangid/account_manager/AccountManagerApi.g.kt',
  kotlinOptions: KotlinOptions(
    package: 'com.lkrjangid.account_manager',
  ),
  swiftOut: 'ios/Classes/AccountManagerApi.g.swift',
  swiftOptions: SwiftOptions(),
))

// ============================================================================
// DATA CLASSES
// ============================================================================

/// Represents a user account with associated metadata
class AccountData {
  AccountData({
    required this.username,
    required this.accountType,
    this.displayName,
    this.userData,
  });

  final String username;
  final String accountType;
  final String? displayName;
  final Map<String?, String?>? userData;
}

/// Statistics from a completed sync operation
class SyncStatsData {
  SyncStatsData({
    required this.itemsUploaded,
    required this.itemsDownloaded,
    required this.conflicts,
    required this.syncTimeMs,
  });

  final int itemsUploaded;
  final int itemsDownloaded;
  final int conflicts;
  final int syncTimeMs;
}

/// Result of a sync operation with statistics
class SyncResultData {
  SyncResultData({
    required this.success,
    this.errorCode,
    this.errorMessage,
    this.stats,
  });

  final bool success;
  final int? errorCode;
  final String? errorMessage;
  final SyncStatsData? stats;
}

/// Configuration for periodic sync scheduling
class PeriodicSyncConfig {
  PeriodicSyncConfig({
    required this.intervalSeconds,
    this.flexSeconds,
    this.requiresNetwork,
    this.requiresCharging,
    this.extras,
  });

  final int intervalSeconds;
  final int? flexSeconds;
  final bool? requiresNetwork;
  final bool? requiresCharging;
  final Map<String?, String?>? extras;
}

/// Current sync status
enum SyncStatus {
  idle,
  pending,
  active,
  failed,
}

/// Auth token request result
class AuthTokenResult {
  AuthTokenResult({
    this.token,
    this.errorCode,
    this.errorMessage,
    this.requiresUserInteraction,
  });

  final String? token;
  final int? errorCode;
  final String? errorMessage;
  final bool? requiresUserInteraction;
}

/// Sync progress update
class SyncProgressData {
  SyncProgressData({
    required this.phase,
    required this.progress,
    this.message,
  });

  final String phase;
  final double progress;
  final String? message;
}

// ============================================================================
// FLUTTER -> NATIVE API
// ============================================================================

@HostApi()
abstract class AccountManagerHostApi {
  // Account Operations
  @async
  bool addAccount(AccountData account, String password);

  @async
  List<AccountData> getAccounts(String accountType);

  @async
  AccountData? getAccount(String username, String accountType);

  @async
  bool updateAccount(AccountData account);

  @async
  bool removeAccount(AccountData account);

  @async
  bool accountExists(String username, String accountType);

  // Credential Operations
  @async
  bool updateCredentials(AccountData account, String newPassword);

  @async
  bool validateCredentials(
      String username, String password, String accountType);

  @async
  bool clearCredentials(AccountData account);

  // Auth Token Operations
  @async
  AuthTokenResult getAuthToken(AccountData account, String tokenType);

  @async
  bool setAuthToken(AccountData account, String tokenType, String token);

  @async
  bool invalidateAuthToken(String accountType, String token);

  @async
  bool invalidateAllTokens(AccountData account, String tokenType);

  @async
  List<String> getAvailableTokenTypes(AccountData account);

  // Sync Operations
  @async
  SyncResultData syncNow(AccountData account, bool expedited);

  @async
  bool setSyncAutomatically(AccountData account, bool enabled);

  @async
  bool isSyncAutomatically(AccountData account);

  @async
  bool addPeriodicSync(AccountData account, PeriodicSyncConfig config);

  @async
  bool removePeriodicSync(AccountData account);

  @async
  SyncStatus getSyncStatus(AccountData account);

  @async
  bool cancelSync(AccountData account);

  // Platform-Specific
  @async
  bool openAccountSettings();

  @async
  bool isConfigured();

  @async
  Map<String, bool> getPlatformCapabilities();
}

// ============================================================================
// NATIVE -> FLUTTER API (Callbacks)
// ============================================================================

@FlutterApi()
abstract class SyncCallbackFlutterApi {
  void onSyncStarted(AccountData account);
  void onSyncProgress(AccountData account, SyncProgressData progress);
  void onSyncCompleted(AccountData account, SyncResultData result);
  void onSyncCancelled(AccountData account);
  void onSyncConflict(AccountData account, String conflictId,
      String localData, String remoteData);
}

@FlutterApi()
abstract class AccountCallbackFlutterApi {
  void onAccountAdded(AccountData account);
  void onAccountRemoved(AccountData account);
  void onAccountUpdated(AccountData account);
  void onAuthTokenExpired(AccountData account, String tokenType);
}
