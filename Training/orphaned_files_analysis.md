# Orphaned Files Analysis

This document tracks files that are not linked from the main `_Table_of_Contents.md` or are suspected to be unused within the project. The purpose of this analysis is to keep the repository clean and easy to navigate.

---

## 1. Documentation Orphans (`/Training` Directory)

The following documents were found in the `/Training` directory but are not linked in `_Table_of_Contents.md`.

| File Name | Status | Recommendation & Notes |
| :--- | :--- | :--- |
| `data_models_and_schema.md` | **Unlinked** | **Integrate**. This file was refactored to explain our data modeling *strategy*. It should be added to the Table of Contents under "Project Foundation & Core Principles". |
| `feature_spec_receipt_management.md` | **Orphaned** | **Review then Delete**. This appears to be an early spec. Review for any unique concepts not captured elsewhere, then delete. |
| `ui_component_library.md` | **Unlinked** | **Integrate**. This is a valuable inventory of our reusable UI components. It has been added to the Table of Contents. |
| `project_progress.md` | **Deleted** | This file contained outdated status information and was superseded by the `technical_roadmap.md`. It has been deleted. |
| `TO_REVIEW.md` | **Deleted** | This was a temporary checklist. All action items were completed or superseded by other documents. It has been deleted. |

---

## 2. Source Code Orphans (`/app` Directory)

*Analysis pending.*

Automatically detecting orphaned source code files is complex and requires static analysis tools that can build a full dependency graph of the project. A manual review would be required to identify unused classes or modules with high confidence.
