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
│   ├── LeadFinderGraphSerializer.java # XML serialization for graph lead finders
│   └── LeadFinderNode.java         # Graph node (SCRAPE/FILTER/ENRICH/SCRIPT/OUTPUT)
└── ui/
    ├── controller/MainController.java
    └── view/
        ├── CodeEditorView.java     # RichTextFX editor pane
        ├── ../GroovySyntaxHighlighter.java # Groovy editor highlighter
        └── GraphEditorView.java    # Node-graph editor pane
```
