import 'package:flutter/widgets.dart';
import 'package:intl/intl.dart';
import 'package:week_of_year/week_of_year.dart';

class DateString {
  final int year;
  final int weekNr;

  const DateString({required this.year, required this.weekNr});

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is DateString && other.year == year && other.weekNr == weekNr;
  }

  @override
  int get hashCode => year ^ weekNr;

  @override
  String toString() {
    return "$year-$weekNr";
  }

  DateString nextWeek() {
    int year;
    int weekNr;

    if (this.weekNr == DateTime(this.year).lastWeekOfYear()) {
      weekNr = 1;
      year = this.year + 1;
    } else {
      weekNr = this.weekNr + 1;
      year = this.year;
    }

    return DateString(year: year, weekNr: weekNr);
  }

  DateString prevWeek() {
    int year;
    int weekNr;

    if (this.weekNr <= 1) {
      weekNr = DateTime(this.year - 1).lastWeekOfYear();
      year = this.year - 1;
    } else {
      weekNr = this.weekNr - 1;
      year = this.year;
    }

    return DateString(year: year, weekNr: weekNr);
  }

  factory DateString.now() {
    DateTime now = DateTime.now();

    int year;
    int weekNr;

    if (now.weekday > 5) {
      if (now.isLastWeekOfYear()) {
        year = now.year + 1;
        weekNr = 1;
      } else {
        year = now.year;
        weekNr = now.weekOfYear + 1;
      }
    } else {
      year = now.year;
      weekNr = now.weekOfYear;
    }

    return DateString(year: year, weekNr: weekNr);
  }

  factory DateString.fromDate(DateTime date) {
    int year;
    int weekNr;

    if (date.weekday > 5) {
      if (date.isLastWeekOfYear()) {
        year = date.year + 1;
        weekNr = 1;
      } else {
        year = date.year;
        weekNr = date.weekOfYear + 1;
      }
    } else {
      year = date.year;
      weekNr = date.weekOfYear;
    }

    return DateString(year: year, weekNr: weekNr);
  }

  factory DateString.fromString(String dateString) {
    int year;
    int weekNr;

    try {
      List parsedString = dateString.split("-");

      String yearStr = parsedString.first;
      int? yearInt = int.tryParse(yearStr);
      if (yearStr.length < 4) {
        throw ErrorDescription(
          "Error while calling DateString.fromString($dateString): The given year part is too short!",
        );
      } else if (yearInt == null) {
        throw ErrorDescription(
          "Error while calling DateString.fromString($dateString): The given year part is not convertable to an integer!",
        );
      } else {
        year = yearInt;
      }

      if (parsedString.length < 2)
        throw ErrorDescription(
          "Error while calling DateString.fromString($dateString): The given string is not compatible with the format year-weekNr! No weekNr part was given!",
        );

      String weekStr = parsedString[1];
      int? weekInt = int.tryParse(weekStr);
      if (weekStr.isEmpty) {
        throw ErrorDescription(
          "Error while calling DateString.fromString($dateString): The given weekNr is empty!",
        );
      } else if (weekInt == null) {
        throw ErrorDescription(
          "Error while calling DateString.fromString($dateString): The given week part is not convertable to an integer!",
        );
      } else {
        weekNr = weekInt;
      }
    } catch (e) {
      throw ErrorDescription(
        "An error occured while calling DateString.fromString($dateString): $e",
      );
    }

    return DateString(year: year, weekNr: weekNr);
  }
}

extension DateTimeExtension on DateTime {
  int lastWeekOfYear() {
    DateTime dec28 = DateTime(year, 12, 28);
    int dayOfDec28 = int.parse(DateFormat("D").format(dec28));
    return ((dayOfDec28 - dec28.weekday + 10) / 7).floor();
  }

  bool isLastWeekOfYear() {
    final int currentWeek = weekOfYear;
    final int lastWeek = lastWeekOfYear();
    return currentWeek == lastWeek;
  }
}
