import 'package:dynamic_system_colors/dynamic_system_colors.dart';
import 'package:flutter/material.dart';
import 'package:animations/animations.dart';
import 'package:provider/provider.dart';
import 'package:school_app/data/repo/beste_schule_repo_impl.dart';
import 'package:school_app/domain/repo/beste_schule_repo.dart';
import 'package:school_app/presentation/pages/login_page/auth_checker.dart';
import 'package:school_app/domain/settings/settings_provider.dart';
import 'package:school_app/utils/global_dialog.dart';
import 'package:school_app/utils/logger.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // load cache before app starts
  final repo = BesteSchuleRepoImpl.instance;
  await repo.loadCacheOnStartup();

  runApp(
    ChangeNotifierProvider(
      create: (_) => SettingsProvider.instance,
      child: SchoolApp(repo: repo),
    ),
  );
}

class SchoolApp extends StatelessWidget {
  final BesteSchuleRepo repo;
  const SchoolApp({super.key, required this.repo});

  final MaterialColor defaultFallbackColor = Colors.green;

  ThemeData getLightTheme(
    ColorScheme? lightDynamic,
    SettingsProvider settingsProvider,
  ) {
    return ThemeData(
      colorScheme: settingsProvider.themeColor == null
          ? lightDynamic ??
                ColorScheme.fromSeed(
                  seedColor: defaultFallbackColor.shade500,
                  brightness: Brightness.light,
                )
          : ColorScheme.fromSeed(
              seedColor: settingsProvider.themeColor!,
              brightness: Brightness.light,
            ),
      useMaterial3: true,
      pageTransitionsTheme: const PageTransitionsTheme(
        builders: {
          TargetPlatform.android: SharedAxisPageTransitionsBuilder(
            transitionType: SharedAxisTransitionType.horizontal,
          ),
          TargetPlatform.iOS: SharedAxisPageTransitionsBuilder(
            transitionType: SharedAxisTransitionType.horizontal,
          ),
        },
      ),
    );
  }

  ThemeData getDarkTheme(
    ColorScheme? darkDynamic,
    SettingsProvider settingsProvider,
  ) {
    return ThemeData(
      colorScheme: settingsProvider.themeColor == null
          ? darkDynamic ??
                ColorScheme.fromSeed(
                  seedColor: defaultFallbackColor.shade500,
                  brightness: Brightness.dark,
                )
          : ColorScheme.fromSeed(
              seedColor: settingsProvider.themeColor!,
              brightness: Brightness.dark,
            ),
      useMaterial3: true,
      pageTransitionsTheme: const PageTransitionsTheme(
        builders: {
          TargetPlatform.android: SharedAxisPageTransitionsBuilder(
            transitionType: SharedAxisTransitionType.horizontal,
          ),
          TargetPlatform.iOS: SharedAxisPageTransitionsBuilder(
            transitionType: SharedAxisTransitionType.horizontal,
          ),
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    logger.d("[UI] Called main build method.");

    // dynamically style page by system color such as material you
    return DynamicColorBuilder(
      builder: (ColorScheme? lightDynamic, ColorScheme? darkDynamic) {
        final settingsProvider = Provider.of<SettingsProvider>(context);
        return Provider<BesteSchuleRepo>(
          create: (_) => repo,
          child: MaterialApp(
            navigatorKey: navigatorKey,
            debugShowCheckedModeBanner: false,
            theme: getLightTheme(lightDynamic, settingsProvider),
            darkTheme: getDarkTheme(darkDynamic, settingsProvider),
            themeMode: settingsProvider.themeMode,
            // auth check page to check wether the token is in storage
            home: const AuthChecker(),
          ),
        );
      },
    );
  }
}
