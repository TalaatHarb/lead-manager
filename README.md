# Lead Manager

A JavaFX desktop application for managing sales leads.

## Features

| Feature | Description |
|---------|-------------|
| **CRUD** | Create, Read, Update, Delete sales leads stored in a MapDB local file |
| **Web Scraping** | Pluggable `LeadFinder` interface; built-in scrapers for Hacker News Jobs & GitHub Trending (jsoup) |
| **Groovy Scripting** | Write and run custom lead-finder scripts in the built-in code editor |
| **Code Editor** | RichTextFX `CodeArea` with line numbers and Groovy syntax support |
| **Graph Editor** | Visual node-graph builder for lead-finder pipelines (JGraphT model, JavaFX canvas renderer) |

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
├── scraper/
│   ├── LeadFinder.java             # Interface for all scrapers
│   ├── HackerNewsLeadFinder.java   # jsoup scraper
│   └── GithubTrendingLeadFinder.java
├── scripting/
│   ├── GroovyScriptRunner.java     # JSR-223 Groovy engine
│   └── GroovyLeadFinder.java       # Script-backed LeadFinder
├── graph/
│   ├── LeadFinderGraph.java        # JGraphT-backed pipeline graph
│   └── LeadFinderNode.java         # Graph node (SCRAPE/FILTER/ENRICH/SCRIPT/OUTPUT)
└── ui/
    ├── controller/MainController.java
    └── view/
        ├── CodeEditorView.java     # RichTextFX editor pane
        └── GraphEditorView.java    # Node-graph editor pane
```
