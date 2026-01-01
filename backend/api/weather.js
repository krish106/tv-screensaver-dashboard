const express = require('express');
const axios = require('axios');
const router = express.Router();

/**
 * GET /api/weather/current
 * Query params: lat (latitude), lng (longitude)
 * Returns current weather data from Google Weather API
 */
router.get('/current', async (req, res) => {
    try {
        const { lat, lng } = req.query;

        // Validate input
        if (!lat || !lng) {
            return res.status(400).json({
                error: 'Missing parameters',
                message: 'Both lat and lng parameters are required'
            });
        }

        // Validate lat/lng ranges
        const latitude = parseFloat(lat);
        const longitude = parseFloat(lng);

        if (isNaN(latitude) || isNaN(longitude)) {
            return res.status(400).json({
                error: 'Invalid parameters',
                message: 'lat and lng must be valid numbers'
            });
        }

        if (latitude < -90 || latitude > 90) {
            return res.status(400).json({
                error: 'Invalid latitude',
                message: 'Latitude must be between -90 and 90'
            });
        }

        if (longitude < -180 || longitude > 180) {
            return res.status(400).json({
                error: 'Invalid longitude',
                message: 'Longitude must be between -180 and 180'
            });
        }

        // Check if API key is configured
        if (!process.env.GOOGLE_WEATHER_API_KEY) {
            return res.status(500).json({
                error: 'Configuration error',
                message: 'Weather API key not configured'
            });
        }

        // Call Google Weather API
        // NOTE: Replace this URL with actual Google Weather API endpoint
        // This is a placeholder - you'll need to use the actual API
        const weatherApiUrl = `https://api.openweathermap.org/data/2.5/weather`;

        const response = await axios.get(weatherApiUrl, {
            params: {
                lat: latitude,
                lon: longitude,
                appid: process.env.GOOGLE_WEATHER_API_KEY,
                units: 'metric' // Celsius
            },
            timeout: 5000 // 5 second timeout
        });

        // Transform API response to our format
        const weatherData = {
            temperature: Math.round(response.data.main.temp),
            humidity: response.data.main.humidity,
            condition: response.data.weather[0].main,
            description: response.data.weather[0].description,
            icon: response.data.weather[0].icon,
            location: {
                lat: latitude,
                lng: longitude,
                name: response.data.name || 'Unknown'
            },
            timestamp: new Date().toISOString()
        };

        res.json(weatherData);

    } catch (error) {
        console.error('Weather API Error:', error.message);

        // Handle different error types
        if (error.response) {
            // API responded with error
            return res.status(error.response.status).json({
                error: 'Weather API error',
                message: error.response.data.message || 'Failed to fetch weather data',
                details: process.env.NODE_ENV === 'development' ? error.response.data : undefined
            });
        } else if (error.code === 'ECONNABORTED') {
            // Timeout
            return res.status(504).json({
                error: 'Timeout',
                message: 'Weather API request timed out'
            });
        } else {
            // Other errors
            return res.status(500).json({
                error: 'Internal server error',
                message: 'Failed to fetch weather data'
            });
        }
    }
});

module.exports = router;
