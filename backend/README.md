# SpeedTest Backend Server

Simple PHP backend for Android SpeedTest app.

## Requirements

- PHP 7.4 or higher
- Web server (Nginx/Apache)

## Quick Start

### Option 1: PHP Built-in Server (Development)

```bash
cd backend
php -S 0.0.0.0:8080
```

### Option 2: Nginx + PHP-FPM (Production)

1. Install dependencies:
```bash
sudo apt-get install nginx php-fpm
```

2. Copy nginx config:
```bash
sudo cp nginx.conf /etc/nginx/sites-available/speedtest
sudo ln -s /etc/nginx/sites-available/speedtest /etc/nginx/sites-enabled/
```

3. Copy PHP files:
```bash
sudo mkdir -p /var/www/speedtest
sudo cp index.php /var/www/speedtest/
sudo chown -R www-data:www-data /var/www/speedtest
```

4. Restart services:
```bash
sudo systemctl restart nginx
sudo systemctl restart php-fpm
```

### Option 3: Apache (Alternative)

1. Install Apache and PHP:
```bash
sudo apt-get install apache2 php libapache2-mod-php
```

2. Create virtual host:
```bash
sudo cp apache.conf /etc/apache2/sites-available/speedtest.conf
sudo a2ensite speedtest
sudo systemctl restart apache2
```

## API Endpoints

### GET/HEAD /ping
Test server connectivity and latency.

**Response:**
```json
{
    "status": "ok",
    "timestamp": 1234567890,
    "server": "SpeedTest-PHP"
}
```

### GET /download?size=5242880
Download test data.

**Parameters:**
- `size` (optional): Bytes to download (default: 5MB, max: 100MB)

**Response:** Binary data stream

### POST /upload
Upload test data.

**Request Body:** Binary data

**Response:**
```json
{
    "status": "ok",
    "received": 2097152,
    "timestamp": 1234567890
}
```

### GET /servers
Get available server list.

**Response:**
```json
[
    {
        "id": "server_local",
        "name": "Local Server",
        "host": "192.168.1.100",
        "port": 8080
    }
]
```

### GET /info
Get server information.

**Response:**
```json
{
    "server": "SpeedTest-PHP",
    "version": "1.0.0",
    "php_version": "8.1.2",
    "client_ip": "192.168.1.50"
}
```

## Testing

Test the server is working:

```bash
# Test ping
curl http://localhost:8080/ping

# Test download (1MB)
curl http://localhost:8080/download?size=1048576 -o /dev/null

# Test upload (1MB)
dd if=/dev/zero bs=1M count=1 | curl -X POST http://localhost:8080/upload --data-binary @-

# Get server info
curl http://localhost:8080/info
```

## Android App Configuration

Update your Android app to point to this server:

1. Find your server IP:
```bash
ip addr show
```

2. In Android app, change server host in `ServerRepositoryImpl.kt`:
```kotlin
host = "YOUR_SERVER_IP_HERE",
port = 8080
```

## Troubleshooting

### Port already in use
```bash
# Find process using port 8080
sudo lsof -i :8080
# Kill it or use different port
```

### Permission denied
```bash
sudo chown -R www-data:www-data /var/www/speedtest
sudo chmod -R 755 /var/www/speedtest
```

### CORS errors
Check that CORS headers are being sent:
```bash
curl -I http://localhost:8080/ping
```

## Security Notes

- This is a basic implementation for testing
- For production, add:
  - Authentication
  - Rate limiting
  - SSL/TLS encryption
  - Input validation
