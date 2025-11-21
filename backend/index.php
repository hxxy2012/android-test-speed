<?php
/**
 * SpeedTest Backend Server - Main Entry Point
 * Simple PHP implementation for speed testing
 */

header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, HEAD, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');
header('Content-Type: application/json');

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

// Parse request URI
$request_uri = $_SERVER['REQUEST_URI'];
$path = parse_url($request_uri, PHP_URL_PATH);

// Route requests
switch ($path) {
    case '/ping':
        handlePing();
        break;

    case '/download':
        handleDownload();
        break;

    case '/upload':
        handleUpload();
        break;

    case '/servers':
        handleServers();
        break;

    case '/info':
        handleInfo();
        break;

    default:
        http_response_code(404);
        echo json_encode(['error' => 'Not found']);
        break;
}

/**
 * Handle ping requests
 */
function handlePing() {
    if ($_SERVER['REQUEST_METHOD'] === 'HEAD' || $_SERVER['REQUEST_METHOD'] === 'GET') {
        header('X-Server-Time: ' . microtime(true));
        echo json_encode([
            'status' => 'ok',
            'timestamp' => time(),
            'server' => 'SpeedTest-PHP'
        ]);
    }
}

/**
 * Handle download speed test
 */
function handleDownload() {
    // Get requested size (default 5MB)
    $size = isset($_GET['size']) ? intval($_GET['size']) : (5 * 1024 * 1024);

    // Limit maximum size to 100MB
    $size = min($size, 100 * 1024 * 1024);

    header('Content-Type: application/octet-stream');
    header('Content-Length: ' . $size);
    header('Cache-Control: no-cache, no-store, must-revalidate');

    // Generate random data in chunks to avoid memory issues
    $chunk_size = 1024 * 1024; // 1MB chunks
    $sent = 0;

    while ($sent < $size) {
        $remaining = $size - $sent;
        $current_chunk = min($chunk_size, $remaining);

        // Generate random data
        echo str_repeat('0', $current_chunk);

        $sent += $current_chunk;

        // Flush output buffer
        if (ob_get_level() > 0) {
            ob_flush();
        }
        flush();
    }
}

/**
 * Handle upload speed test
 */
function handleUpload() {
    if ($_SERVER['REQUEST_METHOD'] === 'POST') {
        // Read uploaded data
        $input = file_get_contents('php://input');
        $size = strlen($input);

        echo json_encode([
            'status' => 'ok',
            'received' => $size,
            'timestamp' => time()
        ]);
    } else {
        http_response_code(405);
        echo json_encode(['error' => 'Method not allowed']);
    }
}

/**
 * Handle server list request
 */
function handleServers() {
    $servers = [
        [
            'id' => 'server_local',
            'name' => 'Local Server',
            'country' => 'Local',
            'city' => 'Local',
            'host' => $_SERVER['SERVER_ADDR'] ?? 'localhost',
            'port' => $_SERVER['SERVER_PORT'] ?? 80,
            'latitude' => 0.0,
            'longitude' => 0.0,
            'sponsor' => 'Self-Hosted',
            'isActive' => true
        ]
    ];

    echo json_encode($servers);
}

/**
 * Handle server info request
 */
function handleInfo() {
    $info = [
        'server' => 'SpeedTest-PHP',
        'version' => '1.0.0',
        'php_version' => PHP_VERSION,
        'server_time' => time(),
        'client_ip' => $_SERVER['REMOTE_ADDR'] ?? 'unknown'
    ];

    echo json_encode($info);
}
