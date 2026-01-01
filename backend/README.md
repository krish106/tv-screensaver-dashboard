# TV Screensaver Backend API

Backend server for the commercial Android TV Screensaver app. Provides secure weather API proxy and future endpoints for subscription management and wallpaper hosting.

## Features

- 🌤️ Weather API proxy (hides API keys from client)
- 🔒 Security with Helmet.js
- 🚦 Rate limiting (100 requests/hour per IP)
- 🌐 CORS configuration for Android app
- ⚡ Express.js for high performance
- 📊 Health check and version endpoints

## Setup

### 1. Install Dependencies

```bash
cd backend
npm install
```

### 2. Configure Environment

Create `.env` file from template:

```bash
cp .env.example .env
```

Edit `.env` and add your API keys:

```env
# Get API key from: https://openweathermap.org/api
GOOGLE_WEATHER_API_KEY=your_actual_api_key_here
PORT=3000
NODE_ENV=development
```

### 3. Run Locally

Development mode (with auto-reload):

```bash
npm run dev
```

Production mode:

```bash
npm start
```

## API Endpoints

### Weather

**GET** `/api/weather/current?lat={latitude}&lng={longitude}`

Returns current weather for the given coordinates.

**Example Request:**

```bash
curl "http://localhost:3000/api/weather/current?lat=23.0225&lng=72.5714"
```

**Example Response:**

```json
{
  "temperature": 28,
  "humidity": 65,
  "condition": "Clear",
  "description": "clear sky",
  "icon": "01d",
  "location": {
    "lat": 23.0225,
    "lng": 72.5714,
    "name": "Ahmedabad"
  },
  "timestamp": "2025-01-26T14:30:00.000Z"
}
```

### Health Check

**GET** `/api/health`

Returns server status.

**Example Response:**

```json
{
  "status": "OK",
  "timestamp": "2025-01-26T14:30:00.000Z",
  "uptime": 3600,
  "version": "1.0.0"
}
```

### Version

**GET** `/api/version`

Returns API version and available endpoints.

## Deployment (VPS)

### 1. VPS Setup

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Node.js 20
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs

# Install PM2
sudo npm install -g pm2

# Install Nginx
sudo apt install -y nginx

# Install certbot for SSL
sudo apt install -y certbot python3-certbot-nginx
```

### 2. Deploy Application

```bash
# Clone repository
cd /var/www
sudo git clone <your-repo-url> tv-screensaver-backend
cd tv-screensaver-backend

# Install dependencies
npm install --production

# Set up environment
sudo nano .env
# Add your production environment variables

# Start with PM2
pm2 start server.js --name="tv-screensaver-api"
pm2 save
pm2 startup
```

### 3. Configure Nginx

Create `/etc/nginx/sites-available/tv-screensaver-api`:

```nginx
server {
    listen 80;
    server_name api.yourapp.com;

    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

Enable site:

```bash
sudo ln -s /etc/nginx/sites-available/tv-screensaver-api /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

### 4. SSL Certificate

```bash
sudo certbot --nginx -d api.yourapp.com
```

### 5. Monitor

```bash
# View logs
pm2 logs tv-screensaver-api

# Monitor
pm2 monit

# Restart
pm2 restart tv-screensaver-api
```

## Security

- ✅ API keys stored in environment variables (never committed to git)
- ✅ Rate limiting prevents abuse
- ✅ CORS restricts access to Android app only
- ✅ Helmet.js adds security headers
- ✅ Input validation on all parameters
- ✅ SSL/TLS encryption in production

## Cost

- **VPS:** ₹600/month (DigitalOcean Basic Droplet, 1GB RAM)
- **Domain:** ₹100/month
- **OpenWeather API:** Free tier (1,000 calls/day)

**Total:** ₹700/month

## Future Endpoints

Coming in Phase 3 & 4:

- `POST /api/billing/verify` - Verify Google Play subscription
- `GET /api/wallpapers/list` - Get curated wallpaper list
- `POST /api/wallpapers/upload` - Upload custom wallpaper (premium)

## Monitoring

Set up free monitoring with UptimeRobot:

- URL: `https://api.yourapp.com/api/health`
- Check interval: 5 minutes
- Alert: Email if down

## License

Proprietary - All rights reserved
