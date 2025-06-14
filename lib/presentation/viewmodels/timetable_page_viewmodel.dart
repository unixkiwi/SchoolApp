import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:school_app/domain/models/day.dart';
import 'package:school_app/domain/repo/beste_schule_repo.dart';
import 'package:school_app/utils/logger.dart';
import 'package:school_app/utils/time_utils.dart';

class TimetablePageViewmodel extends ChangeNotifier {
  final BesteSchuleRepo repo;

  TimetablePageViewmodel({required this.repo});

  List<SchoolDay?> _schoolDays = [];
  bool _isLoading = false;
  bool _dataFetched = false;
  int? _currentWeekNumber;

  List<SchoolDay?> get schoolDays => _schoolDays;
  SchoolDay? get today =>
      _schoolDays.length >= DateTime.now().weekday
          ? _schoolDays[DateTime.now().weekday - 1]
          : null;

  bool get isLoading => _isLoading;
  bool get dataFetched => _dataFetched;

  int? get weekNr => _currentWeekNumber;

  Future<void> fetchData({int? weekNr, bool force = false}) async {
    if (_isLoading) return;

    // await the lessons of the current week from the repo
    _isLoading = true;
    final int weekOfYear = DateTime.now().weekOfYear;

    _currentWeekNumber = weekNr ?? (_currentWeekNumber ?? weekOfYear);

    List<SchoolDay?>? days = await repo.getWeek(nr: _currentWeekNumber!, force: force);

    // return when an error occurred while fetching the api,
    if (days == null) {
      logger.i("[Timetable Viewmodel] Fetched days were null!");
      return;
    }

    // remove saturday and sunday
    days.removeRange(days.length-2, days.length);

    // set data
    _schoolDays = days;

    // assign fiels
    _isLoading = false;
    _dataFetched = true;

    // relaod after fetching data
    notifyListeners();
  }
}
