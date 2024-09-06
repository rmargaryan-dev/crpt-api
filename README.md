# Chestny ZNAK API Integration

This project provides a simple Java-based interface for interacting with the Chestny ZNAK API, specifically for creating documents. The `CrptApi` class handles API calls, manages request limits, and processes documents for submission.


## Setup

### Prerequisites

- Java 11 or higher
- Maven for dependency management

### Installation

Clone the repository and include the `CrptApi` class in your project:

```bash
git clone https://github.com/rmargaryan-dev/crpt-api.git
```

## Usage

### Properties

After cloning repository you should set properties "auth.token" and "api.url" in `resources/application.properties`.

### Class and functionality

The class `CrptApi` contains only method that should be used for calling api.
The method called `createDocument` should receive a document object of any type and signature of type String 
that should be in base64 format as considered. After using this method a message about document creation or problems
will be printed.

