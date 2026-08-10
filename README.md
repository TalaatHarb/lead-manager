# Lead Manager

A JavaFX desktop application for managing sales leads.

## Features

| Feature | Description |
|---------|-------------|
| **CRUD** | Create, Read, Update, Delete sales leads stored in a MapDB local file |
| **Web Scraping** | Pluggable `LeadFinder` interface; built-in scrapers for Hacker News Jobs, GitHub Trending, LinkedIn Jobs, Y Combinator Jobs, Product Hunt Today, and Wellfound (jsoup) |
| **Groovy Scripting** | Write, run, highlight, and track custom lead-finder scripts in the built-in code editor |
| **Code Editor** | RichTextFX `CodeArea` with line numbers and Groovy syntax highlighting |
| **Graph Editor** | Visual node-graph builder for lead-finder pipelines with graph serialization and tracking support |
| **Lead Finder Tracking** | Persist Java, Groovy, and graph-based lead finder definitions alongside sales leads |

## Tech Stack

| Library | Purpose |
|---------|---------|
| JavaFX 21 | UI framework |
| MapDB 3 | Local file persistence |
| jsoup | HTML web scraping |
| Apache Groovy 4 (JSR-223) | Runtime scripting |
| RichTextFX | Code editor component |
| JGraphT | Graph model for the pipeline editor |
| SLF4J + Logback | Logging |
| JUnit 5 | Testing |

## Build & Run

```bash
# Build
mvn clean package

# Run via JavaFX Maven plugin
mvn javafx:run
```

Requires JDK 21+.

## Project Structure

```
src/main/java/com/talaatharb/leadmanager/
├── LeadManagerApplication.java     # JavaFX entry point
├── entity/SalesLead.java           # Domain entity
├── repository/LeadRepository.java  # MapDB CRUD
├── repository/LeadFinderRepository.java # Persisted lead finder definitions
├── scraper/
│   ├── LeadFinder.java             # Interface for all scrapers
│   ├── HackerNewsLeadFinder.java   # jsoup scraper
│   └── GithubTrendingLeadFinder.java
├── scripting/
│   ├── GroovyScriptRunner.java     # JSR-223 Groovy engine
│   └── GroovyLeadFinder.java       # Script-backed LeadFinder
├── tracking/LeadFinderTracker.java # Tracks Java, Groovy, and graph lead finders
├── graph/
│   ├── LeadFinderGraph.java        # JGraphT-backed pipeline graph
│   ├── LeadFinderGraphSerializer.java # Compact text serialization for graph lead finders
│   └── LeadFinderNode.java         # Graph node (SCRAPE/FILTER/ENRICH/SCRIPT/OUTPUT)
└── ui/
    ├── GroovySyntaxHighlighter.java # Groovy editor highlighter
    ├── controller/MainController.java
    └── view/
        ├── CodeEditorView.java     # RichTextFX editor pane
        └── GraphEditorView.java    # Node-graph editor pane
```

## Product Roadmap Plan

The ideas below keep Lead Manager as a local-first desktop application backed by local MapDB files. Network access should stay optional and limited to lead-discovery runs; day-to-day lead management should continue to work offline.

### Feature Opportunities

| Priority | Idea | Why it fits this app |
|----------|------|----------------------|
| High | Advanced lead search, filters, and saved views | The current table is strong for CRUD, but the next step is helping users work larger lead lists by status, source, company, and recency. |
| High | Duplicate detection and merge workflow | Scrapers, scripts, and manual entry can all create overlapping leads, so the app needs a local-first way to flag, compare, and merge duplicates safely. |
| High | Lead activity timeline | Notes exist today, but a dated timeline for status changes, contact attempts, and follow-up events would make the lead lifecycle much easier to manage. |
| High | Follow-up reminders and due dates | This complements the existing status model and keeps the app useful offline by surfacing the next actions directly from local data. |
| Medium | Import/export for CSV and JSON backups | This improves portability, recovery, and reporting while preserving the local database model as the source of truth. |
| Medium | Scraper run history and result review | The scraper tab already logs runs in-session; persisting run summaries would help users compare sources and audit what was added. |
| Medium | Configurable qualification fields | Add fields like industry, lead score, region, deal size, and tags so the app can support more sales workflows without changing its desktop nature. |
| Medium | Reusable scraper and script templates | The app already supports built-in scrapers and Groovy scripts, so template management would make custom lead-finding easier for non-developers. |
| Medium | Graph pipeline execution and scheduling | The graph editor currently tracks graph definitions; turning saved graphs into runnable pipelines would make that feature operational. |
| Low | Dashboard summaries | Simple local metrics such as leads by source, pipeline stage, and overdue follow-ups would help users prioritize work without adding cloud dependencies. |

### Major Architecture Improvements

| Issue | Recommended direction |
|-------|------------------------|
| The main controller directly owns UI logic, repository access, scraper selection, and background work orchestration. | Introduce an application/service layer so lead CRUD, scraper execution, lead-finder tracking, and future reminder logic are outside the JavaFX controller. |
| Lead data and lead-finder definitions are stored in separate repositories with duplicated persistence patterns. | Create a shared persistence module around the local MapDB files so storage concerns, transactions, backup logic, and schema evolution are handled consistently. |
| Background scraper execution uses ad-hoc threads and pushes results back into the UI inline. | Replace manual thread management with a structured task/executor model that supports progress, cancellation, error reporting, and safer UI updates. |
| The graph editor is a visual skeleton, but the graph model is not yet connected to executable pipeline behavior. | Define a pipeline runtime contract that can execute graph nodes locally while keeping graph editing, graph storage, and graph execution as separate responsibilities. |
| Scraper integrations are mixed with source-specific parsing details and UI-facing usage expectations. | Add clearer boundaries for source adapters, normalization, validation, and duplicate handling so each lead source remains replaceable when site HTML changes. |
| The current domain model is centered on a single lead record plus free-form notes. | Expand the local domain model to include activities, reminders, tags, and import/export metadata while keeping everything stored in the same offline data directory. |
| User settings are implicit in code defaults. | Add a local settings layer for scraper defaults, storage location preferences, backup preferences, and UI defaults so behavior is configurable without editing code. |
| Local persistence currently has little visible support for migration, recovery, or inspection. | Add versioned storage metadata, backup/restore flows, and integrity checks to make the local database safer as the application grows. |

### Suggested Delivery Phases

1. **Strengthen daily lead management**
   - Add search, filtering, saved views, reminders, and activity history.
   - Add duplicate detection and merge workflows before expanding scraper usage.
2. **Make the local data layer safer and easier to operate**
   - Add import/export, backup/restore, storage metadata, and repository-level querying.
   - Introduce local settings so scraper and UI defaults are no longer hard-coded.
3. **Turn automation features into complete workflows**
   - Add persisted scraper run history, reusable script templates, and scheduled execution.
   - Move scraper and script execution to a structured background task model.
4. **Finish the graph-based pipeline concept**
   - Add graph validation, execution, dry-run support, and debugging traces.
   - Keep graph design, execution, and persistence as separate modules so the feature can evolve safely.
