import 'package:flutter/material.dart';
import 'package:account_manager/account_manager.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await AccountManagerPlugin.instance.initialize().catchError((_) {});
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Account Manager Example',
      home: Scaffold(
        appBar: AppBar(title: const Text('Account Manager Plugin')),
        body: const _AccountManagerDemo(),
      ),
    );
  }
}

class _AccountManagerDemo extends StatefulWidget {
  const _AccountManagerDemo();

  @override
  State<_AccountManagerDemo> createState() => _AccountManagerDemoState();
}

class _AccountManagerDemoState extends State<_AccountManagerDemo> {
  final _plugin = AccountManagerPlugin.instance;
  String _status = 'Ready';
  List<Account> _accounts = [];

  Future<void> _addAccount() async {
    try {
      final account = Account(
        username: 'demo@example.com',
        accountType: 'com.lkrjangid.demo',
        displayName: 'Demo User',
      );
      final success = await _plugin.addAccount(account, 'secret123');
      setState(() => _status = success ? 'Account added' : 'Add failed');
      await _loadAccounts();
    } catch (e) {
      setState(() => _status = 'Error: $e');
    }
  }

  Future<void> _loadAccounts() async {
    try {
      final list = await _plugin.getAccounts('com.lkrjangid.demo');
      setState(() {
        _accounts = list;
        _status = 'Loaded ${list.length} account(s)';
      });
    } catch (e) {
      setState(() => _status = 'Error: $e');
    }
  }

  Future<void> _syncNow() async {
    if (_accounts.isEmpty) {
      setState(() => _status = 'No accounts to sync');
      return;
    }
    try {
      final result = await _plugin.syncNow(_accounts.first);
      setState(() => _status = result.success
          ? 'Sync complete: ${result.stats?.itemsDownloaded} items'
          : 'Sync failed: ${result.errorMessage}');
    } catch (e) {
      setState(() => _status = 'Error: $e');
    }
  }

  @override
  void initState() {
    super.initState();
    _loadAccounts();
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text('Status: $_status',
              style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 16),
          ElevatedButton(onPressed: _addAccount, child: const Text('Add Account')),
          ElevatedButton(onPressed: _loadAccounts, child: const Text('Refresh Accounts')),
          ElevatedButton(onPressed: _syncNow, child: const Text('Sync Now')),
          const SizedBox(height: 16),
          Expanded(
            child: ListView.builder(
              itemCount: _accounts.length,
              itemBuilder: (ctx, i) {
                final acc = _accounts[i];
                return ListTile(
                  title: Text(acc.displayName ?? acc.username),
                  subtitle: Text(acc.accountType),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
