/// Account Manager Plugin — cross-platform account management, authentication,
/// and background sync via native platform APIs.
// ignore_for_file: unnecessary_library_name
library flutter_account_manager;

export 'src/generated/account_manager_api.g.dart'
    show SyncStatus, PeriodicSyncConfig;
export 'src/models/account.dart';
export 'src/models/account_event.dart';
export 'src/models/sync_event.dart';
export 'src/models/sync_result.dart';
export 'src/exceptions.dart';
export 'src/account_manager_plugin.dart';
