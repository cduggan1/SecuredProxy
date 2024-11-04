# Java HTTP/HTTPS Proxy Server

A lightweight, multithreaded HTTP/HTTPS proxy server built in Java. This server logs and monitors URLs accessed through the proxy and includes configurable controls such as a denylist for restricted URLs. Designed to handle HTTP and HTTPS connections, with support for Server Name Indication (SNI) filtering and connection denial.

## Features

- **HTTP and HTTPS Support**: Handles both standard HTTP connections and HTTPS connections using `CONNECT` requests.
- **Denylist Filtering**: Blocks connections to specified URLs or hostnames by referencing a configurable denylist. Currently, requests to specific hardcoded domains (e.g., `facebook.com`) are denied.
- **Server Name Indication (SNI) Inspection**: For HTTPS requests, the proxy inspects the SNI in the TLS handshake to identify the requested hostname, enabling denylist filtering even for encrypted HTTPS connections.
- **Detailed Logging**: Logs all incoming requests, with options for verbose logging to monitor detailed activity for debugging and analysis.
- **Non-blocking Data Transfer**: Uses non-blocking I/O with threads to manage bidirectional data transfer between client and server, ensuring efficient performance for multiple simultaneous connections.
- **Future-Proof Design**: Architected with modularity in mind, enabling easy extension for additional features, such as loading denylist URLs from external files or adding logging levels.

## Usage

### Prerequisites

- **Java 8+**: Ensure you have Java installed on your system.
- **Port Configuration**: Default port is set to `8081`, but it can be configured in the code.

### Running the Proxy Server

1. **Compile the Project**:
   Default Maven Compilation
