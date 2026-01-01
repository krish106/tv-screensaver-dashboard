const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
require('dotenv').config();

const weatherRouter = require('./api/weather');

const app = express();
const PORT = process.env.PORT || 3000;

// Security middleware
app.use(helmet());

// CORS configuration
const corsOptions = {
  origin: function (origin, callback) {
    // Allow requests from Android app or no origin (like mobile apps)
    const allowedOrigins = (process.env.ALLOWED_ORIGINS || '').split(',');
    if (!origin || allowedOrigins.includes(origin)) {
      callback(null, true);
    } else {
      callback(new Error('Not allowed by CORS'));
    }
  },
  optionsSuccessStatus: 200
};
app.use(cors(corsOptions));

// Rate limiting
const limiter = rateLimit({
  windowMs: parseInt(process.env.RATE_LIMIT_WINDOW_MS) || 3600000, // 1 hour
  max: parseInt(process.env.RATE_LIMIT_MAX_REQUESTS) || 100,
  message: 'Too many requests from this IP, please try again later.',
  standardHeaders: true,
  legacyHeaders: false,
});
app.use('/api/', limiter);

// Body parsing
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Routes
app.use('/api/weather', weatherRouter);

// Health check
app.get('/api/health', (req, res) => {
  res.json({
    status: 'OK',
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
    version: '1.0.0'
  });
});

// API version
app.get('/api/version', (req, res) => {
  res.json({
    version: '1.0.0',
    apiName: 'TV Screensaver Backend',
    endpoints: {
      weather: '/api/weather/current',
      health: '/api/health',
      version: '/api/version'
    }
  });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({
    error: 'Not Found',
    message: 'The requested endpoint does not exist',
    path: req.path
  });
});

// Error handler
app.use((err, req, res, next) => {
  console.error('Error:', err);
  res.status(err.status || 500).json({
    error: err.message || 'Internal Server Error',
    ...(process.env.NODE_ENV === 'development' && { stack: err.stack })
  });
});

// Start server
app.listen(PORT, () => {
  console.log(`🚀 TV Screensaver Backend API running on port ${PORT}`);
  console.log(`📍 Environment: ${process.env.NODE_ENV}`);
  console.log(`🌡️  Weather API: ${process.env.GOOGLE_WEATHER_API_KEY ? 'Configured ✓' : 'NOT CONFIGURED ✗'}`);
  console.log(`\n📋 Available endpoints:`);
  console.log(`   GET  /api/health`);
  console.log(`   GET  /api/version`);
  console.log(`   GET  /api/weather/current?lat={lat}&lng={lng}`);
});

module.exports = app;
