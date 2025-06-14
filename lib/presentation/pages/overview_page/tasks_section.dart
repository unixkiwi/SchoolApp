import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:school_app/presentation/pages/overview_page/task_tile.dart';
import 'package:school_app/presentation/viewmodels/overview_page_viewmodel.dart';

class TasksSection extends StatelessWidget {
  const TasksSection({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<OverviewPageViewmodel>(
      builder: (context, viewModel, child) {
        return Column(
          mainAxisAlignment: MainAxisAlignment.start,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            // heading for this section
            Padding(
              padding: const EdgeInsets.symmetric(
                vertical: 16.0,
                horizontal: 8.0,
              ),
              child: Text(
                "Tasks",
                style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),

            // horizontal list of lessons of the days
            viewModel.isLoading ||
                    !viewModel.dataFetched ||
                    viewModel.today == null
                ? Center(child: CircularProgressIndicator())
                : Column(
                  children: [
                    viewModel.today!.getLessonNotes().isEmpty
                    ? Text("No tasks for today :)", style: TextStyle(fontWeight: FontWeight.bold),)
                    : Column(
                        children: [
                          for (var note in viewModel.today!.getLessonNotes())
                            TaskTile(note: note),
                        ],
                      ),
                  ],
                ),
          ],
        );
      },
    );
  }
}
