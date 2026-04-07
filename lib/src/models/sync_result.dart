import '../../src/generated/account_manager_api.g.dart';

/// Statistics from a completed sync operation.
class SyncStats {
  const SyncStats({
    required this.itemsUploaded,
    required this.itemsDownloaded,
    required this.conflicts,
    required this.syncTimeMs,
  });

  final int itemsUploaded;
  final int itemsDownloaded;
  final int conflicts;
  final int syncTimeMs;

  factory SyncStats.fromData(SyncStatsData data) => SyncStats(
        itemsUploaded: data.itemsUploaded,
        itemsDownloaded: data.itemsDownloaded,
        conflicts: data.conflicts,
        syncTimeMs: data.syncTimeMs,
      );

  @override
  String toString() =>
      'SyncStats(uploaded: $itemsUploaded, downloaded: $itemsDownloaded, '
      'conflicts: $conflicts, timeMs: $syncTimeMs)';
}

/// Result of a sync operation.
class SyncResult {
  const SyncResult({
    required this.success,
    this.errorCode,
    this.errorMessage,
    this.stats,
  });

  final bool success;
  final int? errorCode;
  final String? errorMessage;
  final SyncStats? stats;

  factory SyncResult.fromData(SyncResultData data) => SyncResult(
        success: data.success,
        errorCode: data.errorCode,
        errorMessage: data.errorMessage,
        stats: data.stats != null ? SyncStats.fromData(data.stats!) : null,
      );

  @override
  String toString() =>
      'SyncResult(success: $success, errorMessage: $errorMessage, stats: $stats)';
}
